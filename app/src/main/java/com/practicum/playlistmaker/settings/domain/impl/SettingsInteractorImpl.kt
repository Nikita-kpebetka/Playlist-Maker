package com.practicum.playlistmaker.settings.domain.impl

import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository

class SettingsInteractorImpl (private val repository: SettingsRepository) : SettingsInteractor {

    override fun isDarkThemeEnabled(): Boolean = repository.isDarkThemeEnabled()

    override fun saveThemeSettings(isEnabled: Boolean) = repository.saveThemeSettings(isEnabled)
}