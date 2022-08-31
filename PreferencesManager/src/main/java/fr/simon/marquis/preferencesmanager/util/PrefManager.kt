package fr.simon.marquis.preferencesmanager.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.simon.marquis.preferencesmanager.model.EPreferencesSort
import fr.simon.marquis.preferencesmanager.ui.editor.EFontSize
import fr.simon.marquis.preferencesmanager.ui.editor.EFontTheme

object PrefManager {

    private const val KEY_COLOR_THEME = "KEY_COLOR_THEME"
    private const val KEY_FONT_SIZE = "KEY_FONT_SIZE"
    private const val KEY_SORT_TYPE = "KEY_SORT_TYPE"
    private const val PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS"
    private const val APP_THEME_PREF = "theme_pref"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Light: 0, Dark: 1, Auto: 2
    var themePreference: Int
        get() = prefs.getInt(APP_THEME_PREF, 2)
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
        get() = prefs.getInt(KEY_FONT_SIZE, EFontSize.MEDIUM.ordinal)
        set(value) = prefs.edit { putInt(KEY_FONT_SIZE, value) }
}
