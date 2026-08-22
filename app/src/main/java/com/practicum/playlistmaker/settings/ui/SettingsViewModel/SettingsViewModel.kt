package com.practicum.playlistmaker.settings.ui.SettingsViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.sharing.api.SharingInteractor
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsViewModel(
    private val application: Application,
    private val settingsInteractor: SettingsInteractor,
    private val sharingInteractor: SharingInteractor
) : ViewModel() {

    private val _themeState = MutableLiveData<Boolean>()
    val themeState: LiveData<Boolean> = _themeState

    init {
        _themeState.value = settingsInteractor.isDarkThemeEnabled()
    }

    fun switchTheme(isChecked: Boolean) {
        settingsInteractor.saveThemeSettings(isChecked)
        _themeState.value = isChecked
        (application as App).switchTheme(isChecked)
    }

    fun shareApp() = sharingInteractor.shareApp()
    fun openSupport() = sharingInteractor.openSupport()
    fun openTerms() = sharingInteractor.openTerms()
}