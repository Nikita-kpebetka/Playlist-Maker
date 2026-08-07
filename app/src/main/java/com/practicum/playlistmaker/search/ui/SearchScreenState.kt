package com.practicum.playlistmaker.search.ui

import com.practicum.playlistmaker.search.domain.models.Track

sealed interface SearchScreenState {
    object Loading : SearchScreenState
    object Error : SearchScreenState
    object NotFound : SearchScreenState
    object Empty : SearchScreenState
    data class Success(val tracks: List<Track>) : SearchScreenState
    data class History(val tracks: List<Track>) : SearchScreenState
}