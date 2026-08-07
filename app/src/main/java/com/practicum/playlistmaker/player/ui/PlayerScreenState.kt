package com.practicum.playlistmaker.player.ui

sealed interface PlayerScreenState {
    object Prepared : PlayerScreenState
    data class Playing(val currentPosition: String) : PlayerScreenState
    data class Paused(val currentPosition: String) : PlayerScreenState
}