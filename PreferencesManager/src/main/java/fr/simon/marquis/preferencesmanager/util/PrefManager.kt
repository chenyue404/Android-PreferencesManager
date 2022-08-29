package fr.simon.marquis.preferencesmanager.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import fr.simon.marquis.preferencesmanager.ui.preferences.EPreferencesSort

object PrefManager {

    private const val THEME_PREF = "theme_pref"
    private const val PREF_SHOW_SYSTEM_APPS = "SHOW_SYSTEM_APPS"
    private const val KEY_SORT_TYPE = "KEY_SORT_TYPE"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Light: 0, Dark: 1, Auto: 2
    var themePreference: Int
        get() = prefs.getInt(THEME_PREF, 2)
        set(value) = prefs.edit { putInt(THEME_PREF, value) }

    var showSystemApps: Boolean
        get() = prefs.getBoolean(PREF_SHOW_SYSTEM_APPS, false)
        set(value) = prefs.edit { putBoolean(PREF_SHOW_SYSTEM_APPS, value) }

    var keySortType: Int
        get() = prefs.getInt(KEY_SORT_TYPE, EPreferencesSort.ALPHANUMERIC.ordinal)
        set(value) = prefs.edit { putInt(KEY_SORT_TYPE, value) }
}
