package com.practicum.playlistmaker.library.domain.models

data class Playlist(
    val id: Int = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val tracksCount: Int = 0
)