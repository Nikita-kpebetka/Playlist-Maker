package com.practicum.playlistmaker.library.ui

import com.practicum.playlistmaker.library.domain.models.Playlist

sealed interface PlaylistsScreenState {
    object Empty : PlaylistsScreenState
    data class Content(val playlists: List<Playlist>) : PlaylistsScreenState
}