package com.example.radioplayer.ui.dialogs

import android.R
import android.content.Context
import android.os.Bundle
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


    ) : AppCompatDialog(requireContext) {

    private lateinit var bind : DialogDeletePlaylistBinding




    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogDeletePlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        bind.tvTitle.text = "Delete playlist $currentPlaylist and its content?"


        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvDeletePlaylist.setOnClickListener {

            deletePlaylist()

            dismiss()

        }

    }


}