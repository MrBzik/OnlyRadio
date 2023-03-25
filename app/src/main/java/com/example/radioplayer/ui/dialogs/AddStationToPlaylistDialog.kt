package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PlaylistsAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogAddStationToPlaylistBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.PixabayViewModel

class AddStationToPlaylistDialog(
    private val requireContext : Context,
    private val listOfPlaylists : List<Playlist>,
    private val databaseViewModel: DatabaseViewModel,
    private val pixabayViewModel: PixabayViewModel,
    private  val glide : RequestManager,
    private val insertStationInPlaylist : (String) -> Unit
    ) : BaseDialog<DialogAddStationToPlaylistBinding>(requireContext,
        DialogAddStationToPlaylistBinding::inflate
    ) {


    private var playlistsAdapter = PlaylistsAdapter(glide, false)

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        adjustDialogHeight(bind.clAddStationToPlaylistDialog)


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