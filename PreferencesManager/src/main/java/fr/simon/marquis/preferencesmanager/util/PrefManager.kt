package fr.simon.marquis.preferencesmanager.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

object PrefManager {

    private const val THEME_PREF = "theme_pref"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    // Light: 0, Dark: 1, Auto: 2
    var themePreference: Int
        get() = prefs.getInt(THEME_PREF, 2)
        set(value) = prefs.edit { putInt(THEME_PREF, value) }
}
