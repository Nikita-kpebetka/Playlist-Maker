package com.practicum.playlistmaker.player.ui

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.practicum.playlistmaker.search.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AudioPlayerActivity : AppCompatActivity() {

    private var playerState = STATE_DEFAULT
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())

    private var previewUrl: String? = null

    private lateinit var binding: ActivityAudioPlayerBinding

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                binding.tvPlayTime.text = trackTimeFormatter.format(mediaPlayer.currentPosition)
                handler.postDelayed(this, TIMER_UPDATE_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setNavigationOnClickListener {
            stopAndReleasePlayer()
            finish()
        }

        binding.btnPlay.isEnabled = false

        val trackJson = intent.getStringExtra("TRACK_DATA_KEY")
        if (!trackJson.isNullOrEmpty()) {
            val track = Gson().fromJson(trackJson, Track::class.java)
            previewUrl = track.previewUrl
            bindTrackInfo(track)
            preparePlayer()
        }

        binding.btnPlay.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndReleasePlayer()
    }

    private fun stopAndReleasePlayer() {
        handler.removeCallbacks(updateTimerRunnable)
        mediaPlayer.release()
    }

    private fun bindTrackInfo(track: Track) {
        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvTrackTimeValue.text = trackTimeFormatter.format(track.trackTimeMillis)
        binding.tvGenreValue.text = track.primaryGenreName
        binding.tvCountryValue.text = track.country

        if (!track.collectionName.isNullOrEmpty()) {
            binding.tvCollectionValue.text = track.collectionName
            binding.lblCollectionTitle.visibility = View.VISIBLE
            binding.tvCollectionValue.visibility = View.VISIBLE
        } else {
            binding.lblCollectionTitle.visibility = View.GONE
            binding.tvCollectionValue.visibility = View.GONE
        }

        if (!track.releaseDate.isNullOrEmpty() && track.releaseDate.length >= 4) {
            binding.tvReleaseDateValue.text = track.releaseDate.substring(0, 4)
            binding.lblReleaseDateTitle.visibility = View.VISIBLE
            binding.tvReleaseDateValue.visibility = View.VISIBLE
        } else {
            binding.lblReleaseDateTitle.visibility = View.GONE
            binding.tvReleaseDateValue.visibility = View.GONE
        }

        val radiusPx = dpToPx(8f, this)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .into(binding.ivAlbumCover)
    }

    private fun preparePlayer() {
        if (previewUrl.isNullOrEmpty()) return
        mediaPlayer.reset()
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            binding.btnPlay.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(updateTimerRunnable)
            playerState = STATE_PREPARED
            binding.btnPlay.setImageResource(R.drawable.ic_play)
            binding.tvPlayTime.text = "00:00"
        }
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> pausePlayer()
            STATE_PREPARED, STATE_PAUSED -> startPlayer()
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        binding.btnPlay.setImageResource(R.drawable.ic_pause)
        playerState = STATE_PLAYING
        handler.post(updateTimerRunnable)
    }

    private fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer.pause()
            binding.btnPlay.setImageResource(R.drawable.ic_play)
            playerState = STATE_PAUSED
            handler.removeCallbacks(updateTimerRunnable)
        }
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DELAY = 300L
    }
}