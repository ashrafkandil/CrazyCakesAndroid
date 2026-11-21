package com.marwacrazycakes.app.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Theme(
    val id: Int,
    val name: String,
    val primaryColor: String,
    val primaryForeground: String,
    val secondaryColor: String,
    val secondaryForeground: String,
    val background: String,
    val surface: String,
    val muted: String,
    val mutedForeground: String,
    val border: String,
    val gold: String,
    val overlay: String,
    val blush: String,
    val isDefault: Boolean,
    val sort: Int,
) {
    fun toColorScheme() = androidx.compose.material3.lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(this.primaryColor),
        onPrimary = androidx.compose.ui.graphics.Color(this.primaryForeground),
        secondary = androidx.compose.ui.graphics.Color(this.secondaryColor),
        onSecondary = androidx.compose.ui.graphics.Color(this.secondaryForeground),
        background = androidx.compose.ui.graphics.Color(this.background),
        onBackground = androidx.compose.ui.graphics.Color(this.secondaryForeground), // Using secondaryForeground as onBackground
        surface = androidx.compose.ui.graphics.Color(this.surface),
        onSurface = androidx.compose.ui.graphics.Color(this.secondaryForeground),
        surfaceVariant = androidx.compose.ui.graphics.Color(this.muted),
        onSurfaceVariant = androidx.compose.ui.graphics.Color(this.mutedForeground),
        outline = androidx.compose.ui.graphics.Color(this.border),
        outlineVariant = androidx.compose.ui.graphics.Color(this.border),
    )
}

object ThemeRepository {

    private const val TAG = "ThemeRepository"

    @Volatile private var cachedThemes: List<Theme>? = null
    @Volatile private var defaultTheme: Theme? = null
    private val cacheMutex = kotlinx.coroutines.sync.Mutex()

    suspend fun init(context: Context) = withContext(Dispatchers.IO) {
        loadThemesFromDatabase(context)
    }

    private fun loadThemesFromDatabase(context: Context) {
        val dbPath = context.getDatabasePath("crazy-cakes.db").absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

        try {
            val cursor = db.rawQuery(
                "SELECT id, name, primary_color, primary_foreground, secondary_color, secondary_foreground, " +
                    "background, surface, muted, muted_foreground, border, gold, overlay, blush, is_default, sort " +
                    "FROM themes ORDER BY sort, id",
                null
            )

            val themes = mutableListOf<Theme>()
            var default: Theme? = null

            while (cursor.moveToNext()) {
                val theme = Theme(
                    id = cursor.getInt(0),
                    name = cursor.getString(1),
                    primaryColor = cursor.getString(2),
                    primaryForeground = cursor.getString(3),
                    secondaryColor = cursor.getString(4),
                    secondaryForeground = cursor.getString(5),
                    background = cursor.getString(6),
                    surface = cursor.getString(7),
                    muted = cursor.getString(8),
                    mutedForeground = cursor.getString(9),
                    border = cursor.getString(10),
                    gold = cursor.getString(11),
                    overlay = cursor.getString(11), // Using gold as overlay fallback
                    blush = cursor.getString(13),
                    isDefault = cursor.getInt(14) == 1,
                    sort = cursor.getInt(15)
                )
                themes.add(theme)
                if (theme.isDefault) default = theme
            }
            cursor.close()

            cachedThemes = themes
            defaultTheme = default ?: themes.firstOrNull()
        } finally {
            // Don't close - database is managed elsewhere
        }
    }

    suspend fun getThemes(): List<Theme> = withContext(Dispatchers.IO) {
        cacheMutex.withLock {
            if (cachedThemes == null) {
                val context = com.marwacrazycakes.app.MainActivity.getAppContext()
                loadThemesFromDatabase(context)
            }
            cachedThemes ?: emptyList()
        }
    }

    suspend fun getDefaultTheme(): Theme? = withContext(Dispatchers.IO) {
        if (defaultTheme == null) {
            val context = com.marwacrazycakes.app.MainActivity.getAppContext()
            loadThemesFromDatabase(context)
        }
        defaultTheme
    }

    suspend fun setDefaultTheme(themeId: Int): Boolean = withContext(Dispatchers.IO) {
        val dbPath = com.marwacrazycakes.app.MainActivity.getAppContext().getDatabasePath("crazy-cakes.db").absolutePath
        val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE)

        try {
            db.execSQL("UPDATE themes SET is_default = 0")
            val result = db.execSQL("UPDATE themes SET is_default = 1 WHERE id = ?", arrayOf(themeId.toString()))
            if (result > 0) {
                // Refresh cache
                val context = com.marwacrazycakes.app.MainActivity.getAppContext()
                loadThemesFromDatabase(context)
                return true
            }
            return false
        } finally {
            // Don't close
        }
    }
}