/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.ui

import android.app.Application
import android.util.Log
import androidx.preference.PreferenceManager

import fr.simon.marquis.preferencesmanager.model.AppTheme

//TODO floating dialog inputs need work
//TODO in light theme, menu options colors are wrong
//TODO restore dialog
//TODO in light theme, editing a pref crashes

class App : Application() {

    override fun onCreate() {
        initTheme()
        setTheme(App.theme.theme)
        super.onCreate()
    }

    private fun initTheme() {
        App.theme = try {
            AppTheme.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(AppTheme.APP_THEME_KEY, AppTheme.DEFAULT_THEME.name)!!)
        } catch (iae: IllegalArgumentException) {
            Log.d(App::class.java.simpleName, "No theme specified, using the default one")
            AppTheme.DEFAULT_THEME
        }

    }

    fun switchTheme() {
        App.theme = if (App.theme == AppTheme.DARK) AppTheme.LIGHT else AppTheme.DARK
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(AppTheme.APP_THEME_KEY, App.theme.name).apply()
    }


    companion object {
        var theme = AppTheme.DEFAULT_THEME
    }

}
