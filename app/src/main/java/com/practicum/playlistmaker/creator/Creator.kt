package com.practicum.playlistmaker.creator

import android.content.Context
import com.practicum.playlistmaker.search.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.search.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.settings.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.sharing.api.SharingInteractor
import com.practicum.playlistmaker.sharing.impl.SharingInteractorImpl

object Creator {

    private const val PREFS_NAME = "playlist_maker_prefs"

    fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(TracksRepositoryImpl())
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return SearchHistoryInteractorImpl(SearchHistoryRepositoryImpl(sharedPrefs))
    }

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return SettingsInteractorImpl(SettingsRepositoryImpl(sharedPrefs))
    }

    fun provideSharingInteractor(context: Context): SharingInteractor {
        return SharingInteractorImpl(context.applicationContext)
    }
}