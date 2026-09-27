package com.practicum.playlistmaker.player.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AudioPlayerFragment : Fragment() {

    private var _binding: FragmentAudioPlayerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlayerViewModel by viewModel()

    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trackJson = arguments?.getString(TRACK_DATA_KEY)
        val track = Gson().fromJson(trackJson, Track::class.java)

        binding.backButton.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvTrackName.text = track.trackName
        binding.tvArtistName.text = track.artistName
        binding.tvTrackTimeValue.text = trackTimeFormatter.format(track.trackTimeMillis)
        binding.tvCollectionValue.text = track.collectionName ?: ""
        binding.tvReleaseDateValue.text = if (!track.releaseDate.isNullOrEmpty() && track.releaseDate.length >= 4) {
            track.releaseDate.substring(0, 4)
        } else {
            ""
        }
        binding.tvGenreValue.text = track.primaryGenreName
        binding.tvCountryValue.text = track.country

        val radiusPx = resources.getDimensionPixelSize(R.dimen.button_radius_search)
        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .into(binding.ivAlbumCover)

        viewModel.playerScreenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlayerScreenState.Playing -> {
                    binding.btnPlay.setImageResource(R.drawable.ic_pause)
                    binding.tvPlayTime.text = state.component1()
                }
                is PlayerScreenState.Paused -> {
                    binding.btnPlay.setImageResource(R.drawable.ic_play)
                    binding.tvPlayTime.text = state.component1()
                }
                is PlayerScreenState.Prepared -> {
                    binding.btnPlay.setImageResource(R.drawable.ic_play)
                    binding.tvPlayTime.text = "00:00"
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            viewModel.playbackControl()
        }

        viewModel.preparePlayer(track.previewUrl)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopAndReleasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TRACK_DATA_KEY = "track_data"
    }
}