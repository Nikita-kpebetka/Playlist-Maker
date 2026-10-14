package com.practicum.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private val _state = MutableLiveData<FavoritesScreenState>()
    val state: LiveData<FavoritesScreenState> = _state

    init {
        loadFavoriteTracks()
    }

    private fun loadFavoriteTracks() {
        viewModelScope.launch {
            favoritesInteractor.getFavoriteTracks()
                .collect { tracks ->
                    if (tracks.isEmpty()) {
                        _state.postValue(FavoritesScreenState.Empty)
                    } else {
                        _state.postValue(FavoritesScreenState.Content(tracks))
                    }
                }
        }
    }
}