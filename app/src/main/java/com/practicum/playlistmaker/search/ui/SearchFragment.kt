package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentSearchBinding
import com.practicum.playlistmaker.player.ui.AudioPlayerFragment
import com.practicum.playlistmaker.search.domain.models.Track
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openAudioPlayer(track)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openAudioPlayer(track)
            }
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = historyAdapter

        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            render(state)
        }

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty()) {
                viewModel.clearSearch()
            }
        }

        binding.searchEditText.doOnTextChanged { s, _, _, _ ->
            binding.clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            if (s.isNullOrEmpty()) {
                viewModel.clearSearch()
            } else {
                binding.historyContainer.visibility = View.GONE
                viewModel.searchDebounce(s.toString())
            }
        }

        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }

        binding.btnClearHistory.setOnClickListener { viewModel.clearHistory() }
        binding.btnRefresh.setOnClickListener { viewModel.refreshSearch() }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.refreshSearch()
                true
            }
            false
        }
    }

    private fun render(state: SearchScreenState) {
        when (state) {
            SearchScreenState.Loading -> showLoading()
            is SearchScreenState.Success -> showSuccess(state.tracks)
            is SearchScreenState.History -> showHistory(state.tracks)
            SearchScreenState.Error -> showError()
            SearchScreenState.NotFound -> showNotFound()
            SearchScreenState.Empty -> showEmpty()
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
    }

    private fun showSuccess(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        trackAdapter.updateTracks(tracks)
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showHistory(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        historyAdapter.updateTracks(tracks)
        binding.historyContainer.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.VISIBLE
    }

    private fun showNotFound() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.VISIBLE
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.placeholderEmpty.visibility = View.GONE
        binding.placeholderError.visibility = View.GONE
    }

    private fun openAudioPlayer(track: Track) {
        val trackJson = Gson().toJson(track)
        val bundle = Bundle().apply {
            putString(AudioPlayerFragment.TRACK_DATA_KEY, trackJson)
        }

        findNavController().navigate(
            R.id.action_fragmentSearch_to_fragmentAudioPlayer,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}