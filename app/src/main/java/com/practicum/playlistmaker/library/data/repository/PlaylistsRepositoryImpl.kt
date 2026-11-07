package com.practicum.playlistmaker.library.data.repository

import com.practicum.playlistmaker.library.data.db.PlaylistDao
import com.practicum.playlistmaker.library.data.db.PlaylistDbConvertor
import com.practicum.playlistmaker.library.data.db.PlaylistTrackEntity
import com.practicum.playlistmaker.library.data.db.TrackDbConvertor
import com.practicum.playlistmaker.library.domain.api.PlaylistsRepository
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
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

    override suspend fun addTrackToPlaylist(playlistId: Int, track: Track): Boolean {
        val trackIdStr = track.trackId.toString()
        val isExist = playlistDao.isTrackInPlaylist(playlistId, trackIdStr) > 0
        if (isExist) {
            return false
        }
        val trackConvertor = TrackDbConvertor()
        playlistDao.insertTrackData(trackConvertor.map(track, isFavorite = false))
        playlistDao.insertTrackToPlaylist(PlaylistTrackEntity(playlistId, trackIdStr))
        playlistDao.incrementTracksCount(playlistId)
        return true
    }

    override fun getTracksForPlaylist(playlistId: Int): Flow<List<Track>> {
        val trackConvertor = TrackDbConvertor()
        return playlistDao.getTracksForPlaylist(playlistId).map { entities ->
            entities.map { trackConvertor.map(it) }
        }
    }

    override suspend fun getPlaylistById(playlistId: Int): Playlist {
        return convertor.map(playlistDao.getPlaylistById(playlistId))
    }

    override fun getTracksByIds(trackIds: List<Long>): Flow<List<Track>> {
        val trackConvertor = TrackDbConvertor()
        return playlistDao.getTracksByIds(trackIds).map { entities ->
            entities.map { trackConvertor.map(it) }
        }
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Int, trackId: String) {
        playlistDao.deleteTrackFromPlaylist(playlistId, trackId)
        playlistDao.decrementTracksCount(playlistId)
    }

    override suspend fun deletePlaylist(playlistId: Int) {
        playlistDao.deletePlaylistById(playlistId)
        playlistDao.deletePlaylistRelations(playlistId)
    }
}