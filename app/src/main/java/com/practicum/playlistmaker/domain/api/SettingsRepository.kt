package com.practicum.playlistmaker.domain.api

interface SettingsRepository {
    fun isDarkThemeEnabled(): Boolean
    fun saveThemeSettings(isEnabled: Boolean)
}