package com.practicum.playlistmaker.search.domain.impl

import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.api.TracksRepository

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        repository.searchTracks(expression,
            onSuccess = { tracks -> consumer.consume(tracks, false) },
            onError = { consumer.consume(null, true) }
        )
    }
}