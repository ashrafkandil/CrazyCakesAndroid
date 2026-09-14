package com.marwacrazycakes.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

object AppData {

    const val GALLERY_PAGE_SIZE = 6

    private const val ASSET_NAME = "crazy-cakes.db"

    private var database: SQLiteDatabase? = null

    fun init(context: Context) {
        if (database != null) return
        synchronized(this) {
            if (database != null) return
            val target = File(context.filesDir, ASSET_NAME)
            if (!target.exists()) {
                context.assets.open(ASSET_NAME).use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
            database = SQLiteDatabase.openDatabase(target.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }
    }

    private fun db(): SQLiteDatabase =
        database ?: error("AppData.init(context) must be called before querying.")

    fun content(): Map<String, String> =
        db().rawQuery("SELECT key, value FROM content", null).use { cursor ->
            buildMap {
                while (cursor.moveToNext()) {
                    put(cursor.getString(0), cursor.getString(1))
                }
            }
        }

    fun text(content: Map<String, String>, key: String): String =
        content[key] ?: error("Missing content slot \"$key\".")

    fun gallery(category: String?, page: Int): GalleryPage {
        val active = if (category.isNullOrEmpty() || category == "all") null else category

        val categories = db().rawQuery(
            "SELECT id, slug, title, sort FROM categories ORDER BY sort",
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(GalleryCategory(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)))
                }
            }
        }

        val where = if (active != null) "WHERE c.slug = ?" else ""
        val whereArgs = if (active != null) arrayOf(active) else null

        val total = db().rawQuery(
            "SELECT COUNT(*) FROM images i JOIN categories c ON c.id = i.category_id $where",
            whereArgs,
        ).use { cursor -> if (cursor.moveToFirst()) cursor.getInt(0) else 0 }

        val pageCount = ((total + GALLERY_PAGE_SIZE - 1) / GALLERY_PAGE_SIZE).coerceAtLeast(1)
        val safePage = page.coerceIn(1, pageCount)
        val offset = (safePage - 1) * GALLERY_PAGE_SIZE

        val args = (whereArgs ?: emptyArray()) + arrayOf(GALLERY_PAGE_SIZE.toString(), offset.toString())
        val images = db().rawQuery(
            """
            SELECT i.id, i.src, i.alt, i.width, i.height, i.sort, i.featured, c.slug, c.title
            FROM images i JOIN categories c ON c.id = i.category_id
            $where
            ORDER BY i.sort, i.id
            LIMIT ? OFFSET ?
            """.trimIndent(),
            args,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        GalleryImage(
                            id = cursor.getInt(0),
                            src = cursor.getString(1),
                            alt = cursor.getString(2),
                            width = if (cursor.isNull(3)) null else cursor.getInt(3),
                            height = if (cursor.isNull(4)) null else cursor.getInt(4),
                            sort = cursor.getInt(5),
                            featured = cursor.getInt(6) == 1,
                            categorySlug = cursor.getString(7),
                            categoryTitle = cursor.getString(8),
                        )
                    )
                }
            }
        }

        return GalleryPage(
            categories = categories,
            images = images,
            activeCategory = active ?: "all",
            page = safePage,
            pageSize = GALLERY_PAGE_SIZE,
            total = total,
            pageCount = pageCount,
        )
    }

    fun testimonials(): List<Testimonial> =
        db().rawQuery(
            """
            SELECT id, author, initials, review_date, quote, rating
            FROM testimonials
            WHERE published = 1
            ORDER BY sort, id
            """.trimIndent(),
            null,
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        Testimonial(
                            id = cursor.getInt(0),
                            author = cursor.getString(1),
                            initials = cursor.getString(2),
                            date = cursor.getString(3),
                            quote = cursor.getString(4),
                            rating = cursor.getInt(5),
                        )
                    )
                }
            }
        }

    fun eventTypes(): List<String> =
        db().rawQuery("SELECT label FROM event_types ORDER BY sort, id", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(0))
                }
            }
        }
}
