package com.practicum.playlistmaker.data.repository

import com.practicum.playlistmaker.data.dto.SearchResponse
import com.practicum.playlistmaker.data.network.RetrofitClient
import com.practicum.playlistmaker.domain.api.TracksRepository
import com.practicum.playlistmaker.domain.models.Track
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TracksRepositoryImpl : TracksRepository {
    override fun searchTracks(expression: String, onSuccess: (List<Track>) -> Unit, onError: () -> Unit) {
        RetrofitClient.api.search(expression).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val dtos = response.body()?.results ?: emptyList()
                    val domainTracks = dtos.map { dto ->
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
                    onSuccess(domainTracks)
                } else {
                    onError()
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                onError()
            }
        })
    }
}