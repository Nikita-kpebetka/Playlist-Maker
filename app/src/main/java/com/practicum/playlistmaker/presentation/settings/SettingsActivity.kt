package com.practicum.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.di.Creator
import com.practicum.playlistmaker.presentation.App

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<MaterialToolbar>(R.id.backButton)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val themeSwitch = findViewById<SwitchMaterial>(R.id.themeSwitch)

        val settingsInteractor = Creator.provideSettingsInteractor(this)

        themeSwitch.isChecked = settingsInteractor.isDarkThemeEnabled()

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsInteractor.saveThemeSettings(isChecked)

            (applicationContext as App).switchTheme(isChecked)
        }

        val shareAppButton = findViewById<FrameLayout>(R.id.shareApp)
        shareAppButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.Share_click))
            startActivity(Intent.createChooser(shareIntent, getString(R.string.Share_app)))
        }

        val writeSupportButton = findViewById<FrameLayout>(R.id.writeSupport)
        writeSupportButton.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.My_email)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.topic))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.message))
            }
            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
            }
        }

        val userAgreementButton = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreementButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = android.net.Uri.parse(getString(R.string.link_to_the_user_agreement_))
            startActivity(browserIntent)
        }
    }
}