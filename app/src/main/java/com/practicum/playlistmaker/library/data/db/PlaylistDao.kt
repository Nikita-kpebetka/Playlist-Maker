package com.practicum.playlistmaker.library.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist_table ORDER BY id DESC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackToPlaylist(relation: PlaylistTrackEntity)

    @Query("SELECT COUNT(*) FROM playlist_track_table WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun isTrackInPlaylist(playlistId: Int, trackId: String): Int

    @Query("UPDATE playlist_table SET tracksCount = tracksCount + 1 WHERE id = :playlistId")
    suspend fun incrementTracksCount(playlistId: Int)

    @Query("""
        SELECT favorite_tracks_table.* FROM favorite_tracks_table 
        INNER JOIN playlist_track_table ON favorite_tracks_table.trackId = playlist_track_table.trackId 
        WHERE playlist_track_table.playlistId = :playlistId
        ORDER BY favorite_tracks_table.createdAt DESC
    """)
    fun getTracksForPlaylist(playlistId: Int): Flow<List<TrackEntity>>

    @Query("DELETE FROM playlist_track_table WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deleteTrackFromPlaylist(playlistId: Int, trackId: String)

    @Query("UPDATE playlist_table SET tracksCount = tracksCount - 1 WHERE id = :playlistId")
    suspend fun decrementTracksCount(playlistId: Int)

    @Query("SELECT * FROM playlist_table WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Int): PlaylistEntity

    @Query("SELECT * FROM favorite_tracks_table WHERE trackId IN (:trackIds)")
    fun getTracksByIds(trackIds: List<Long>): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackData(track: TrackEntity)

    @Query("DELETE FROM playlist_table WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: Int)

    @Query("DELETE FROM playlist_track_table WHERE playlistId = :playlistId")
    suspend fun deletePlaylistRelations(playlistId: Int)
}