package com.practicum.playlistmaker.library.data.repository

import com.practicum.playlistmaker.library.data.db.PlaylistDao
import com.practicum.playlistmaker.library.data.db.PlaylistDbConvertor
import com.practicum.playlistmaker.library.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val convertor: PlaylistDbConvertor
) : PlaylistsRepository {

    override suspend fun insertPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(convertor.map(playlist))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(convertor.map(playlist))
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { convertor.map(it) }
        }
    }

    override suspend fun addTrackToPlaylist(playlistId: Int, trackId: String): Boolean {
        val isExist = playlistDao.isTrackInPlaylist(playlistId, trackId) > 0
        if (isExist) {
            return false
        }

        playlistDao.insertTrackToPlaylist(PlaylistTrackEntity(playlistId, trackId))
        playlistDao.incrementTracksCount(playlistId)
        return true
    }
}