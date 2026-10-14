package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var playerState = STATE_DEFAULT

    private var timerJob: Job? = null

    private val _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun checkFavoriteStatus(trackId: Long) {
        viewModelScope.launch {
            _isFavorite.value = favoritesInteractor.isFavorite(trackId)
        }
    }

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            val currentStatus = _isFavorite.value ?: false
            if (currentStatus) {
                favoritesInteractor.removeTrack(track)
                _isFavorite.value = false
            } else {
                favoritesInteractor.addTrack(track)
                _isFavorite.value = true
            }
        }
    }

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        }

        try {
            mediaPlayer?.apply {
                reset()
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    playerState = STATE_PREPARED
                    _playerScreenState.value = PlayerScreenState.Prepared
                }
                setOnCompletionListener {
                    stopTimer()
                    playerState = STATE_PREPARED
                    _playerScreenState.value = PlayerScreenState.Prepared
                }
            }
        } catch (e: Exception) {
            playerState = STATE_DEFAULT
        }
    }

    fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer?.pause()
            playerState = STATE_PAUSED
            stopTimer()
            mediaPlayer?.let {
                _playerScreenState.value = PlayerScreenState.Paused(
                    trackTimeFormatter.format(it.currentPosition)
                )
            }
        }
    }

    private fun startPlayer() {
        mediaPlayer?.start()
        playerState = STATE_PLAYING
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (playerState == STATE_PLAYING) {
                mediaPlayer?.let {
                    _playerScreenState.value = PlayerScreenState.Playing(
                        trackTimeFormatter.format(it.currentPosition)
                    )
                }
                delay(TIMER_UPDATE_DELAY)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun stopAndReleasePlayer() {
        stopTimer()
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        playerState = STATE_DEFAULT
    }

    override fun onCleared() {
        super.onCleared()
        stopAndReleasePlayer()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DELAY = 300L
    }
}