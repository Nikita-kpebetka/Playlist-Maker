package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.library.data.db.TrackDao
import com.practicum.playlistmaker.search.data.network.iTunesApi
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TracksRepositoryImpl(
    private val iTunesService: iTunesApi,
    private val trackDao: TrackDao
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<List<Track>> = flow {
        try {
            val response = iTunesService.search(expression)
            val favoriteIds = trackDao.getFavoriteTrackIds().map { it.toString() }

            val domainTracks = response.results.map { dto ->
                Track(
                    trackId = dto.trackId,
                    trackName = dto.trackName,
                    artistName = dto.artistName,
                    trackTimeMillis = dto.trackTimeMillis,
                    artworkUrl100 = dto.artworkUrl100,
                    collectionName = dto.collectionName,
                    releaseDate = dto.releaseDate,
                    primaryGenreName = dto.primaryGenreName,
                    country = dto.country,
                    previewUrl = dto.previewUrl,
                    isFavorite = favoriteIds.contains(dto.trackId.toString())
                )
            }

            emit(domainTracks)
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}