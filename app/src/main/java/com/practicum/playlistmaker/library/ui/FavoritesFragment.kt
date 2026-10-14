package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentFavoritesBinding
import com.practicum.playlistmaker.player.ui.AudioPlayerFragment
import com.practicum.playlistmaker.search.ui.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private val viewModel: FavoritesViewModel by viewModel()
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private var adapter: TrackAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TrackAdapter(emptyList()) { track ->
            val trackJson = Gson().toJson(track)
            val bundle = Bundle().apply {
                putString(AudioPlayerFragment.TRACK_DATA_KEY, trackJson)
            }
            findNavController().navigate(
                R.id.action_fragmentMediaLibrary_to_fragmentAudioPlayer,
                bundle
            )
        }

        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.favoritesRecyclerView.adapter = adapter

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FavoritesScreenState.Content -> {
                    binding.emptyPlaceholder.visibility = View.GONE
                    binding.favoritesRecyclerView.visibility = View.VISIBLE
                    adapter?.updateTracks(state.tracks)
                }
                is FavoritesScreenState.Empty -> {
                    binding.favoritesRecyclerView.visibility = View.GONE
                    binding.emptyPlaceholder.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        adapter = null
    }

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}