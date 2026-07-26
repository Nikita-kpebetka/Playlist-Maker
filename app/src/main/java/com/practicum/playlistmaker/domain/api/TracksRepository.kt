package com.practicum.playlistmaker.domain.api

import com.practicum.playlistmaker.domain.models.Track

interface TracksRepository {
    fun searchTracks(expression: String, onSuccess: (List<Track>) -> Unit, onError: () -> Unit)
}