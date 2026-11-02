package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.Playlist
import kotlinx.coroutines.launch

class NewPlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _isCreateButtonEnabled = MutableLiveData<Boolean>(false)
    val isCreateButtonEnabled: LiveData<Boolean> = _isCreateButtonEnabled

    fun onNameChanged(text: String?) {
        _isCreateButtonEnabled.value = !text.isNullOrBlank()
    }

    fun createPlaylist(name: String, description: String?, coverPath: String?) {
        viewModelScope.launch {
            val newPlaylist = Playlist(
                name = name,
                description = description,
                coverPath = coverPath,
                tracksCount = 0
            )
            playlistsInteractor.createPlaylist(newPlaylist)
        }
    }
}