package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PlaylistDetailsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playlistLiveData = MutableLiveData<Playlist>()
    val playlistLiveData: LiveData<Playlist> = _playlistLiveData

    private val _tracksLiveData = MutableLiveData<List<Track>>()
    val tracksLiveData: LiveData<List<Track>> = _tracksLiveData

    fun loadPlaylistDetails(playlistId: Int) {
        viewModelScope.launch {
            val playlist = playlistsInteractor.getPlaylistById(playlistId)
            _playlistLiveData.postValue(playlist)
            playlistsInteractor.getTracksForPlaylist(playlistId)
                .flowOn(Dispatchers.IO)
                .collect { tracks ->
                    _tracksLiveData.postValue(tracks)
                }
        }
    }

    fun deleteTrack(playlistId: Int, trackId: Long) {
        viewModelScope.launch {
            playlistsInteractor.deleteTrackFromPlaylist(playlistId, trackId.toString())
            loadPlaylistDetails(playlistId)
        }
    }

    fun deletePlaylist(playlistId: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(playlistId)
            onComplete()
        }
    }
}