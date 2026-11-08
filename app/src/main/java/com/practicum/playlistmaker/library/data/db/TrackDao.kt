package com.practicum.playlistmaker.library.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks_table WHERE isFavoriteTrack = 1 ORDER BY createdAt DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_tracks_table WHERE trackId = :id AND isFavoriteTrack = 1)")
    suspend fun isFavorite(id: Long): Boolean

    @Query("SELECT trackId FROM favorite_tracks_table WHERE isFavoriteTrack = 1")
    suspend fun getFavoriteTrackIds(): List<Long>
}