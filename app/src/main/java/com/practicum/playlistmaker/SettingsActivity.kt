package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val toolbar = findViewById<MaterialToolbar>(R.id.backButton)
        toolbar.setNavigationOnClickListener {
            finish()
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
            val emailIntent = Intent(Intent.ACTION_SEND)
            emailIntent.type = "message/rfc822"
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.My_email)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.topic))
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.message))

            startActivity(emailIntent)
        }

        val userAgreementButton = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreementButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = android.net.Uri.parse(getString(R.string.link_to_the_user_agreement_))
            startActivity(browserIntent)
        }
    }
}