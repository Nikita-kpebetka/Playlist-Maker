package com.practicum.playlistmaker.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.practicum.playlistmaker.domain.api.SearchHistoryRepository
import com.practicum.playlistmaker.domain.models.Track

class SearchHistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SearchHistoryRepository {

    private val gson = Gson()
    private val historyKey = "SEARCH_HISTORY_KEY"
    private val maxHistorySize = 10

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(historyKey, null) ?: return emptyList()
        val type = object : TypeToken<ArrayList<Track>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun addTrack(track: Track) {
        val currentHistory = ArrayList(getHistory())
        currentHistory.removeIf { it.trackId == track.trackId }
        currentHistory.add(0, track)

        if (currentHistory.size > maxHistorySize) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        saveHistory(currentHistory)
    }

    override fun clearHistory() {
        sharedPreferences.edit().remove(historyKey).apply()
    }

    private fun saveHistory(history: ArrayList<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(historyKey, json).apply()
    }
}