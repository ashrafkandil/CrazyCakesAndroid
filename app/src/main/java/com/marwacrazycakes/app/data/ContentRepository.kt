package com.marwacrazycakes.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.marwacrazycakes.app.BuildConfig
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * On-device SQLite content store. Mirrors the React Native `src/db/database.ts`:
 * the bundled database is the first-launch/offline fallback, and a validated content
 * pack can replace it at runtime without shipping a new build.
 */
object ContentRepository {

    const val GALLERY_PAGE_SIZE = 6

    private const val TAG = "ContentRepository"
    private const val DATABASE_NAME = "crazy-cakes.db"
    private const val META_TABLE = "mobile_meta"
    private const val VERSION_KEY = "content_version"
    private val VERSION_PATTERN = Regex("^[a-f0-9]{64}$")
    private val REQUIRED_TABLES = listOf(
        "categories",
        "images",
        "videos",
        "testimonials",
        "content",
        "event_types",
        "social_links",
    )
    private val CONTENT_BASE_URL = BuildConfig.MOBILE_CONTENT_URL.trim().trimEnd('/')

    private lateinit var appContext: Context
    @Volatile private var initialized = false
    @Volatile private var activeDatabase: SQLiteDatabase? = null
    @Volatile private var fallbackDatabase: SQLiteDatabase? = null
    @Volatile private var cache: ContentSnapshot = ContentSnapshot()
    private val updateMutex = Mutex()
    private val _revision = MutableStateFlow(0)
    private val _contentVersion = MutableStateFlow("bundled")

    val revision: StateFlow<Int> = _revision.asStateFlow()
    val contentVersion: StateFlow<String> = _contentVersion.asStateFlow()

    fun isInitialized(): Boolean = initialized

    suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        if (initialized) return@withContext
        synchronized(this) {
            if (initialized) return@synchronized
            appContext = context.applicationContext
            val target = File(appContext.filesDir, DATABASE_NAME)
            val installBundled = {
                appContext.assets.open(DATABASE_NAME).use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
            if (!target.exists()) {
                installBundled()
            } else {
                var existing: SQLiteDatabase? = null
                try {
                    existing = openDatabase(target)
                    validateDatabase(existing)
                } catch (error: Exception) {
                    Log.w(TAG, "Installed database is invalid; replacing it with the bundled copy.", error)
                    existing?.close()
                    existing = null
                    target.delete()
                    installBundled()
                } finally {
                    existing?.close()
                }
            }

            val bundled = openDatabase(target)
            bundled.execSQL(
                "CREATE TABLE IF NOT EXISTS $META_TABLE (key TEXT PRIMARY KEY, value TEXT NOT NULL)",
            )
            validateDatabase(bundled)
            var chosen = bundled
            var chosenCache = readContentCache(bundled)
            var version = "bundled"

            val installedVersion = readMetaVersion(bundled)
            if (installedVersion != null && VERSION_PATTERN.matches(installedVersion)) {
                val cachedFile = File(appContext.filesDir, "mobile-content-$installedVersion.db")
                if (cachedFile.exists()) {
                    var candidate: SQLiteDatabase? = null
                    try {
                        candidate = openDatabase(cachedFile)
                        candidate.execSQL(
                            "CREATE TABLE IF NOT EXISTS $META_TABLE (key TEXT PRIMARY KEY, value TEXT NOT NULL)",
                        )
                        validateDatabase(candidate)
                        val cachedVersion = readMetaVersion(candidate)
                        if (cachedVersion != installedVersion) {
                            throw IllegalStateException(
                                "Cached database version does not match the saved content version.",
                            )
                        }
                        chosen = candidate
                        chosenCache = readContentCache(candidate)
                        version = installedVersion
                    } catch (error: Exception) {
                        Log.w(TAG, "Could not restore downloaded content; using bundled database.", error)
                        candidate?.close()
                        cachedFile.delete()
                        clearMetaVersion(bundled)
                    }
                } else {
                    clearMetaVersion(bundled)
                }
            } else if (installedVersion != null) {
                clearMetaVersion(bundled)
            }

            fallbackDatabase = bundled
            activeDatabase = chosen
            cache = chosenCache
            _contentVersion.value = version
            initialized = true
            _revision.value += 1
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch { checkForContentUpdates() }
    }

    fun content(): Map<String, String> = cache.content

    fun text(key: String): String =
        cache.content[key] ?: "Missing content slot \"$key\""

    fun testimonials(): List<Testimonial> = cache.testimonials

    fun eventTypes(): List<String> = cache.eventTypes

    fun socialLinks(): List<SocialLink> = cache.socialLinks

    fun getGallery(category: String? = null, page: Int = 1, shuffleSeed: String = "gallery-default"): GalleryPage {
        val active = if (category.isNullOrEmpty() || category == "all") null else category
        val snapshot = cache
        val filtered = if (active == null) snapshot.images else snapshot.images.filter { it.slug == active }
        val ordered = if (active == null) shuffleWithSeed(filtered, shuffleSeed) else filtered
        val videos = if (active == null) snapshot.videos else snapshot.videos.filter { it.slug == active }
        val total = ordered.size
        val pageCount = maxOf(1, (total + GALLERY_PAGE_SIZE - 1) / GALLERY_PAGE_SIZE)
        val safePage = page.coerceIn(1, pageCount)
        val offset = (safePage - 1) * GALLERY_PAGE_SIZE
        return GalleryPage(
            categories = snapshot.categories,
            images = ordered.drop(offset).take(GALLERY_PAGE_SIZE),
            videos = videos,
            activeCategory = active ?: "all",
            page = safePage,
            pageSize = GALLERY_PAGE_SIZE,
            total = total,
            pageCount = pageCount,
        )
    }

    fun contentMediaUrl(src: String): String? {
        if (CONTENT_BASE_URL.isEmpty()) return null
        val clean = decodePath(src.substringBefore('?').replace('\\', '/')) ?: return null
        val dot = clean.lastIndexOf('.')
        val slash = clean.lastIndexOf('/')
        val path = if (dot > slash) {
            clean.substring(0, dot) + clean.substring(dot).lowercase(Locale.US)
        } else {
            clean
        }
        if (!path.startsWith("/assets/crazy/") && !path.startsWith("/assets/videos/")) return null
        val encoded = Uri.encode(path, "/")
        val version = Uri.encode(_contentVersion.value)
        return "$CONTENT_BASE_URL/media$encoded?v=$version"
    }

    fun assetUri(src: String): String? {
        val rel = relativeAssetPath(src) ?: return null
        return "file:///android_asset/" + Uri.encode(rel, "/")
    }

    fun mediaAssetUri(src: String): String? {
        val rel = relativeAssetPath(src) ?: return null
        return "asset:///" + Uri.encode(rel, "/")
    }

    fun logoAssetUri(): String = "file:///android_asset/images/logo.png"

    fun posterFor(video: GalleryVideo): String =
        if (video.slug == "wedding") {
            "/assets/crazy/Wedding/0_IMG_4922.JPEG"
        } else {
            "/assets/crazy/Celebrations/0_IMG_3530.JPEG"
        }

    private fun relativeAssetPath(src: String): String? {
        val clean = decodePath(src.substringBefore('?').replace('\\', '/')) ?: return null
        val relative = when {
            clean.startsWith("/assets/crazy/") -> "images/" + clean.removePrefix("/assets/crazy/")
            clean.startsWith("/assets/videos/") -> "videos/" + clean.removePrefix("/assets/videos/")
            else -> return null
        }
        val dot = relative.lastIndexOf('.')
        val slash = relative.lastIndexOf('/')
        return if (dot > slash) {
            relative.substring(0, dot) + relative.substring(dot).lowercase(Locale.US)
        } else {
            relative
        }
    }

    private fun decodePath(value: String): String? = try {
        Uri.decode(value)
    } catch (_: Exception) {
        null
    }

    suspend fun checkForContentUpdates(): Boolean {
        if (!initialized || CONTENT_BASE_URL.isEmpty()) return false
        return updateMutex.withLock {
            withContext(Dispatchers.IO) { downloadContentUpdate() }
        }
    }

    private fun downloadContentUpdate(): Boolean {
        var candidate: SQLiteDatabase? = null
        var downloadedFile: File? = null
        try {
            val manifestUrl = "$CONTENT_BASE_URL/manifest.json"
            val manifest = JSONObject(httpGetText(manifestUrl, "application/json"))
            val version = manifest.optString("version", "")
            if (!VERSION_PATTERN.matches(version)) {
                throw IllegalArgumentException("Mobile content manifest has an invalid version.")
            }
            val database = manifest.optJSONObject("database")
                ?: throw IllegalArgumentException("Mobile content manifest has no database descriptor.")
            val file = database.optString("file", "")
            val bytes = database.optLong("bytes", -1)
            val sha256 = database.optString("sha256", "")
            if (file != DATABASE_NAME || bytes <= 0 || !VERSION_PATTERN.matches(sha256)) {
                throw IllegalArgumentException("Mobile content manifest has an invalid database descriptor.")
            }

            val current = activeDatabase?.let { readMetaVersion(it) }
            if (current == version) return false

            val downloadDirectory = File(appContext.cacheDir, "mobile-content-downloads")
            downloadDirectory.mkdirs()
            val download = File(downloadDirectory, "crazy-cakes.db")
            if (download.exists()) download.delete()
            try {
                val databaseUrl = "$CONTENT_BASE_URL/$file?version=${Uri.encode(version)}"
                downloadBinary(databaseUrl, download, bytes)
                val digest = sha256(download)
                if (!digest.equals(sha256, ignoreCase = true)) {
                    throw IllegalStateException("Downloaded content database failed SHA-256 verification.")
                }
                val databaseName = "mobile-content-$version.db"
                val destination = File(appContext.filesDir, databaseName)
                if (destination.exists()) destination.delete()
                if (!download.renameTo(destination)) {
                    download.copyTo(destination, overwrite = true)
                    download.delete()
                }
                downloadedFile = destination

                candidate = openDatabase(destination)
                candidate.execSQL(
                    "CREATE TABLE IF NOT EXISTS $META_TABLE (key TEXT PRIMARY KEY, value TEXT NOT NULL)",
                )
                validateDatabase(candidate)
                saveMetaVersion(candidate, version)
                val nextCache = readContentCache(candidate)

                fallbackDatabase?.let { saveMetaVersion(it, version) }
                val previous = activeDatabase
                activeDatabase = candidate
                cache = nextCache
                _contentVersion.value = version
                _revision.value += 1
                candidate = null
                if (previous != null && previous !== fallbackDatabase) {
                    try {
                        previous.close()
                    } catch (error: Exception) {
                        Log.w(TAG, "Prior content database did not close cleanly.", error)
                    }
                }
                return true
            } finally {
                if (download.exists()) download.delete()
            }
        } catch (error: Exception) {
            Log.w(TAG, "Unable to refresh mobile content; keeping the last valid local database.", error)
            return false
        } finally {
            candidate?.close()
            if (downloadedFile != null && candidate != null) {
                downloadedFile.delete()
            }
        }
    }

    private fun openDatabase(file: File): SQLiteDatabase =
        SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READWRITE)

