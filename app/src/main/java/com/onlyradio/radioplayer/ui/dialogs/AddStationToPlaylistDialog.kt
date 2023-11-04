package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.adapters.PlaylistsAdapter
import com.onlyradio.radioplayer.data.local.entities.Playlist
import com.onlyradio.radioplayer.databinding.DialogAddStationToPlaylistBinding
import com.onlyradio.radioplayer.ui.viewmodels.DatabaseViewModel
import com.onlyradio.radioplayer.ui.viewmodels.PixabayViewModel

class AddStationToPlaylistDialog(
    private val requireContext : Context,
    private val listOfPlaylists : List<Playlist>,
    private val databaseViewModel: DatabaseViewModel,
    private val pixabayViewModel: PixabayViewModel,
    private val glide : RequestManager,
    private val title : String,
    private val insertStationInPlaylist : (String) -> Unit
    ) : BaseDialog<DialogAddStationToPlaylistBinding>(requireContext,
        DialogAddStationToPlaylistBinding::inflate
    ) {


    private var playlistsAdapter = PlaylistsAdapter(glide, false)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        adjustDialogHeight(bind.clAddStationToPlaylistDialog)

        bind.tvTitle.text = title

        if(listOfPlaylists.isEmpty()){
            bind.tvMessageNoPlaylists.isVisible = true
        }



            bind.rvPlaylists.apply {

                adapter = playlistsAdapter
                layoutManager = GridLayoutManager(requireContext, 3)
                setHasFixedSize(true)
            }




            playlistsAdapter.differ.submitList(listOfPlaylists)

            playlistsAdapter.setPlaylistClickListener { playlist, _ ->

                insertStationInPlaylist(playlist.playlistName)

                dismiss()
            }



        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.tvCreateNewPlaylist.setOnClickListener {

            CreatePlaylistDialog(requireContext,
                listOfPlaylists,
                databaseViewModel,
                pixabayViewModel, glide) {
                insertStationInPlaylist(it)
            }
                .show()

            dismiss()
        }
    }



    override fun onStop() {
        super.onStop()
        bind.rvPlaylists.adapter = null
        playlistsAdapter.clearReferences()
        _bind = null
    }

}