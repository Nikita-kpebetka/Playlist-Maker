package com.practicum.playlistmaker.player.ui

import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerViewModel : ViewModel() {

    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private var playerState = STATE_DEFAULT

    private val _playerScreenState = MutableLiveData<PlayerScreenState>()
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                _playerScreenState.value = PlayerScreenState.Playing(
                    trackTimeFormatter.format(mediaPlayer.currentPosition)
                )
                handler.postDelayed(this, TIMER_UPDATE_DELAY)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAndReleasePlayer()
    }

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty() || playerState != STATE_DEFAULT) return

        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            playerState = STATE_PREPARED
            _playerScreenState.value = PlayerScreenState.Prepared
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(updateTimerRunnable)
            playerState = STATE_PREPARED
            _playerScreenState.value = PlayerScreenState.Paused(PLAY_TIME_START)
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
            mediaPlayer.pause()
            playerState = STATE_PAUSED
            handler.removeCallbacks(updateTimerRunnable)
            _playerScreenState.value = PlayerScreenState.Paused(
                trackTimeFormatter.format(mediaPlayer.currentPosition)
            )
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        playerState = STATE_PLAYING
        handler.post(updateTimerRunnable)
    }

    private fun stopAndReleasePlayer() {
        handler.removeCallbacks(updateTimerRunnable)
        mediaPlayer.release()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DELAY = 300L
        private const val PLAY_TIME_START = "00:00"
    }
}