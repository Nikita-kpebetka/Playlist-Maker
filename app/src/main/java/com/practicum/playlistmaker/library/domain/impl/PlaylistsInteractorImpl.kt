package com.practicum.playlistmaker.library.domain.impl

import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
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

    override suspend fun addTrackToPlaylist(playlistId: Int, trackId: String): Boolean {
        return repository.addTrackToPlaylist(playlistId, trackId)
    }
}