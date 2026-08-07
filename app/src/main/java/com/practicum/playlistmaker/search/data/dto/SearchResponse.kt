package com.practicum.playlistmaker.search.data.dto

data class SearchResponse(val resultCount: Int, val results: List<TrackDto>)