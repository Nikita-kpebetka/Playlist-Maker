package com.practicum.playlistmaker.di

import android.content.Context
import com.practicum.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.practicum.playlistmaker.data.repository.SettingsRepositoryImpl
import com.practicum.playlistmaker.data.repository.TracksRepositoryImpl
import com.practicum.playlistmaker.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.domain.api.SettingsInteractor
import com.practicum.playlistmaker.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.domain.impl.TracksInteractor
import com.practicum.playlistmaker.domain.impl.TracksInteractorImpl

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
}