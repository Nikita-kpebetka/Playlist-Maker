package com.practicum.playlistmaker.settings.domain.api

interface SettingsRepository {
    fun isDarkThemeEnabled(): Boolean
    fun saveThemeSettings(isEnabled: Boolean)
}