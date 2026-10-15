package com.practicum.playlistmaker.library.data.repository

import com.practicum.playlistmaker.library.data.db.AppDatabase
import com.practicum.playlistmaker.library.data.db.TrackDbConvertor
import com.practicum.playlistmaker.library.domain.api.FavoritesRepository
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val convertor: TrackDbConvertor
) : FavoritesRepository {

    override suspend fun addTrack(track: Track) {
        appDatabase.trackDao().insertTrack(convertor.map(track))
    }

    override suspend fun removeTrack(track: Track) {
        appDatabase.trackDao().deleteTrack(convertor.map(track))
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return appDatabase.trackDao().getFavoriteTracks().map { entities ->
            entities.map { convertor.map(it) }
        }
    }

    override suspend fun isFavorite(trackId: Long): Boolean {
        return appDatabase.trackDao().isFavorite(trackId)
    }
}