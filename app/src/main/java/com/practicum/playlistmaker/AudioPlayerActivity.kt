package com.practicum.playlistmaker

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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AudioPlayerActivity : AppCompatActivity() {

    companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TIMER_UPDATE_DELAY = 300L
    }

    private var playerState = STATE_DEFAULT
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var btnPlay: ImageView
    private lateinit var tvPlayTime: TextView
    private var previewUrl: String? = null

    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (playerState == STATE_PLAYING) {
                tvPlayTime.text = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                handler.postDelayed(this, TIMER_UPDATE_DELAY)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_player)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.backButton)
        toolbar.setNavigationOnClickListener {
            stopAndReleasePlayer()
            finish()
        }

        btnPlay = findViewById(R.id.btnPlay)
        tvPlayTime = findViewById(R.id.tvPlayTime)

        btnPlay.isEnabled = false

        val trackJson = intent.getStringExtra("TRACK_DATA_KEY")
        if (!trackJson.isNullOrEmpty()) {
            val track = Gson().fromJson(trackJson, Track::class.java)
            previewUrl = track.previewUrl
            bindTrackInfo(track)
            preparePlayer()
        }

        btnPlay.setOnClickListener {
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
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun bindTrackInfo(track: Track) {
        val ivAlbumCover = findViewById<ImageView>(R.id.ivAlbumCover)
        val tvTrackName = findViewById<TextView>(R.id.tvTrackName)
        val tvArtistName = findViewById<TextView>(R.id.tvArtistName)
        val tvTrackTimeValue = findViewById<TextView>(R.id.tvTrackTimeValue)
        val lblCollectionTitle = findViewById<TextView>(R.id.lblCollectionTitle)
        val tvCollectionValue = findViewById<TextView>(R.id.tvCollectionValue)
        val lblReleaseDateTitle = findViewById<TextView>(R.id.lblReleaseDateTitle)
        val tvReleaseDateValue = findViewById<TextView>(R.id.tvReleaseDateValue)
        val tvGenreValue = findViewById<TextView>(R.id.tvGenreValue)
        val tvCountryValue = findViewById<TextView>(R.id.tvCountryValue)

        tvTrackName.text = track.trackName
        tvArtistName.text = track.artistName
        tvTrackTimeValue.text = formatTime(track.trackTimeMillis)
        tvGenreValue.text = track.primaryGenreName
        tvCountryValue.text = track.country

        if (!track.collectionName.isNullOrEmpty()) {
            tvCollectionValue.text = track.collectionName
            lblCollectionTitle.visibility = View.VISIBLE
            tvCollectionValue.visibility = View.VISIBLE
        } else {
            lblCollectionTitle.visibility = View.GONE
            tvCollectionValue.visibility = View.GONE
        }

        if (!track.releaseDate.isNullOrEmpty() && track.releaseDate.length >= 4) {
            tvReleaseDateValue.text = track.releaseDate.substring(0, 4)
            lblReleaseDateTitle.visibility = View.VISIBLE
            tvReleaseDateValue.visibility = View.VISIBLE
        } else {
            lblReleaseDateTitle.visibility = View.GONE
            tvReleaseDateValue.visibility = View.GONE
        }

        val radiusPx = dpToPx(8f, this)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.vector)
            .error(R.drawable.vector)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .into(ivAlbumCover)
    }
    private fun preparePlayer() {
        if (previewUrl.isNullOrEmpty()) return
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            btnPlay.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            handler.removeCallbacks(updateTimerRunnable)
            playerState = STATE_PREPARED
            btnPlay.setImageResource(R.drawable.ic_play)
            tvPlayTime.text = "00:00"
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
        btnPlay.setImageResource(R.drawable.ic_pause)
        playerState = STATE_PLAYING
        handler.post(updateTimerRunnable)
    }

    private fun pausePlayer() {
        if (playerState == STATE_PLAYING) {
            mediaPlayer.pause()
            btnPlay.setImageResource(R.drawable.ic_play)
            playerState = STATE_PAUSED
            handler.removeCallbacks(updateTimerRunnable)
        }
    }

    private fun formatTime(millis: Long): String {
        val formatter = SimpleDateFormat("mm:ss", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(millis)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}