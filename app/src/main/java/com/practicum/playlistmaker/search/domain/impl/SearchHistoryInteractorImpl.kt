package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.search.domain.models.Track

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()
    override fun addTrack(track: Track) = repository.addTrack(track)
    override fun clearHistory() = repository.clearHistory()
}