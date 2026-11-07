package com.practicum.playlistmaker.library.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.gson.Gson
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.library.domain.models.Playlist
import java.io.File

class EditPlaylistFragment : NewPlaylistFragment() {

    private lateinit var playlist: Playlist

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val playlistJson = arguments?.getString(EDIT_PLAYLIST_ARG_KEY) ?: return
        playlist = Gson().fromJson(playlistJson, Playlist::class.java)

        binding.backButton.title = getString(R.string.edit_playlist_title)
        binding.btnCreate.text = getString(R.string.edit_playlist_save_button)

        binding.etName.setText(playlist.name)
        binding.etDescription.setText(playlist.description)

        val file = playlist.coverPath?.let { File(it) }
        Glide.with(this)
            .load(if (file != null && file.exists()) file else R.drawable.placeholder)
            .placeholder(R.drawable.placeholder)
            .transform(CenterCrop())
            .into(binding.ivPlaylistCover)

        if (playlist.coverPath != null) {
            binding.ivPlaylistCover.setPadding(0, 0, 0, 0)
        }

        binding.backButton.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

        binding.btnCreate.setOnClickListener {
            saveChanges()
        }
    }

    private fun saveChanges() {
        val newName = binding.etName.text?.toString() ?: ""
        val newDescription = binding.etDescription.text?.toString() ?: ""

        if (newName.isNotBlank()) {
            val finalCoverPath = if (imageUri != null) {
                saveImageToPrivateStorage(imageUri!!)
            } else {
                playlist.coverPath
            }

            val updatedPlaylist = playlist.copy(
                name = newName,
                description = newDescription,
                coverPath = finalCoverPath
            )

            viewModel.updatePlaylist(updatedPlaylist)

            Toast.makeText(
                requireContext(),
                "Плейлист $newName сохранен",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }
    }

    companion object {
        const val EDIT_PLAYLIST_ARG_KEY = "edit_playlist_data"
    }
}