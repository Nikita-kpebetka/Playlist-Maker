package com.practicum.playlistmaker.di

import com.practicum.playlistmaker.library.ui.FavoritesViewModel
import com.practicum.playlistmaker.library.ui.MediaLibraryViewModel
import com.practicum.playlistmaker.library.ui.PlaylistsViewModel
import com.practicum.playlistmaker.player.ui.PlayerViewModel
import com.practicum.playlistmaker.search.ui.SearchViewModel
import com.practicum.playlistmaker.settings.ui.SettingsViewModel.SettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlayerViewModel() }
    viewModel { SettingsViewModel(androidApplication(), get(), get()) }
    viewModel { MediaLibraryViewModel() }
    viewModel { FavoritesViewModel() }
    viewModel { PlaylistsViewModel() }
}