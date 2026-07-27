package com.practicum.playlistmaker.search.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.creator.Creator
import com.practicum.playlistmaker.databinding.ActivitySearchBinding
import com.practicum.playlistmaker.player.ui.AudioPlayerActivity
import com.practicum.playlistmaker.search.domain.api.SearchHistoryInteractor
import com.practicum.playlistmaker.search.domain.api.TracksInteractor
import com.practicum.playlistmaker.search.domain.models.Track
import kotlin.collections.isNotEmpty

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openAudioPlayer(track)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (viewModel.clickDebounce()) {
                viewModel.addTrackToHistory(track)
                openAudioPlayer(track)
            }
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        viewModel.screenState.observe(this) { state ->
            render(state)
        }

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty()) {
                viewModel.clearSearch()
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = clearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    viewModel.clearSearch()
                } else {
                    binding.historyContainer.visibility = View.GONE
                    viewModel.searchDebounce(s.toString())
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        binding.searchEditText.addTextChangedListener(simpleTextWatcher)

        binding.clearButton.setOnClickListener {
            binding.searchEditText.setText("")
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        }

        binding.btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.refreshSearch()
        }

        binding.backButton.setNavigationOnClickListener {
            finish()
        }

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
            is SearchScreenState.Loading -> showLoading()
            is SearchScreenState.Success -> showSuccess(state.tracks)
            is SearchScreenState.History -> showHistory(state.tracks)
            is SearchScreenState.Error -> showError()
            is SearchScreenState.NotFound -> showNotFound()
            is SearchScreenState.Empty -> showEmpty()
        }
    }

    private fun showLoading() {
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun showSuccess(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
        trackAdapter.updateTracks(tracks)
        binding.recyclerView.visibility = View.VISIBLE
    }

    private fun showHistory(tracks: List<Track>) {
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
        historyAdapter.updateTracks(tracks)
        binding.historyContainer.visibility = View.VISIBLE
    }

    private fun showError() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        trackAdapter.updateTracks(emptyList())
        showPlaceholder(View.GONE, View.VISIBLE)
    }

    private fun showNotFound() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        trackAdapter.updateTracks(emptyList())
        showPlaceholder(View.VISIBLE, View.GONE)
    }

    private fun showEmpty() {
        binding.progressBar.visibility = View.GONE
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
    }

    private fun showPlaceholder(emptyVisibility: Int, errorVisibility: Int) {
        binding.placeholderEmpty.visibility = emptyVisibility
        binding.placeholderError.visibility = errorVisibility
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, binding.searchEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        binding.searchEditText.setText(savedText)
    }

    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun openAudioPlayer(track: Track) {
        val intent = Intent(this, AudioPlayerActivity::class.java)
        val trackJson = com.google.gson.Gson().toJson(track)
        intent.putExtra("TRACK_DATA_KEY", trackJson)
        startActivity(intent)
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }
}