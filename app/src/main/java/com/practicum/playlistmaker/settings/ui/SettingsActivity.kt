package com.practicum.playlistmaker.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.practicum.playlistmaker.databinding.ActivitySettingsBinding
import com.practicum.playlistmaker.settings.ui.SettingsViewModel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setNavigationOnClickListener {
            finish()
        }

        viewModel.themeState.observe(this) { isDark ->
            binding.themeSwitch.setOnCheckedChangeListener(null)
            binding.themeSwitch.isChecked = isDark

            binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.switchTheme(isChecked)
            }
        }

        binding.shareApp.setOnClickListener {
            viewModel.shareApp()
        }

        binding.writeSupport.setOnClickListener {
            viewModel.openSupport()
        }

        binding.userAgreement.setOnClickListener {
            viewModel.openTerms()
        }
    }
}