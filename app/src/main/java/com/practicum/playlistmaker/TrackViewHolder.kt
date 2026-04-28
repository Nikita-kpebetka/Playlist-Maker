package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context)
        .inflate(R.layout.recycler_view_item, parent, false)
) {
    val albumArt: ImageView = itemView.findViewById(R.id.ivAlbumArt)
    val songTitle: TextView = itemView.findViewById(R.id.tvSongTitle)
    val songSubtitle: TextView = itemView.findViewById(R.id.tvSongSubtitle)

    fun bind(track: Track) {
        songTitle.text = track.trackName
        songSubtitle.text = "${track.artistName} • ${track.trackTime}"

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(2))
            .centerCrop()
            .into(albumArt)
    }
}