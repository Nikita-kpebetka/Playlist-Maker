package com.practicum.playlistmaker.search.domain.api

import com.practicum.playlistmaker.search.domain.models.Track


interface SearchHistoryInteractor {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clearHistory()
}