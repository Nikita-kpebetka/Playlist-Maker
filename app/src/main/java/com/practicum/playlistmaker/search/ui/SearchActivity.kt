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

    private val tracksInteractor: TracksInteractor = Creator.provideTracksInteractor()
    private lateinit var searchHistoryInteractor: SearchHistoryInteractor

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }
    private var isClickAllowed = true

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

        searchHistoryInteractor = Creator.provideSearchHistoryInteractor(this)

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.addTrack(track)
                openAudioPlayer(track)
            }
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistoryInteractor.addTrack(track)
                historyAdapter.updateTracks(searchHistoryInteractor.getHistory())
                openAudioPlayer(track)
            }
        }
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.searchEditText.text.isEmpty() && searchHistoryInteractor.getHistory().isNotEmpty()) {
                showHistoryList()
            } else {
                binding.historyContainer.visibility = View.GONE
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clearButton.visibility = clearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    trackAdapter.updateTracks(emptyList())
                    showPlaceholder(View.GONE, View.GONE)
                    binding.progressBar.visibility = View.GONE

                    if (binding.searchEditText.hasFocus() && searchHistoryInteractor.getHistory().isNotEmpty()) {
                        showHistoryList()
                    } else {
                        binding.historyContainer.visibility = View.GONE
                    }
                } else {
                    binding.historyContainer.visibility = View.GONE
                    searchDebounce()
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
            searchHistoryInteractor.clearHistory()
            binding.historyContainer.visibility = View.GONE
        }

        binding.btnRefresh.setOnClickListener {
            val query = binding.searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        binding.backButton.setNavigationOnClickListener {
            finish()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                val query = binding.searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                }
                true
            }
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    private fun showHistoryList() {
        showPlaceholder(View.GONE, View.GONE)
        binding.recyclerView.visibility = View.GONE
        binding.progressBar.visibility = View.GONE

        historyAdapter.updateTracks(searchHistoryInteractor.getHistory())
        binding.historyContainer.visibility = View.VISIBLE
    }

    private fun performSearch(query: String) {
        binding.historyContainer.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
        binding.progressBar.visibility = View.VISIBLE

        tracksInteractor.searchTracks(query, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?, hasError: Boolean) {
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE

                    if (hasError) {
                        showPlaceholder(View.GONE, View.VISIBLE)
                        trackAdapter.updateTracks(emptyList())
                    } else if (foundTracks.isNullOrEmpty()) {
                        showPlaceholder(View.VISIBLE, View.GONE)
                        trackAdapter.updateTracks(emptyList())
                    } else {
                        showPlaceholder(View.GONE, View.GONE)
                        trackAdapter.updateTracks(foundTracks)
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        })
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
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}