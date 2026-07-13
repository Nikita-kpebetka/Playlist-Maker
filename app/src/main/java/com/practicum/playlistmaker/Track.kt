package com.practicum.playlistmaker

data class Track(val trackName: String,
                 val artistName: String,
                 val trackTimeMillis: Long,
                 val artworkUrl100: String,
                 val trackId: Long,
                 val collectionName: String?,
                 val releaseDate: String?,
                 val primaryGenreName: String,
                 val country: String,
                 val previewUrl: String?,)
{
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
}

