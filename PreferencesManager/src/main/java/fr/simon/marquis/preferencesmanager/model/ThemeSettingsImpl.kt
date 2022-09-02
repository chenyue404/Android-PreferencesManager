package fr.simon.marquis.preferencesmanager.model

import fr.simon.marquis.preferencesmanager.util.PrefManager
import javax.inject.Inject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.MutableStateFlow

class ThemeSettingsImpl @Inject constructor() : ThemeSettings {

    override val themeStream: MutableStateFlow<EAppTheme>
    override var theme: EAppTheme by ThemePreferenceDelegate(PrefManager.themePreference)

    init {
        themeStream = MutableStateFlow(theme)
    }

    inner class ThemePreferenceDelegate(
        private val theme: Int
    ) : ReadWriteProperty<Any?, EAppTheme> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): EAppTheme =
            EAppTheme.getAppTheme(theme)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: EAppTheme) {
            themeStream.value = value
            PrefManager.themePreference = value.ordinal
        }
    }
}
