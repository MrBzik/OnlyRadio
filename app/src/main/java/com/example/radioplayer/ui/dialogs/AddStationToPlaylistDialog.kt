package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.example.radioplayer.adapters.PlaylistsAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogAddStationToPlaylistBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.PixabayViewModel

class AddStationToPlaylistDialog(
    private val requireContext : Context,
    private val listOfPlaylists : List<Playlist>,
    private val databaseViewModel: DatabaseViewModel,
    private val pixabayViewModel: PixabayViewModel,
    private  val glide : RequestManager,
    private val insertStationInPlaylist : (String) -> Unit
    ) : AppCompatDialog(requireContext) {

    private var _bind : DialogAddStationToPlaylistBinding? = null
    private val bind get() = _bind!!

    private var playlistsAdapter = PlaylistsAdapter(glide, false)

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogAddStationToPlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

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
        _bind = null
    }

}