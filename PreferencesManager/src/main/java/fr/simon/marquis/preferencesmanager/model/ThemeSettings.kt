package fr.simon.marquis.preferencesmanager.model

import kotlinx.coroutines.flow.StateFlow

interface ThemeSettings {
    val themeStream: StateFlow<EAppTheme>
    var theme: EAppTheme
}
