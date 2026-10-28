package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.data.repository.PlaylistsRepositoryImpl
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.impl.FavoritesInteractorImpl
import com.practicum.playlistmaker.library.domain.impl.PlaylistsInteractorImpl
import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.impl.SearchHistoryInteractorImpl
import com.practicum.playlistmaker.search.domain.impl.TracksInteractorImpl
import com.practicum.playlistmaker.settings.domain.api.SettingsInteractor
import com.practicum.playlistmaker.settings.domain.impl.SettingsInteractorImpl
import com.practicum.playlistmaker.sharing.api.SharingInteractor
import com.practicum.playlistmaker.sharing.impl.SharingInteractorImpl
import org.koin.dsl.module

val domainModule = module {
    factory<TracksInteractor> { TracksInteractorImpl(get()) }
    factory<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    factory<SettingsInteractor> { SettingsInteractorImpl(get()) }
    factory<SharingInteractor> { SharingInteractorImpl(get()) }
    single<FavoritesInteractor> { FavoritesInteractorImpl(get()) }
    single<PlaylistsInteractor> { PlaylistsInteractorImpl(get()) }
}