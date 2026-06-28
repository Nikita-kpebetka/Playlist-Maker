package com.practicum.playlistmaker

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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }

    private lateinit var searchEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var placeholderEmpty: LinearLayout
    private lateinit var placeholderError: android.widget.ScrollView
    private lateinit var btnRefresh: Button
    private lateinit var historyContainer: LinearLayout
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var btnClearHistory: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var trackAdapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchHistory: SearchHistory

    private val handler = Handler(Looper.getMainLooper())

    private val searchRunnable = Runnable {
        val query = searchEditText.text.toString().trim()
        if (query.isNotEmpty()) {
            performSearch(query)
        }
    }
    private var isClickAllowed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPreferences = getSharedPreferences("playlist_maker_prefs", MODE_PRIVATE)
        searchHistory = SearchHistory(sharedPreferences)

        val toolbar = findViewById<MaterialToolbar>(R.id.backButton)
        searchEditText = findViewById(R.id.searchEditText)
        clearButton = findViewById(R.id.clearButton)
        recyclerView = findViewById(R.id.recyclerView)
        placeholderEmpty = findViewById(R.id.placeholderEmpty)
        placeholderError = findViewById(R.id.placeholderError)
        btnRefresh = findViewById(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar)
        historyContainer = findViewById(R.id.historyContainer)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        trackAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                openAudioPlayer(track)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trackAdapter

        historyAdapter = TrackAdapter(emptyList()) { track ->
            if (clickDebounce()) {
                searchHistory.addTrack(track)
                historyAdapter.updateTracks(searchHistory.getHistory())
                openAudioPlayer(track)
            }
        }
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
        historyRecyclerView.adapter = historyAdapter

        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && searchEditText.text.isEmpty() && searchHistory.getHistory().isNotEmpty()) {
                showHistoryList()
            } else {
                historyContainer.visibility = View.GONE
            }
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)

                if (s.isNullOrEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    trackAdapter.updateTracks(emptyList())
                    showPlaceholder(View.GONE, View.GONE)
                    progressBar.visibility = View.GONE

                    if (searchEditText.hasFocus() && searchHistory.getHistory().isNotEmpty()) {
                        showHistoryList()
                    } else {
                        historyContainer.visibility = View.GONE
                    }
                } else {
                    historyContainer.visibility = View.GONE
                    searchDebounce()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        searchEditText.addTextChangedListener(simpleTextWatcher)

        clearButton.setOnClickListener {
            searchEditText.setText("")
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        }

        btnClearHistory.setOnClickListener {
            searchHistory.clearHistory()
            historyContainer.visibility = View.GONE
        }

        btnRefresh.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                performSearch(query)
            }
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(searchRunnable)
                val query = searchEditText.text.toString().trim()
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
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE

        historyAdapter.updateTracks(searchHistory.getHistory())
        historyContainer.visibility = View.VISIBLE
    }

    private fun performSearch(query: String) {
        historyContainer.visibility = View.GONE
        recyclerView.visibility = View.GONE
        showPlaceholder(View.GONE, View.GONE)
        progressBar.visibility = View.VISIBLE

        RetrofitClient.api.search(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val tracks = response.body()?.results ?: emptyList()
                    if (tracks.isEmpty()) {
                        showPlaceholder(View.VISIBLE, View.GONE)
                        trackAdapter.updateTracks(emptyList())
                    } else {
                        showPlaceholder(View.GONE, View.GONE)
                        trackAdapter.updateTracks(tracks)
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    showPlaceholder(View.GONE, View.VISIBLE)
                    trackAdapter.updateTracks(emptyList())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showPlaceholder(View.GONE, View.VISIBLE)
                trackAdapter.updateTracks(emptyList())
            }
        })
    }

    private fun showPlaceholder(emptyVisibility: Int, errorVisibility: Int) {
        placeholderEmpty.visibility = emptyVisibility
        placeholderError.visibility = errorVisibility
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, searchEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        searchEditText.setText(savedText)
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
}