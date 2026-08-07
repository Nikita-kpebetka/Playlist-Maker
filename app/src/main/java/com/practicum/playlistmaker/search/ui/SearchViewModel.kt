package com.practicum.playlistmaker.search.ui

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val tracksInteractor: TracksInteractor = Creator.provideTracksInteractor()
    private val searchHistoryInteractor: SearchHistoryInteractor = Creator.provideSearchHistoryInteractor(application)

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { performSearch(latestSearchText) }

    private var latestSearchText: String = ""
    private var isClickAllowed = true

    private val _screenState = MutableLiveData<SearchScreenState>()
    val screenState: LiveData<SearchScreenState> = _screenState

    init {
        showHistoryIfNeeded()
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(searchRunnable)
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) return
        this.latestSearchText = changedText

        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    fun clearSearch() {
        handler.removeCallbacks(searchRunnable)
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
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
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

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, hasError: Boolean) {
                handler.post {
                    if (hasError) {
                        _screenState.value = SearchScreenState.Error
                    } else if (foundTracks.isNullOrEmpty()) {
                        _screenState.value = SearchScreenState.NotFound
                    } else {
                        _screenState.value = SearchScreenState.Success(foundTracks)
                    }
                }
            }
        })
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}