    private fun validateDatabase(database: SQLiteDatabase) {
        database.rawQuery("PRAGMA integrity_check", null).use { cursor ->
            if (!cursor.moveToFirst() || cursor.getString(0) != "ok") {
                throw IllegalStateException("SQLite integrity check failed.")
            }
        }
        for (table in REQUIRED_TABLES) {
            database.rawQuery("SELECT 1 FROM $table LIMIT 0", null).use { }
        }
        database.rawQuery("SELECT count(*) FROM categories", null).use { cursor ->
            if (!cursor.moveToFirst() || cursor.getInt(0) == 0) {
                throw IllegalStateException("Mobile content database contains no categories.")
            }
        }
    }

    private fun readContentCache(database: SQLiteDatabase): ContentSnapshot {
        val categories = mutableListOf<GalleryCategory>()
        database.rawQuery(
            "SELECT id, slug, title, sort FROM categories ORDER BY sort",
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                categories += GalleryCategory(
                    id = cursor.getInt(0),
                    slug = cursor.getString(1),
                    title = cursor.getString(2),
                    sort = cursor.getInt(3),
                )
            }
        }

        val images = mutableListOf<GalleryImage>()
        database.rawQuery(
            """
            SELECT i.id, i.src, i.alt, i.width, i.height, i.sort, i.featured, i.category_id,
                   c.slug, c.title
            FROM images i JOIN categories c ON c.id = i.category_id
            ORDER BY i.sort, i.id
            """.trimIndent(),
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                images += GalleryImage(
                    id = cursor.getInt(0),
                    src = cursor.getString(1),
                    alt = cursor.getString(2),
                    width = if (cursor.isNull(3)) null else cursor.getInt(3),
                    height = if (cursor.isNull(4)) null else cursor.getInt(4),
                    sort = cursor.getInt(5),
                    featured = cursor.getInt(6) == 1,
                    categoryId = cursor.getInt(7),
                    slug = cursor.getString(8),
                    categoryTitle = cursor.getString(9),
                )
            }
        }

        val videos = mutableListOf<GalleryVideo>()
        database.rawQuery(
            """
            SELECT v.id, v.src, v.title, v.sort, v.category_id, c.slug, c.title
            FROM videos v JOIN categories c ON c.id = v.category_id
            ORDER BY v.sort, v.id
            """.trimIndent(),
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                videos += GalleryVideo(
                    id = cursor.getInt(0),
                    src = cursor.getString(1),
                    title = cursor.getString(2),
                    sort = cursor.getInt(3),
                    categoryId = cursor.getInt(4),
                    slug = cursor.getString(5),
                    categoryTitle = cursor.getString(6),
                )
            }
        }

        val testimonials = mutableListOf<Testimonial>()
        database.rawQuery(
            """
            SELECT id, author, initials, review_date, quote, rating
            FROM testimonials WHERE published = 1 ORDER BY sort, id
            """.trimIndent(),
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                testimonials += Testimonial(
                    id = cursor.getInt(0),
                    author = cursor.getString(1),
                    initials = cursor.getString(2),
                    date = cursor.getString(3),
                    quote = cursor.getString(4),
                    rating = cursor.getInt(5),
                )
            }
        }

        val content = LinkedHashMap<String, String>()
        database.rawQuery("SELECT key, value FROM content", null).use { cursor ->
            while (cursor.moveToNext()) content[cursor.getString(0)] = cursor.getString(1)
        }

        val eventTypes = mutableListOf<String>()
        database.rawQuery("SELECT label FROM event_types ORDER BY sort, id", null).use { cursor ->
            while (cursor.moveToNext()) eventTypes += cursor.getString(0)
        }

        val socialLinks = mutableListOf<SocialLink>()
        database.rawQuery(
            "SELECT id, label, href, icon, sort, active FROM social_links WHERE active = 1 ORDER BY sort, id",
            null,
        ).use { cursor ->
            while (cursor.moveToNext()) {
                socialLinks += SocialLink(
                    id = cursor.getInt(0),
                    label = cursor.getString(1),
                    href = cursor.getString(2),
                    icon = cursor.getString(3),
                    sort = cursor.getInt(4),
                    active = cursor.getInt(5) == 1,
                )
            }
        }

        return ContentSnapshot(
            categories = categories,
            images = images,
            videos = videos,
            testimonials = testimonials,
            content = content,
            eventTypes = eventTypes,
            socialLinks = socialLinks,
        )
    }

    private fun readMetaVersion(database: SQLiteDatabase): String? =
        database.rawQuery(
            "SELECT value FROM $META_TABLE WHERE key = ?",
            arrayOf(VERSION_KEY),
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }

    private fun saveMetaVersion(database: SQLiteDatabase, version: String) {
        database.execSQL(
            "INSERT INTO $META_TABLE (key, value) VALUES (?, ?) " +
                "ON CONFLICT(key) DO UPDATE SET value = excluded.value",
            arrayOf(VERSION_KEY, version),
        )
    }

    private fun clearMetaVersion(database: SQLiteDatabase) {
        database.execSQL("DELETE FROM $META_TABLE WHERE key = ?", arrayOf(VERSION_KEY))
    }

    private fun httpGetText(url: String, accept: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 30_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Accept", accept)
        connection.setRequestProperty("Cache-Control", "no-cache")
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Manifest request failed with HTTP $status.")
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun downloadBinary(url: String, destination: File, expectedBytes: Long) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15_000
        connection.readTimeout = 60_000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Cache-Control", "no-cache")
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Content database request failed with HTTP $status.")
            }
            var total = 0L
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > expectedBytes) {
                            throw IllegalStateException("Downloaded content database is larger than declared.")
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            if (total != expectedBytes) {
                throw IllegalStateException(
                    "Downloaded content database size mismatch (expected $expectedBytes, received $total).",
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
    }

    private fun shuffleWithSeed(items: List<GalleryImage>, seed: String): List<GalleryImage> {
        var state = 2166136261u.toInt()
        for (character in seed) {
            state = (state xor character.code) * 16777619
        }
        fun random(): Double {
            state += 0x6d2b79f5
            var value = state
            value = (value xor (value ushr 15)) * (value or 1)
            value = value xor (value + (value xor (value ushr 7)) * (value or 61))
            return ((value xor (value ushr 14)).toLong() and 0xFFFFFFFFL) / 4294967296.0
        }
        val shuffled = items.toMutableList()
        for (index in shuffled.lastIndex downTo 1) {
            val swapIndex = (random() * (index + 1)).toInt()
            val item = shuffled[index]
            shuffled[index] = shuffled[swapIndex]
            shuffled[swapIndex] = item
        }
        return shuffled
    }
}
