package com.practicum.playlistmaker.library.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.practicum.playlistmaker.R
import com.practicum.playlistmaker.databinding.FragmentNewPlaylistBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewPlaylistViewModel by viewModel()
    private var imageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri
            binding.ivPlaylistCover.setPadding(0, 0, 0, 0)
            val radiusPx = resources.getDimensionPixelSize(R.dimen.button_radius_search)
            Glide.with(this)
                .load(uri)
                .transform(CenterCrop(), RoundedCorners(radiusPx))
                .into(binding.ivPlaylistCover)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setNavigationOnClickListener {
            handleBackAction()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackAction()
                }
            }
        )

        binding.ivPlaylistCover.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.etName.doOnTextChanged { text, _, _, _ ->
            viewModel.onNameChanged(text?.toString())
        }

        viewModel.isCreateButtonEnabled.observe(viewLifecycleOwner) { isEnabled ->
            binding.btnCreate.isEnabled = isEnabled
        }

        binding.btnCreate.setOnClickListener {
            val playlistName = binding.etName.text.toString()
            val playlistDescription = binding.etDescription.text.toString()
            val savedImagePath = imageUri?.let { saveImageToPrivateStorage(it) }

            viewModel.createPlaylist(playlistName, playlistDescription, savedImagePath)
            Toast.makeText(
                requireContext(),
                "Плейлист $playlistName создан",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }
    }

    private fun handleBackAction() {
        val hasName = !binding.etName.text.isNullOrBlank()
        val hasDescription = !binding.etDescription.text.isNullOrBlank()
        val hasCover = imageUri != null

        if (hasName || hasDescription || hasCover) {
            showConfirmationDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.cancel_dialog_title))
            .setMessage(getString(R.string.cancel_dialog_message))
            .setNegativeButton(getString(R.string.cancel_dialog_negative)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.cancel_dialog_positive)) { _, _ ->
                findNavController().navigateUp()
            }
            .show()
    }

    private fun saveImageToPrivateStorage(uri: Uri): String {
        val filePath = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "playlist_covers")
        if (!filePath.exists()) {
            filePath.mkdirs()
        }
        val file = File(filePath, "cover_${System.currentTimeMillis()}.jpg")
        requireContext().contentResolver.openInputStream(uri).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}