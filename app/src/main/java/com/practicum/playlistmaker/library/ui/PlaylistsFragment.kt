package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentPlaylistsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private val playlistsAdapter = PlaylistsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvPlaylists.adapter = playlistsAdapter
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        binding.btnNewPlaylist.setOnClickListener {
            findNavController().navigate(
                R.id.action_fragmentMediaLibrary_to_newPlaylistFragment
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fillData()
    }

    private fun render(state: PlaylistsScreenState) {
        when (state) {
            is PlaylistsScreenState.Empty -> {
                binding.emptyStateContainer.visibility = View.VISIBLE
                binding.rvPlaylists.visibility = View.GONE
            }
            is PlaylistsScreenState.Content -> {
                binding.emptyStateContainer.visibility = View.GONE
                binding.rvPlaylists.visibility = View.VISIBLE
                playlistsAdapter.playlists = state.playlists
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}