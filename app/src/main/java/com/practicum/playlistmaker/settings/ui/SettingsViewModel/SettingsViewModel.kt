package com.practicum.playlistmaker.settings.ui.SettingsViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.sharing.api.SharingInteractor

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsInteractor: SettingsInteractor = Creator.provideSettingsInteractor(application)
    private val sharingInteractor: SharingInteractor = Creator.provideSharingInteractor(application)

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        _themeState.value = settingsInteractor.isDarkThemeEnabled()
    }

    fun switchTheme(isChecked: Boolean) {
        settingsInteractor.saveThemeSettings(isChecked)
        _themeState.value = isChecked
        (getApplication<Application>() as App).switchTheme(isChecked)
    }

    fun shareApp() = sharingInteractor.shareApp()
    fun openSupport() = sharingInteractor.openSupport()
    fun openTerms() = sharingInteractor.openTerms()
}