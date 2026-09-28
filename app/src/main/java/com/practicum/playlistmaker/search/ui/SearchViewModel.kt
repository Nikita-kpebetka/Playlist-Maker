package com.practicum.playlistmaker.search.ui

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SearchViewModel(
    private val tracksInteractor: TracksInteractor,
    private val searchHistoryInteractor: SearchHistoryInteractor
) : ViewModel() {

    private var latestSearchText: String = ""
    private var isClickAllowed = true

    private var searchJob: Job? = null

    private val _screenState = MutableLiveData<SearchScreenState>()
    val screenState: LiveData<SearchScreenState> = _screenState

    init {
        showHistoryIfNeeded()
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) return
        this.latestSearchText = changedText

        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            performSearch(latestSearchText)
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        latestSearchText = ""
        showHistoryIfNeeded()
    }

    fun refreshSearch() {
        if (latestSearchText.isNotEmpty()) {
            performSearch(latestSearchText)
        }
    }

    fun addTrackToHistory(track: Track) {
        searchHistoryInteractor.addTrack(track)
        if (_screenState.value is SearchScreenState.History) {
            _screenState.value = SearchScreenState.History(searchHistoryInteractor.getHistory())
        }
    }

    fun clearHistory() {
        searchHistoryInteractor.clearHistory()
        _screenState.value = SearchScreenState.Empty
    }

    fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private fun showHistoryIfNeeded() {
        val history = searchHistoryInteractor.getHistory()
        if (history.isNotEmpty()) {
            _screenState.value = SearchScreenState.History(history)
        } else {
            _screenState.value = SearchScreenState.Empty
        }
    }

    private fun performSearch(query: String) {
        if (query.isEmpty()) return

        _screenState.value = SearchScreenState.Loading

        viewModelScope.launch {
            tracksInteractor.searchTracks(query)
                .catch { _ ->
                    _screenState.value = SearchScreenState.Error
                }
                .collect { foundTracks ->
                    if (foundTracks.isEmpty()) {
                        _screenState.value = SearchScreenState.NotFound
                    } else {
                        _screenState.value = SearchScreenState.Success(foundTracks)
                    }
                }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}