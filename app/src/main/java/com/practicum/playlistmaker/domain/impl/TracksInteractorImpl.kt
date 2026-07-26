package com.practicum.playlistmaker.domain.impl

import com.practicum.playlistmaker.domain.api.TracksRepository

class TracksInteractorImpl(private val repository: TracksRepository) : TracksInteractor {

    override fun searchTracks(expression: String, consumer: TracksInteractor.TracksConsumer) {
        repository.searchTracks(expression,
            onSuccess = { tracks -> consumer.consume(tracks, false) },
            onError = { consumer.consume(null, true) }
        )
    }
}