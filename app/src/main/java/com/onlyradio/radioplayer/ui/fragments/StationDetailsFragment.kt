package com.onlyradio.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_DRAGGING
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.bumptech.glide.RequestManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.ViewPagerStationsAdapter
import com.onlyradio.radioplayer.data.local.entities.Playlist
import com.onlyradio.radioplayer.data.local.entities.RadioStation
import com.onlyradio.radioplayer.data.local.entities.Title
import com.onlyradio.radioplayer.databinding.FragmentStationDetailsBinding
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.exoPlayer.RadioSource
import com.onlyradio.radioplayer.exoPlayer.isPlaying
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.animations.PagerZoomOutSlideTransformer
import com.onlyradio.radioplayer.ui.animations.reduceDragSensitivity
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.dialogs.AddStationToPlaylistDialog
import com.onlyradio.radioplayer.ui.viewmodels.PixabayViewModel
import com.onlyradio.radioplayer.utils.Constants.NO_PLAYLIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_API
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_LAZY_LIST
import com.onlyradio.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.onlyradio.radioplayer.utils.Utils
import com.onlyradio.radioplayer.utils.addAction
import com.onlyradio.radioplayer.utils.toRadioStation
import com.google.android.material.snackbar.Snackbar
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.extensions.snackbarSimple
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailsFragment : BaseFragment<FragmentStationDetailsBinding>(
    FragmentStationDetailsBinding::inflate
)
{

     private val pixabayViewModel: PixabayViewModel by lazy {
         ViewModelProvider(requireActivity())[PixabayViewModel::class.java]
    }

    lateinit var viewPagerAdapter : ViewPagerStationsAdapter

    private var listOfPlaylists : List<Playlist> = emptyList()

    @Inject
    lateinit var glide : RequestManager

    private var currentRadioStation : RadioStation? = null

    private var isFavoured = false

    private var isInitialLaunch = true

    private var songTitle = ""

    private var isToTogglePlayStation = true

    private var isFavStateObserverSet = false

    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }

    private var isViewPagerCallbackSet = false
    private var isPagerTransAnimSet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RadioService.isInStationDetails = true

        observeCurrentSongTitle()

        observeIfNewStationFavoured()

        updateListOfPlaylists()

        setAddToPlaylistClickListener()

