package fr.simon.marquis.preferencesmanager.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.simon.marquis.preferencesmanager.model.ThemeSettings
import fr.simon.marquis.preferencesmanager.model.ThemeSettingsImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ThemeModule {

    @Binds
    @Singleton
    abstract fun bindUserSettings(
        userSettingsImpl: ThemeSettingsImpl
    ): ThemeSettings
}
