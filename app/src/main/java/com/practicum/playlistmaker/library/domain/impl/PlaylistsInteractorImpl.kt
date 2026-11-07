package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(
    private val repository: PlaylistsRepository
) : PlaylistsInteractor {

    override suspend fun createPlaylist(playlist: Playlist) {
        repository.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        repository.updatePlaylist(playlist)
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return repository.getAllPlaylists()
    }

    override suspend fun addTrackToPlaylist(playlistId: Int, track: Track): Boolean {
        return repository.addTrackToPlaylist(playlistId, track)
    }

    override fun getTracksForPlaylist(playlistId: Int): Flow<List<Track>> {
        return repository.getTracksForPlaylist(playlistId)
    }

    override suspend fun getPlaylistById(playlistId: Int): Playlist {
        return repository.getPlaylistById(playlistId)
    }

    override fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>> {
        return repository.getTracksByIds(trackIds)
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Int, trackId: String) {
        repository.deleteTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Int) {
        repository.deletePlaylist(playlistId)
    }
}