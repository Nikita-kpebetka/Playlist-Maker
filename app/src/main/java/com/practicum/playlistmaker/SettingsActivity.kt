package com.practicum.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener {
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
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = android.net.Uri.parse("mailto:${getString(R.string.My_email)}")
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                "Сообщение разработчикам и разработчицам приложения Playlist Maker"
            )
            emailIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Спасибо разработчикам и разработчицам за крутое приложение!"
            )
            startActivity(emailIntent)
        }

        val userAgreementButton = findViewById<FrameLayout>(R.id.userAgreement)
        userAgreementButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = android.net.Uri.parse("https://yandex.ru/legal/practicum_offer/ru/")
            startActivity(browserIntent)
        }
    }
}