package com.practicum.playlistmaker.library.ui

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface FavoritesScreenState {
    object Empty : FavoritesScreenState
    data class Content(val tracks: List<Track>) : FavoritesScreenState
}