//        setHomePageClickListener()

        addToFavClickListener()

        setupRecordingButton()

        observeExoRecordState()

        setTitleCopy()

        setBookmarkClickListener()

        setupPagerView()

        getCurrentPlaylistItems()

    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity)
    }


    private fun getCurrentPlaylistItems(){


        var listName = ""

        val list = when(RadioService.currentMediaItems){
            SEARCH_FROM_API -> {
                listName = resources.getString(R.string.from_search)

                mainViewModel.radioSource.stationsFromApi.map {
                    it.toRadioStation()
                }
            }


            SEARCH_FROM_FAVOURITES -> {

                listName = resources.getString(R.string.from_fav)

                if(favViewModel.isStationFavoured.value == false){
                    RadioService.currentPlayingStation.value?.let {
                        listOf(it)
                    } ?: emptyList()
                }
                else
                mainViewModel.radioSource.stationsFavoured

            }

            SEARCH_FROM_PLAYLIST -> {
                listName = "\"${RadioService.currentPlaylistName}\""
                RadioSource.stationsInPlaylist
            }


            SEARCH_FROM_LAZY_LIST -> {
                listName = resources.getString(R.string.lazy_playlist_name)
                RadioSource.lazyListStations
            }


            SEARCH_FROM_HISTORY -> {
                listName = resources.getString(R.string.from_history)

                mainViewModel.radioSource.stationsFromHistory
            }

            SEARCH_FROM_HISTORY_ONE_DATE -> {

//              val calendar = Calendar.getInstance()
//              calendar.time = Date(historyViewModel.selectedDate)

                listName = Utils.convertLongToOnlyDate(historyViewModel.selectedDate, DateFormat.MEDIUM)


                RadioSource.stationsFromHistoryOneDate
            }

            NO_PLAYLIST -> {

                listName = resources.getString(R.string.in_fall_out)
                RadioService.currentPlayingStation.value?.let {
                    listOf(it)
                } ?: emptyList()

            }
            else -> emptyList()

       }

        bind.tvPlaylistTitle.text = listName
        viewPagerAdapter.listOfStations = list

//        var position = 0
//
//
//        RadioService.currentPlayingStation.value?.let { station ->
//
//            if(list.size <= RadioService.currentPlayingItemPosition){
//                viewPagerAdapter.listOfStations = listOf(station)
//                mainViewModel.clearMediaItems()
//
//            } else if(station.stationuuid != list[RadioService.currentPlayingItemPosition].stationuuid){
//                viewPagerAdapter.listOfStations = listOf(station)
//                mainViewModel.clearMediaItems()
//            }
//            else {
//                position = RadioService.currentPlayingItemPosition
//                viewPagerAdapter.listOfStations = list
//            }
//        }

        bind.viewPager.apply {

            doOnLayout {
                setCurrentItem(mainViewModel.getPlayerCurrentIndex(), false)
                observePlayingRadioStation()
                bind.viewPager.registerOnPageChangeCallback(pageChangeCallback)

                doOnLayout {
                    setPageTransformer(PagerZoomOutSlideTransformer())
                    reduceDragSensitivity(2)
                }
            }
        }
    }



    private val pageChangeCallback = object: ViewPager2.OnPageChangeCallback(){


        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            when(state){

                SCROLL_STATE_DRAGGING -> {

                    bind.ivSwipeLeft.apply {
                        if(isVisible){
                            visibility = View.INVISIBLE
                            slideAnim(250, 0, R.anim.fade_out_anim)
                        }
                    }

                    bind.ivSwipeRight.apply {
                        if(isVisible){
                            visibility = View.INVISIBLE
                            slideAnim(250, 0, R.anim.fade_out_anim)
                        }
                    }
                }
                SCROLL_STATE_IDLE -> {
                    handleSwipeIconsVisibility()
                }
            }
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            if(isToTogglePlayStation){
                val newStation = viewPagerAdapter.listOfStations[position]

                val playWhenReady = mainViewModel.playbackState.value?.isPlaying ?: true

                mainViewModel.playOrToggleStation(
                    stationId = newStation.stationuuid, searchFlag =  RadioService.currentMediaItems,
                    playWhenReady = playWhenReady, itemIndex = position,
                    isToChangeMediaItems = false
                )
            } else {
                isToTogglePlayStation = true
            }
        }
    }





    private fun setupPagerView(){

        viewPagerAdapter = ViewPagerStationsAdapter(
            glide = glide,
            languagesTitle = requireContext().resources.getString(R.string.languages_title),
            tagsTitle = requireContext().resources.getString(R.string.tags_title),
        ){
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            try {
                startActivity(webIntent)
            } catch (e : Exception){
                val clip = ClipData.newPlainText("label", it)
                clipBoard?.setPrimaryClip(clip)
                requireContext().makeToast(R.string.no_browser_error_plus_clipboard)
            }
        }

        bind.viewPager.apply {
            adapter = viewPagerAdapter
            orientation = ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
        }
    }

    private fun handleSwipeIconsVisibility(){

        bind.ivSwipeLeft.apply {

            if(bind.viewPager.currentItem != 0 &&
                    viewPagerAdapter.listOfStations.size != 1
                    ){
                if(!isVisible){
                    visibility = View.VISIBLE
                    slideAnim(300, 100, R.anim.fade_in_anim)

                }
            }
        }

        bind.ivSwipeRight.apply {
            if(viewPagerAdapter.listOfStations.size != 1 &&
                bind.viewPager.currentItem < viewPagerAdapter.listOfStations.size - 1
            ){
                if(!isVisible){
                    visibility = View.VISIBLE
                    slideAnim(300, 100, R.anim.fade_in_anim)
                }
            }
        }
    }


    private fun observePlayingRadioStation(){

        RadioService.currentPlayingStation.observe(viewLifecycleOwner){ station ->

            currentRadioStation = station


//            favViewModel.getRadioStationPlayDuration(station.stationuuid){ dur ->
//                bind.tvDuration.text = "${dur/ 1000}s"
//            }



            checkIfStationFavoured(station)
            bind.viewPager.apply {

                if(station.stationuuid != viewPagerAdapter.listOfStations[currentItem].stationuuid){
                    if(viewPagerAdapter.listOfStations.size == 1){
                        viewPagerAdapter.listOfStations = listOf(station)
                    } else {

                        isToTogglePlayStation = false
                        setCurrentItem(mainViewModel.getPlayerCurrentIndex(), true)
                    }
                }
            }
            handleSwipeIconsVisibility()

        }
    }



    private fun observeCurrentSongTitle(){

        mainViewModel.currentSongTitle.observe(viewLifecycleOwner){ title ->


            if(title.isBlank()){

                bind.tvSongTitle.text = resources.getString(R.string.playing_no_info)

                bind.tvSongTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.default_text_color))

                bind.ivCopy.visibility = View.INVISIBLE
                bind.ivBookmark.visibility = View.INVISIBLE



            } else {

                songTitle = title

                bind.tvSongTitle.text = title
                bind.tvSongTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.selected_text_color))

                bind.ivCopy.visibility = View.VISIBLE
                bind.ivBookmark.visibility = View.VISIBLE

            }
        }
    }



    private fun setBookmarkClickListener(){

        bind.ivBookmark.setOnClickListener {

            val name = currentRadioStation?.name ?: ""
            val iconUri = currentRadioStation?.favicon ?: ""

         historyViewModel.upsertBookmarkedTitle(
             Title(
                 System.currentTimeMillis(),
                 RadioService.currentDateLong,
                 songTitle,
                 name,
                 iconUri,
                 true
             )
         )

            requireContext().makeToast(R.string.title_bookmarked)

        }
    }


    private fun setTitleCopy(){
        bind.ivCopy.setOnClickListener {

            bind.ivCopy.isPressed = true

            val clip = ClipData.newPlainText("label", songTitle)
            clipBoard?.setPrimaryClip(clip)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootLayout),
                    resources.getString(R.string.title_copied),
                    Snackbar.LENGTH_LONG).apply {
                    setAction("WEBSEARCH"){
                        val intent = Intent(Intent.ACTION_WEB_SEARCH)
                        intent.putExtra(SearchManager.QUERY, songTitle)
                        try {
                            startActivity(intent)
                        } catch (e : Exception){
                            requireContext().makeToast(R.string.no_browser_error)
                        }
                    }
                addAction(R.layout.snackbar_extra_action, "YOUTUBE"){
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage("com.google.android.youtube")
                    intent.putExtra(SearchManager.QUERY, songTitle)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        startActivity(intent)
                    } catch (e : Exception){
                        val webIntent = Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/results?search_query=$songTitle"))
                        try {
                            startActivity(webIntent)
                        } catch (e : Exception){
                            requireContext().makeToast(R.string.no_browser_error)
                        }
                    }
                }
            }.show()

        }
    }


    private fun setupRecordingButton(){

        setFabRecordListener()

    }

    private var isRecording = false

    private var durationOfRecording = ""

    private fun observeExoRecordState(){

        recordingsViewModel.exoRecordState.observe(viewLifecycleOwner){

            if(it){
                 isRecording = true
                 bind.tvTimer.visibility = View.VISIBLE
                 bind.fabRecording.setImageResource(R.drawable.ic_stop_recording)
            } else {
                durationOfRecording = bind.tvTimer.text.toString()
                bind.fabRecording.setImageResource(R.drawable.ic_start_recording)
                bind.tvTimer.text = requireContext().resources.getString(R.string.rec_saved)
                isRecording = false

            }
        }


        recordingsViewModel.exoRecordTimer.observe(viewLifecycleOwner){ time ->
            if(isRecording)
                bind.tvTimer.text = time as String
        }
    }

    private fun setFabRecordListener(){
        bind.fabRecording.setOnClickListener{
            if(isRecording){
                recordingsViewModel.stopRecording()
            } else {
                recordingsViewModel.startRecording()
            }
        }
    }


    private fun observeIfNewStationFavoured(){

        favViewModel.isStationFavoured.observe(viewLifecycleOwner){

            paintButtonAddToFav(it)

            isFavoured = it

        }
    }


    private fun checkIfStationFavoured(station: RadioStation){
        favViewModel.checkIfStationIsFavoured(station.stationuuid)
    }


    private fun paintButtonAddToFav(isInDB : Boolean){
        if(!isInDB){
            bind.fabAddToFav.setImageResource(R.drawable.ic_add_to_fav)

        } else {
            bind.fabAddToFav.setImageResource(R.drawable.ic_added_to_fav)

        }
    }



    private fun updateListOfPlaylists(){

        favViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            listOfPlaylists = it
        }
    }



    private fun addToPlaylistLogic(){

        AddStationToPlaylistDialog(
            requireContext(), listOfPlaylists, favViewModel, pixabayViewModel, glide,
            resources.getString(R.string.add_to_playlist_title)
        ) { playlistName ->
            insertStationInPlaylist(playlistName)
        }.show()

    }


    private fun setAddToPlaylistClickListener(){

        bind.tvAddToPlaylist?.setOnClickListener {

          addToPlaylistLogic()

        } ?: kotlin.run {

            bind.btnAddToPlaylist?.setOnClickListener {

             addToPlaylistLogic()
            }
        }
    }


    private fun addToFavClickListener(){

        bind.fabAddToFav.setOnClickListener {

            if(isFavoured) {

                currentRadioStation?.let {
                    favViewModel.updateIsFavouredState(0, it.stationuuid)
                    requireActivity().snackbarSimple(R.string.removed_from_favs)
//                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout),
//                        resources.getString(R.string.removed_from_favs), Snackbar.LENGTH_SHORT).show()
                    favViewModel.isStationFavoured.postValue(false)
                }

            } else {
                currentRadioStation?.let {
                    favViewModel.updateIsFavouredState(System.currentTimeMillis(), it.stationuuid)
                    requireActivity().snackbarSimple(R.string.saved_to_favs)
//                    Snackbar.make(requireActivity().findViewById(R.id.rootLayout),
//                        resources.getString(R.string.saved_to_favs), Snackbar.LENGTH_SHORT).show()
                    favViewModel.isStationFavoured.postValue(true)
                }
            }
        }
    }



    private fun insertStationInPlaylist(playlistName : String){

        currentRadioStation?.let { station ->

            favViewModel.checkAndInsertStationPlaylistCrossRef(
                station.stationuuid, playlistName
            ) {

                val message = if(it) resources.getString(R.string.already_in_playlist) + " " + playlistName
                               else resources.getString(R.string.added_to_playlist) + " " + playlistName

                requireActivity().snackbarSimple(message)

//                Snackbar.make((activity as MainActivity).findViewById(R.id.rootLayout),
//                        message, Snackbar.LENGTH_SHORT).show()

            }
        }
    }


    private fun callFavPlaylistUpdateIfNeeded(){
        if(RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES)
            favViewModel.updateFavPlaylist()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        bind.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        bind.viewPager.adapter = null
        _bind = null
        isViewPagerCallbackSet = false
        isInitialLaunch = false
        isFavStateObserverSet = false
        RadioService.isInStationDetails = false
        callFavPlaylistUpdateIfNeeded()
        isPagerTransAnimSet = false
        if(RadioService.currentMediaItems == SEARCH_FROM_FAVOURITES && !isFavoured){
            RadioService.currentMediaItems = NO_PLAYLIST
            favViewModel.clearMediaItems()
        }

    }
}