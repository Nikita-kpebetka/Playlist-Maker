package com.practicum.playlistmaker.library.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistDetailsBinding
import com.practicum.playlistmaker.library.domain.models.Playlist
import com.practicum.playlistmaker.player.ui.AudioPlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import com.practicum.playlistmaker.search.ui.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class PlaylistDetailsFragment : Fragment() {

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PlaylistDetailsViewModel by viewModel()
    private var trackAdapter: TrackAdapter? = null

    private lateinit var tracksBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var menuBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistId = arguments?.getInt(PLAYLIST_ID_KEY) ?: return

        binding.backButton.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (menuBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                        menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    } else {
                        findNavController().navigateUp()
                    }
                }
            }
        )

        setupBottomSheetTracks()
        setupBottomSheetMenu()
        setupTracksList()

        viewModel.playlistLiveData.observe(viewLifecycleOwner) { playlist ->
            renderPlaylistInfo(playlist)
            renderMenuPlaylistHeader(playlist)
        }

        viewModel.tracksLiveData.observe(viewLifecycleOwner) { tracks ->
            trackAdapter?.updateTracks(tracks)

            if (tracks.isEmpty()) {
                binding.bottomSheetTracks.rvTracks.visibility = View.GONE
                binding.bottomSheetTracks.emptyTracksPlaceholder.visibility = View.VISIBLE

                tracksBottomSheetBehavior.isHideable = false
                tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                tracksBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                            tracksBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            } else {
                binding.bottomSheetTracks.rvTracks.visibility = View.VISIBLE
                binding.bottomSheetTracks.emptyTracksPlaceholder.visibility = View.GONE

                tracksBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {}
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }

            viewModel.playlistLiveData.value?.let { playlist ->
                updatePlaylistStatistics(tracks, playlist)
            }
        }

        viewModel.loadPlaylistDetails(playlistId)

        binding.btnShare.setOnClickListener {
            sharePlaylist()
        }

        binding.btnMore.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.bottomSheetMenuContainer.btnMenuShare.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            sharePlaylist()
        }

        binding.bottomSheetMenuContainer.btnMenuEdit.setOnClickListener {
            binding.overlay.visibility = View.GONE
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            val playlist = viewModel.playlistLiveData.value ?: return@setOnClickListener
            val playlistJson = Gson().toJson(playlist)
            val bundle = Bundle().apply {
                putString(EditPlaylistFragment.EDIT_PLAYLIST_ARG_KEY, playlistJson)
            }
            findNavController().navigate(
                R.id.action_playlistDetailsFragment_to_editPlaylistFragment,
                bundle
            )
        }

        binding.bottomSheetMenuContainer.btnMenuDelete.setOnClickListener {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog(playlistId)
        }

        binding.root.post {
            menuBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun renderPlaylistInfo(playlist: Playlist) {
        binding.tvPlaylistName.text = playlist.name

        if (!playlist.description.isNullOrBlank()) {
            binding.tvYear.text = playlist.description
            binding.tvYear.visibility = View.VISIBLE
        } else {
            binding.tvYear.visibility = View.GONE
        }

        val file = playlist.coverPath?.let { File(it) }
        Glide.with(this)
            .load(if (file != null && file.exists()) file else R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop())
            .into(binding.ivPlaylistCover)
    }

    private fun renderMenuPlaylistHeader(playlist: Playlist) {
        val menuHeader = binding.bottomSheetMenuContainer.playlistItem
        menuHeader.tvPlaylistName.text = playlist.name

        val countText = resources.getQuantityString(R.plurals.tracks_count, playlist.tracksCount, playlist.tracksCount)
        menuHeader.tvTrackCount.text = countText

        val file = playlist.coverPath?.let { File(it) }
        Glide.with(this)
            .load(if (file != null && file.exists()) file else R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(resources.getDimensionPixelSize(R.dimen.album_cover_corner_radius_small)))
            .into(menuHeader.ivPlaylistCover)
    }

    private fun setupBottomSheetTracks() {
        tracksBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetTracks.root).apply {
            isHideable = false
        }

        binding.root.post {
            val btnBottom = binding.btnShare.bottom
            val screenHeight = binding.root.height
            val paddingPx = (24 * resources.displayMetrics.density).toInt()
            val computedPeekHeight = screenHeight - btnBottom - paddingPx
            tracksBottomSheetBehavior.peekHeight = computedPeekHeight
        }
    }

    private fun setupBottomSheetMenu() {
        menuBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheetMenuContainer.root).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        menuBottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (_binding != null) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        binding.overlay.visibility = View.GONE
                    } else {
                        binding.overlay.visibility = View.VISIBLE
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (_binding != null) {
                    binding.overlay.alpha = slideOffset + 1f
                }
            }
        })
    }

    private fun setupTracksList() {
        trackAdapter = TrackAdapter(
            tracks = emptyList(),
            clickListener = { track ->
                val trackJson = Gson().toJson(track)
                val bundle = Bundle().apply {
                    putString(AudioPlayerFragment.TRACK_DATA_KEY, trackJson)
                }
                findNavController().navigate(
                    R.id.action_playlistDetailsFragment_to_fragmentAudioPlayer,
                    bundle
                )
            },
            longClickListener = { track ->
                val playlistId = arguments?.getInt(PLAYLIST_ID_KEY) ?: return@TrackAdapter
                showDeleteTrackDialog(playlistId, track)
            }
        )

        binding.bottomSheetTracks.rvTracks.layoutManager = LinearLayoutManager(requireContext())
        binding.bottomSheetTracks.rvTracks.adapter = trackAdapter
    }

    private fun sharePlaylist() {
        val tracksCount = trackAdapter?.itemCount ?: 0
        if (tracksCount == 0) {
            Toast.makeText(
                requireContext(),
                getString(R.string.empty_playlist_share_error),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val playlist = viewModel.playlistLiveData.value ?: return
            val tracks = viewModel.tracksLiveData.value ?: return

            val countText = resources.getQuantityString(R.plurals.tracks_count, tracks.size, tracks.size)
            val shareStringBuilder = StringBuilder()

            shareStringBuilder.append(playlist.name).append("\n")
            if (!playlist.description.isNullOrBlank()) {
                shareStringBuilder.append(playlist.description).append("\n")
            }
            shareStringBuilder.append(countText).append("\n")

            tracks.forEachIndexed { index, track ->
                val trackNumber = index + 1
                val duration = formatTrackTime(track.trackTimeMillis)
                shareStringBuilder.append("$trackNumber. ${track.artistName} - ${track.trackName} ($duration)")
                if (index < tracks.size - 1) {
                    shareStringBuilder.append("\n")
                }
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareStringBuilder.toString())
            }

            startActivity(Intent.createChooser(shareIntent, null))
        }
    }

    private fun showDeleteTrackDialog(playlistId: Int, track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.delete_track_dialog_message))
            .setNegativeButton(getString(R.string.delete_track_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete_track_dialog_positive)) { _, _ ->
                viewModel.deleteTrack(playlistId, track.trackId)
            }
            .show()
    }

    private fun showDeletePlaylistDialog(playlistId: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_playlist_dialog_title))
            .setMessage(getString(R.string.delete_playlist_dialog_message))
            .setNegativeButton(getString(R.string.delete_track_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.delete_track_dialog_positive)) { _, _ ->
                viewModel.deletePlaylist(playlistId) {
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    private fun updatePlaylistStatistics(tracks: List<Track>, playlist: Playlist) {
        val totalMillis = tracks.sumOf { it.trackTimeMillis }
        val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(totalMillis).toInt()

        val minutesText = resources.getQuantityString(
            R.plurals.minutes_count,
            totalMinutes,
            totalMinutes
        )

        val countText = resources.getQuantityString(
            R.plurals.tracks_count,
            tracks.size,
            tracks.size
        )

        binding.tvInfo.text = "$minutesText • $countText"
    }

    private fun formatTrackTime(millis: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(millis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        trackAdapter = null
    }

    companion object {
        const val PLAYLIST_ID_KEY = "playlist_id_data"
    }
}