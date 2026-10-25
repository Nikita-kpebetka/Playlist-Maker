package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.library.domain.api.FavoritesRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val repository: FavoritesRepository
) : FavoritesInteractor {
    override suspend fun addTrack(track: Track) = repository.addTrack(track)
    override suspend fun removeTrack(track: Track) = repository.removeTrack(track)
    override fun getFavoriteTracks(): Flow<List<Track>> = repository.getFavoriteTracks()
    override suspend fun isFavorite(trackId: Long): Boolean = repository.isFavorite(trackId)
}