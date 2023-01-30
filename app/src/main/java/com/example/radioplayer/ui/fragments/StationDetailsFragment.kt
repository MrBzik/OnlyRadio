package com.example.radioplayer.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentStationDetailsBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.dialogs.AddStationToPlaylistDialog
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailsFragment : Fragment()
{
    lateinit var bind : FragmentStationDetailsBinding

    lateinit var mainViewModel: MainViewModel
    lateinit var databaseViewModel: DatabaseViewModel

    private var homepageUrl : String? = null

    private var listOfPlaylists : List<Playlist> = emptyList()

    @Inject
    lateinit var glide : RequestManager

    private var currentRadioStation : RadioStation? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_stationDetailsFragment_to_radioSearchFragment2)
                    (activity as MainActivity).fabAddToFav.visibility = View.GONE
                    (activity as MainActivity).tvExpandHide.setText(R.string.Expand)
                }
            })
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentStationDetailsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mainViewModel = (activity as MainActivity).mainViewModel
        databaseViewModel = (activity as MainActivity).databaseViewModel


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
            bind.tvCountry.text = it.country

            glide.load(it.favicon).into(bind.ivIcon)

            homepageUrl = it.homepage

            val tags = it.tags?.replace(",", ", ")
            bind.tvTags.text = "Tags : $tags"

            bind.tvLanguage.text = "Languages : ${it.language}"
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
                requireContext(), listOfPlaylists, databaseViewModel
            ) { playlistName ->

                insertStationInPlaylist(playlistName)

            }.show()
        }


    }


    private fun insertStationInPlaylist(playlistName : String){

        currentRadioStation?.let { station ->

            databaseViewModel.checkIfInPlaylistOrIncrement(playlistName, station.stationuuid)

            databaseViewModel.insertStationPlaylistCrossRef(
                StationPlaylistCrossRef(
                    station.stationuuid, playlistName
                )
            )

            Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
                "Station was added to $playlistName",
                Snackbar.LENGTH_SHORT).show()
        }
    }




}