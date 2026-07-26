package com.practicum.playlistmaker.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.App
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val settingsInteractor = Creator.provideSettingsInteractor(this)
        val sharingInteractor = Creator.provideSharingInteractor(this)

        binding.backButton.setNavigationOnClickListener {
            finish()
        }

        binding.themeSwitch.isChecked = settingsInteractor.isDarkThemeEnabled()
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsInteractor.saveThemeSettings(isChecked)
            (applicationContext as App).switchTheme(isChecked)
        }

        binding.shareApp.setOnClickListener {
            sharingInteractor.shareApp()
        }

        binding.writeSupport.setOnClickListener {
            sharingInteractor.openSupport()
        }

        binding.userAgreement.setOnClickListener {
            sharingInteractor.openTerms()
        }
    }
}