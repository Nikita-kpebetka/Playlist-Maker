package com.practicum.playlistmaker.sharing.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.sharing.api.SharingInteractor

class SharingInteractorImpl (private val context: Context) : SharingInteractor {

    override fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.Share_click))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.Share_app)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun openTerms() {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.link_to_the_user_agreement_))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
    }

    override fun openSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.My_email)))
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.topic))
            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.message))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
        }
    }
}