package com.practicum.playlistmaker.library.domain.api

import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun insertPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun addTrackToPlaylist(playlistId: Int, track: Track): Boolean
    fun getTracksForPlaylist(playlistId: Int): Flow<List<Track>>
    suspend fun getPlaylistById(playlistId: Int): Playlist
    fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>>
    suspend fun deleteTrackFromPlaylist(playlistId: Int, trackId: String)
    suspend fun deletePlaylist(playlistId: Int)
}