package fr.simon.marquis.preferencesmanager.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.simon.marquis.preferencesmanager.model.EFontSize
import fr.simon.marquis.preferencesmanager.model.EFontTheme
import fr.simon.marquis.preferencesmanager.model.EPreferencesSort

object PrefManager {

    private const val APP_THEME_PREF = "theme_pref"
    private const val FAVORITES_KEY = "FAVORITES_KEY"
    private const val KEY_COLOR_THEME = "KEY_COLOR_THEME"
    private const val KEY_FONT_SIZE = "KEY_FONT_SIZE"
    private const val KEY_SORT_TYPE = "KEY_SORT_TYPE"
    private const val PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun clearFavorites() {
        prefs.edit().remove(FAVORITES_KEY).apply()
    }

    // Auto: 0, Day: 1, Night: 2
    var themePreference: Int
        get() = prefs.getInt(APP_THEME_PREF, 0)
        set(value) = prefs.edit { putInt(APP_THEME_PREF, value) }

    var showSystemApps: Boolean
        get() = prefs.getBoolean(PREF_SHOW_SYSTEM_APPS, false)
        set(value) = prefs.edit { putBoolean(PREF_SHOW_SYSTEM_APPS, value) }

    var keySortType: Int
        get() = prefs.getInt(KEY_SORT_TYPE, EPreferencesSort.ALPHANUMERIC.ordinal)
        set(value) = prefs.edit { putInt(KEY_SORT_TYPE, value) }

    var keyFontTheme: Int
        get() = prefs.getInt(KEY_COLOR_THEME, EFontTheme.ECLIPSE.ordinal)
        set(value) = prefs.edit { putInt(KEY_COLOR_THEME, value) }

    var keyFontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, EFontSize.MEDIUM.size)
        set(value) = prefs.edit { putInt(KEY_FONT_SIZE, value) }

    var favorites: String?
        get() = prefs.getString(FAVORITES_KEY, "[]")
        set(value) = prefs.edit { putString(FAVORITES_KEY, value) }
}
