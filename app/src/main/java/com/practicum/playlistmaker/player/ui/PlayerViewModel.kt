package com.practicum.playlistmaker.player.ui

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerViewModel : ViewModel() {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var playerState = STATE_DEFAULT

    private var timerJob: Job? = null

    private val _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
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