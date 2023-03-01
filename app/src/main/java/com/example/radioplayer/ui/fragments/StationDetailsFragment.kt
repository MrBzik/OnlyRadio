package com.example.radioplayer.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.entities.Recording
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.data.models.PlayingItem
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
    private var currentRecording : Recording? = null

    private var isFavoured = false



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]

        checkIfRadioOrRecordAndCallFunctions()

        endLoadingBarIfNeeded()

    }

    private fun checkIfRadioOrRecordAndCallFunctions(){

        mainViewModel.newRadioStation.value?.let {

            if(it is PlayingItem.FromRadio){
                observeIfNewStationFavoured()
                updateUiForRadioStation(it.radioStation)
                updateListOfPlaylists()
                setAddToPlaylistClickListener()
                setFabStationHomePageClickListener()
                addToFavClickListener()
                setupRecordingButton()
                observeExoRecordState()

            } else if(it is PlayingItem.FromRecordings){
                updateUiForRecording(it.recording)
            }




        }

    }

    private fun updateUiForRecording(recording: Recording){

        bind.tvName.text = recording.name
        glide
            .load(recording.iconUri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(bind.ivIcon)
    }




    private fun setupRecordingButton(){

        setFabRecordListener()

    }


    private var isRecording = false
    private var isTimerObserverSet = false
    private var isConverterCallbackSet = false
    private var durationOfRecording = ""

    private fun observeExoRecordState(){

        mainViewModel.exoRecordState.observe(viewLifecycleOwner){

            if(it){
                 isRecording = true

                 bind.tvTimer.visibility = View.VISIBLE

                 if(!isTimerObserverSet){
                     mainViewModel.exoRecordTimer.observe(viewLifecycleOwner){ time ->
                         bind.tvTimer.text = time
                     }
                     isTimerObserverSet = true
                 }


                 bind.fabRecording.setImageResource(R.drawable.ic_stop_recording)

                 val id = (mainViewModel.radioSource.newExoRecord)
                 val name = "Rec. ${currentRadioStation?.name}"
                 databaseViewModel.insertNewRecording(
                     id,
                     currentRadioStation?.favicon ?: "",
                     name,
                     "00:00:00"
                 )


            } else {

                durationOfRecording = bind.tvTimer.text.toString()
                bind.tvTimer.text = "Processing..."
                bind.fabRecording.setImageResource(R.drawable.ic_start_recording)

                if(!isConverterCallbackSet){
                    mainViewModel.exoRecordFinishConverting.observe(viewLifecycleOwner){ finished ->
                        if(finished){
                            bind.tvTimer.text = "Saved"
                            isRecording = false
                            databaseViewModel
                                .updateRecordingDuration(
                                    durationOfRecording,
                                mainViewModel.radioSource.newExoRecord)
                        }
                    }
                    isConverterCallbackSet = true
                }
            }
        }
    }

    private fun setFabRecordListener(){
        bind.fabRecording.setOnClickListener{
            if(isRecording){
                mainViewModel.stopRecording()
            } else {
                mainViewModel.startRecording()
            }
        }
    }


    private fun endLoadingBarIfNeeded(){
        (activity as MainActivity).separatorLeftAnim.endLoadingAnim()
        (activity as MainActivity).separatorRightAnim.endLoadingAnim()
    }



    private fun observeIfNewStationFavoured(){

        databaseViewModel.isStationFavoured.observe(viewLifecycleOwner){

            paintButtonAddToFav(it)

            isFavoured = it

        }
    }


    private fun checkIfStationFavoured(station: RadioStation){
        databaseViewModel.checkIfStationIsFavoured(station.stationuuid)
    }


    private fun paintButtonAddToFav(isInDB : Boolean){
        if(!isInDB){
            bind.fabAddToFav.setImageResource(R.drawable.ic_add_to_fav)

        } else {
            bind.fabAddToFav.setImageResource(R.drawable.ic_added_to_fav)

        }
    }


    private fun updateUiForRadioStation(station : RadioStation){

            homepageUrl = station.homepage
            if(!homepageUrl.isNullOrBlank()){
                bind.tvHomePage.visibility = View.VISIBLE
            }

            bind.fabAddToFav.visibility = View.VISIBLE
            bind.fabRecording.visibility = View.VISIBLE
            bind.tvAddToPlaylist.visibility = View.VISIBLE
            bind.ivArrowAddToPlaylist.visibility = View.VISIBLE


            checkIfStationFavoured(station)

            currentRadioStation = station

            bind.tvName.text = station.name

            glide
                .load(station.favicon)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.ivIcon)

            if(!station.country.isNullOrBlank()){
                bind.tvCountry.isVisible = true
                bind.tvCountry.text = station.country
            }

            if(!station.language.isNullOrBlank()){
                bind.tvLanguage.isVisible = true
                bind.tvLanguage.text = "Languages : ${station.language}"
            }
            if(!station.tags.isNullOrBlank()){
                bind.svTvTags.isVisible = true
                val tags = station.tags.replace(",", ", ")
                bind.tvTags.text = "Tags : $tags"
            }

    }


    private fun updateListOfPlaylists(){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            listOfPlaylists = it
        }
    }


    private fun setFabStationHomePageClickListener(){

        bind.tvHomePage.setOnClickListener {

            if(!homepageUrl.isNullOrBlank()) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl))
                startActivity(webIntent)
            }
        }
    }

    private fun setAddToPlaylistClickListener(){

        bind.tvAddToPlaylist.setOnClickListener {
            AddStationToPlaylistDialog(
                requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide
            ) { playlistName ->
                insertStationInPlaylist(playlistName)
            }.show()
        }


    }


    private fun addToFavClickListener(){

        bind.fabAddToFav.setOnClickListener {

            if(isFavoured) {

                currentRadioStation?.let {
                    databaseViewModel.updateIsFavouredState(0, it.stationuuid)
                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout),
                        "Station removed from favs", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.isStationFavoured.postValue(false)
                }

            } else {
                currentRadioStation?.let {
                    databaseViewModel.updateIsFavouredState(System.currentTimeMillis(), it.stationuuid)
                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout),
                        "Station saved to favs", Snackbar.LENGTH_SHORT).show()
                    databaseViewModel.isStationFavoured.postValue(true)
                }
            }
        }
    }



    private fun insertStationInPlaylist(playlistName : String){

        currentRadioStation?.let { station ->

            databaseViewModel.insertStationPlaylistCrossRef(
                StationPlaylistCrossRef(
                    station.stationuuid, playlistName, System.currentTimeMillis()
                )
            )

            Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
                "Station was added to $playlistName",
                Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isTimerObserverSet = false
        isConverterCallbackSet = false
        _bind = null
    }


}