package com.practicum.playlistmaker.library.ui

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.ItemPlaylistGridBinding
import com.practicum.playlistmaker.library.domain.models.Playlist
import java.io.File

class PlaylistViewHolder(
    private val binding: ItemPlaylistGridBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(playlist: Playlist) {
        binding.tvPlaylistName.text = playlist.name

        val count = playlist.tracksCount
        binding.tvTrackCount.text =
            itemView.resources.getQuantityString(R.plurals.tracks_count, count, count)

        val radiusPx = itemView.resources.getDimensionPixelSize(R.dimen.button_radius_search)
        val file = playlist.coverPath?.let { File(it) }

        Glide.with(itemView)
            .load(if (file != null && file.exists()) file else R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .fallback(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .transform(CenterCrop(), RoundedCorners(radiusPx))
            .into(binding.ivPlaylistCover)
    }
}