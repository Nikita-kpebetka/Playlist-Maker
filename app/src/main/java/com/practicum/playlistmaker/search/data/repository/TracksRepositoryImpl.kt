package com.practicum.playlistmaker.search.data.repository

import com.practicum.playlistmaker.search.data.dto.SearchResponse
import com.practicum.playlistmaker.search.data.network.RetrofitClient
import com.practicum.playlistmaker.search.data.network.iTunesApi
import com.practicum.playlistmaker.search.domain.api.TracksRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl(
    private val iTunesService: iTunesApi
) : TracksRepository {

    override fun searchTracks(expression: String): Flow<List<Track>> = flow {
        try {
            val response = iTunesService.search(expression)
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
                    previewUrl = dto.previewUrl
                )
            }
            emit(domainTracks)
        } catch (e: Exception) {
            throw e
        }
    }.flowOn(Dispatchers.IO)
}