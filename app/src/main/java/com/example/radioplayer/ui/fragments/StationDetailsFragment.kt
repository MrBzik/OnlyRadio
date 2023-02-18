package com.example.radioplayer.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentStationDetailsBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.dialogs.AddStationToPlaylistDialog
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailsFragment : BaseFragment<FragmentStationDetailsBinding>(
    FragmentStationDetailsBinding::inflate
)
{

    lateinit var pixabayViewModel: PixabayViewModel

    private var homepageUrl : String? = null

    private var listOfPlaylists : List<Playlist> = emptyList()

    @Inject
    lateinit var glide : RequestManager

    private var currentRadioStation : RadioStation? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]

        subscribeToObservers()

        setAddToPlaylistClickListener()

        setFabStationHomePageClickListener()

    }


    private fun subscribeToObservers(){

        observeCurrentStationAndUpdateUI()

        updateListOfPlaylists()

    }


    private fun observeCurrentStationAndUpdateUI(){

        mainViewModel.newRadioStation.observe(viewLifecycleOwner){

            currentRadioStation = it

            bind.tvName.text = it.name

            glide
                .load(it.favicon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.ivIcon)

            homepageUrl = it.homepage


            if(!it.country.isNullOrBlank()){
                bind.tvCountry.isVisible = true
                bind.tvCountry.text = it.country
            }

            if(!it.language.isNullOrBlank()){
                bind.tvLanguage.isVisible = true
                bind.tvLanguage.text = "Languages : ${it.language}"
            }
            if(!it.tags.isNullOrBlank()){
                bind.tvTags.isVisible = true
                val tags = it.tags.replace(",", ", ")
                bind.tvTags.text = "Tags : $tags"
            }
        }
    }


    private fun updateListOfPlaylists(){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            listOfPlaylists = it
        }
    }


    private fun setFabStationHomePageClickListener(){

        bind.fabStationHomePage.setOnClickListener {

            if(homepageUrl != "null") {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl))
                startActivity(webIntent)
            }
        }
    }

    private fun setAddToPlaylistClickListener(){

        bind.ivAddToPlaylist.setOnClickListener {
            AddStationToPlaylistDialog(
                requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide
            ) { playlistName ->

                insertStationInPlaylist(playlistName)

            }.show()
        }


    }


    private fun insertStationInPlaylist(playlistName : String){

        currentRadioStation?.let { station ->

            databaseViewModel.insertStationPlaylistCrossRefAndUpdate(
                StationPlaylistCrossRef(
                    station.stationuuid, playlistName
                ), playlistName
            )

            Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
                "Station was added to $playlistName",
                Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _bind = null
    }


}