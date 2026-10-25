package com.practicum.playlistmaker.player.ui

sealed interface PlayerScreenState {
    data class Prepared(val isFavorite: Boolean) : PlayerScreenState
    data class Playing(val progress: String, val isFavorite: Boolean) : PlayerScreenState
    data class Paused(val progress: String, val isFavorite: Boolean) : PlayerScreenState
}