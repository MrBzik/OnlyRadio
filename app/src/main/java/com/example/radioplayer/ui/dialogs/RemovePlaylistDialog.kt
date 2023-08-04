package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import com.example.radioplayer.databinding.DialogDeletePlaylistBinding

class RemovePlaylistDialog(
    private val requireContext : Context,
    private val currentPlaylist : String,
    private val deletePlaylist : () -> Unit


    ) : BaseDialog<DialogDeletePlaylistBinding>
    (requireContext, DialogDeletePlaylistBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        bind.tvNameOfPlaylist.text = currentPlaylist


        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvDeletePlaylist.setOnClickListener {

            deletePlaylist()

            dismiss()

        }

    }


    override fun onStop() {
        super.onStop()
        _bind = null
    }

}