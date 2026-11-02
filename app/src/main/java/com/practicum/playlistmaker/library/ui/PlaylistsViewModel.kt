package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<PlaylistsScreenState>()
    val stateLiveData: LiveData<PlaylistsScreenState> = _stateLiveData

    fun fillData() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylists()
                .flowOn(Dispatchers.IO)
                .collect { playlists ->
                    if (playlists.isEmpty()) {
                        _stateLiveData.postValue(PlaylistsScreenState.Empty)
                    } else {
                        _stateLiveData.postValue(PlaylistsScreenState.Content(playlists))
                    }
                }
        }
    }
}