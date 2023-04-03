package com.example.radioplayer.ui.dialogs

import android.R
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogAddStationToPlaylistBinding
import com.example.radioplayer.databinding.DialogDeletePlaylistBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel

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