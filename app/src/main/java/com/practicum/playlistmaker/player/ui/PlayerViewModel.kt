package com.practicum.playlistmaker.player.ui

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.practicum.playlistmaker.library.domain.api.FavoritesInteractor
import com.practicum.playlistmaker.library.domain.api.PlaylistsInteractor
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.search.domain.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PlayerViewModel(
    private val favoritesInteractor: FavoritesInteractor,
    private val mediaPlayer: MediaPlayer,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var playerState = STATE_DEFAULT
    private var timerJob: Job? = null

    private var currentProgress = "00:00"
    private var currentIsFavorite = false

    private val _playerScreenState = MutableLiveData<PlayerScreenState>(PlayerScreenState.Prepared(false))
    val playerScreenState: LiveData<PlayerScreenState> = _playerScreenState

    private val _playlistsLiveData = MutableLiveData<List<Playlist>>()
    val playlistsLiveData: LiveData<List<Playlist>> = _playlistsLiveData

    private val _showToastLiveData = MutableLiveData<String>()
    val showToastLiveData: LiveData<String> = _showToastLiveData

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    fun observePlaylists() {
        viewModelScope.launch(Dispatchers.IO) {
            playlistsInteractor.getPlaylists()
                .collect { list ->
                    _playlistsLiveData.postValue(list)
                }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist, track: Track) {
        viewModelScope.launch {
            val isAdded = playlistsInteractor.addTrackToPlaylist(playlist.id, track.trackId.toString())
            if (isAdded) {
                _showToastLiveData.postValue("Добавлено в плейлист [${playlist.name}]")
            } else {
                _showToastLiveData.postValue("Трек уже добавлен в плейлист [${playlist.name}]")
            }
        }
    }

    fun checkFavoriteStatus(trackId: Long) {
        viewModelScope.launch {
            currentIsFavorite = favoritesInteractor.isFavorite(trackId)
            updateStateByPlayerState()
        }
    }

    fun onFavoriteClicked(track: Track) {
        viewModelScope.launch {
            if (currentIsFavorite) {
                favoritesInteractor.removeTrack(track)
                currentIsFavorite = false
            } else {
                favoritesInteractor.addTrack(track)
                currentIsFavorite = true
            }
            updateStateByPlayerState()
        }
    }

    fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrEmpty()) return

        try {
            mediaPlayer.apply {
                reset()
                setDataSource(previewUrl)
                prepareAsync()
                setOnPreparedListener {
                    playerState = STATE_PREPARED
                    currentProgress = "00:00"
                    _playerScreenState.value = PlayerScreenState.Prepared(currentIsFavorite)
                }
                setOnCompletionListener {
                    stopTimer()
                    playerState = STATE_PREPARED
                    currentProgress = "00:00"
                    _playerScreenState.value = PlayerScreenState.Prepared(currentIsFavorite)
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
            try {
                mediaPlayer.pause()
                playerState = STATE_PAUSED
                stopTimer()
                currentProgress = trackTimeFormatter.format(mediaPlayer.currentPosition)
                _playerScreenState.value = PlayerScreenState.Paused(currentProgress, currentIsFavorite)
            } catch (e: Exception) {
            }
        }
    }

    private fun startPlayer() {
        try {
            mediaPlayer.start()
            playerState = STATE_PLAYING
            startTimer()
        } catch (e: Exception) {
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (playerState == STATE_PLAYING) {
                try {
                    currentProgress = trackTimeFormatter.format(mediaPlayer.currentPosition)
                    _playerScreenState.value = PlayerScreenState.Playing(currentProgress, currentIsFavorite)
                } catch (e: Exception) {
                }
                delay(TIMER_UPDATE_DELAY)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun updateStateByPlayerState() {
        _playerScreenState.value = when (playerState) {
            STATE_PLAYING -> PlayerScreenState.Playing(currentProgress, currentIsFavorite)
            STATE_PAUSED -> PlayerScreenState.Paused(currentProgress, currentIsFavorite)
            else -> PlayerScreenState.Prepared(currentIsFavorite)
        }
    }

    fun stopAndReleasePlayer() {
        stopTimer()
        try {
            mediaPlayer.apply {
                if (isPlaying) stop()
                reset()
            }
        } catch (e: Exception) {
        } finally {
            playerState = STATE_DEFAULT
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        try {
            mediaPlayer.release()
        } catch (e: Exception) {
        }
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DELAY = 300L
    }
}