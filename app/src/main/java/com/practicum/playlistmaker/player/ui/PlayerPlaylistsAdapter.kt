package com.practicum.playlistmaker.player.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.practicum.playlistmaker.databinding.ItemBottomSheetPlaylistBinding
import com.practicum.playlistmaker.library.domain.models.Playlist

class PlayerPlaylistsAdapter(
    private val clickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<PlayerPlaylistViewHolder>() {

    var playlists = listOf<Playlist>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerPlaylistViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PlayerPlaylistViewHolder(ItemBottomSheetPlaylistBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: PlayerPlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { clickListener(playlist) }
    }

    override fun getItemCount(): Int = playlists.size
}