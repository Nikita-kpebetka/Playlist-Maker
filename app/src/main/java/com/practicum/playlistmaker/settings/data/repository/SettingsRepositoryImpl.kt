package com.practicum.playlistmaker.settings.data.repository

import android.content.SharedPreferences
import com.practicum.playlistmaker.settings.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    private val themeKey = "DARK_THEME_KEY"

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(themeKey, false)
    }

    override fun saveThemeSettings(isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(themeKey, isEnabled).apply()
    }
}