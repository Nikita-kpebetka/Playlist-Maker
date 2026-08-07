package com.practicum.playlistmaker.settings.domain.api

interface SettingsInteractor {
    fun isDarkThemeEnabled(): Boolean
    fun saveThemeSettings(isEnabled: Boolean)
}