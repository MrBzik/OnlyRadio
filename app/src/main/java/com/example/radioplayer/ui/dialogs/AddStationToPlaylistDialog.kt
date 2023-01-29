package com.example.radioplayer.ui.dialogs

import android.R
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogAddStationToPlaylistBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel

class AddStationToPlaylistDialog(
    private val requireContext : Context,
    private val listOfPlaylists : List<Playlist>,
    private val databaseViewModel: DatabaseViewModel,
    private val insertStationInPlaylist : (String) -> Unit


    ) : AppCompatDialog(requireContext) {

    private lateinit var bind : DialogAddStationToPlaylistBinding

    private lateinit var listOfNamesOfPlaylists : List<String>

    private lateinit var  arrayAdapter : ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogAddStationToPlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        if(listOfPlaylists.isEmpty()){
            bind.tvMessageNoPlaylists.isVisible = true

        } else{

            listOfNamesOfPlaylists = listOfPlaylists.map {
                it.playlistName
            }

            arrayAdapter = ArrayAdapter(requireContext, R.layout.simple_list_item_1, listOfNamesOfPlaylists)

            bind.listView.adapter = arrayAdapter

            bind.listView.setOnItemClickListener { parent, view, position, id ->

                val playlist = parent.getItemAtPosition(position) as String
                insertStationInPlaylist(playlist)
                dismiss()
            }

        }

        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvCreateNewPlaylist.setOnClickListener {

            CreatePlaylistDialog(requireContext, listOfPlaylists, databaseViewModel).show()
            dismiss()
        }
    }


}