package com.practicum.playlistmaker.library.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.ItemPlaylistGridBinding
import com.practicum.playlistmaker.library.domain.models.Playlist

class PlaylistsAdapter(
    private val clickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistViewHolder>() {

    var playlists: List<Playlist> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlaylistViewHolder(ItemPlaylistGridBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { clickListener(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}