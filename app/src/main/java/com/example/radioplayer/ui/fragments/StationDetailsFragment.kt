package com.example.radioplayer.ui.fragments

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.*
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.ViewPagerStationsAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.data.local.relations.StationPlaylistCrossRef
import com.example.radioplayer.databinding.FragmentStationDetailsBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.exoPlayer.RadioSource
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.reduceDragSensitivity
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.AddStationToPlaylistDialog
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import com.example.radioplayer.utils.Constants.FRAG_FAV
import com.example.radioplayer.utils.Constants.FRAG_HISTORY
import com.example.radioplayer.utils.Constants.FRAG_REC
import com.example.radioplayer.utils.Constants.FRAG_SEARCH
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.Constants.SEARCH_FROM_FAVOURITES
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY_ONE_DATE
import com.example.radioplayer.utils.Constants.SEARCH_FROM_PLAYLIST
import com.example.radioplayer.utils.Constants.TITLE_UNKNOWN
import com.example.radioplayer.utils.RandomColors
import com.example.radioplayer.utils.Utils
import com.example.radioplayer.utils.addAction
import com.example.radioplayer.utils.toRadioStation
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailsFragment : BaseFragment<FragmentStationDetailsBinding>(
    FragmentStationDetailsBinding::inflate
)
{

    lateinit var pixabayViewModel: PixabayViewModel

    lateinit var viewPagerAdapter : ViewPagerStationsAdapter

    private var homepageUrl : String? = null

    private var listOfPlaylists : List<Playlist> = emptyList()

    @Inject
    lateinit var glide : RequestManager

    private var currentRadioStation : RadioStation? = null

    private var isFavoured = false

    private var songTitle = ""

    private val clipBoard : ClipboardManager? by lazy {
        ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
    }


    private var isViewPagerCallbackSet = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pixabayViewModel = ViewModelProvider(requireActivity())[PixabayViewModel::class.java]


        observeIfNewStationFavoured()

        observeCurrentSongTitle()

        updateListOfPlaylists()

        setAddToPlaylistClickListener()

//        setHomePageClickListener()

        addToFavClickListener()

        setupRecordingButton()

        observeExoRecordState()

        setTitleCopy()

        setupPagerView()

        getCurrentPlaylistItems()


        setSystemBarsColor()

    }


    private fun setSystemBarsColor(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){

            val color = when(mainViewModel.currentFragment){

                FRAG_SEARCH -> ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)
                FRAG_FAV -> ContextCompat.getColor(requireContext(), R.color.nav_bar_fav_fragment)
                FRAG_HISTORY -> ContextCompat.getColor(requireContext(), R.color.nav_bar_history_frag)
                FRAG_REC -> ContextCompat.getColor(requireContext(), R.color.nav_bar_rec_frag)
                else -> ContextCompat.getColor(requireContext(), R.color.nav_bar_settings_frag)
            }

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }
        }
    }


    private fun getCurrentPlaylistItems(){

        val list = when(RadioService.currentPlaylist){
            SEARCH_FROM_API -> mainViewModel.radioSource.stationsFromApi.map {
                it.toRadioStation()
        }
            SEARCH_FROM_FAVOURITES -> mainViewModel.radioSource.stationsFavoured

            SEARCH_FROM_PLAYLIST -> RadioSource.stationsInPlaylist

            SEARCH_FROM_HISTORY -> mainViewModel.radioSource.stationsFromHistory

            SEARCH_FROM_HISTORY_ONE_DATE -> mainViewModel.radioSource.stationsFromHistoryOneDate

            else -> emptyList()
       }

        viewPagerAdapter.listOfStations = list

        bind.viewPager.apply {
            post {
                setCurrentItem(RadioService.currentPlayingItemPosition, false)
                if(!isViewPagerCallbackSet){
                    isViewPagerCallbackSet = true
                    observePlayingRadioStation()
                    bind.viewPager.registerOnPageChangeCallback(pageChangeCallback)
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

            val newStation = viewPagerAdapter.listOfStations[position]

            val playWhenReady = mainViewModel.playbackState.value?.isPlaying ?: true

//            val historyStationId = if(RadioService.currentPlaylist == SEARCH_FROM_HISTORY)
//                newStation.stationuuid else null

            mainViewModel.playOrToggleStation(
                station =  newStation, searchFlag =  RadioService.currentPlaylist,
                playWhenReady = playWhenReady, itemIndex = position

            )
        }
    }

    private fun setupPagerView(){

        viewPagerAdapter = ViewPagerStationsAdapter(glide){
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
            startActivity(webIntent)
        }

        bind.viewPager.apply {
            adapter = viewPagerAdapter
            orientation = ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
            reduceDragSensitivity(2)

        }
    }

    private fun handleSwipeIconsVisibility(){

        bind.ivSwipeLeft.apply {

            if(RadioService.currentPlayingItemPosition != 0){
                if(!isVisible){
                    visibility = View.VISIBLE
                    slideAnim(300, 100, R.anim.fade_in_anim)

                }
            }
        }

        bind.ivSwipeRight.apply {
            if(RadioService.currentPlayingItemPosition < viewPagerAdapter.listOfStations.size - 1){
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
            checkIfStationFavoured(station)
            bind.viewPager.apply {
                if(currentItem != RadioService.currentPlayingItemPosition){
                    setCurrentItem(RadioService.currentPlayingItemPosition, true)
                }
            }
            handleSwipeIconsVisibility()

//            updateUiForRadioStation(station)
        }
    }



    private fun observeCurrentSongTitle(){

        mainViewModel.currentSongTitle.observe(viewLifecycleOwner){ title ->


            if(title.equals("NULL", ignoreCase = true) || title.isBlank()){
                bind.tvSongTitle.text = TITLE_UNKNOWN

                bind.tvSongTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.default_text_color))

                bind.ivCopy.visibility = View.GONE


            } else {
                bind.tvSongTitle.text = title
                bind.tvSongTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.selected_text_color))

                bind.ivCopy.visibility = View.VISIBLE

            }
        }
    }



    private fun setTitleCopy(){
        bind.llSongTitle.setOnClickListener {

            bind.ivCopy.isPressed = true

            val clip = ClipData.newPlainText("label", songTitle)
            clipBoard?.setPrimaryClip(clip)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootLayout),
                "Title copied", Snackbar.LENGTH_LONG).apply {
                    setAction("WEBSEARCH"){
                        val intent = Intent(Intent.ACTION_WEB_SEARCH)
                        intent.putExtra(SearchManager.QUERY, bind.tvSongTitle.text)
                        startActivity(intent)
                    }
                addAction(R.layout.snackbar_extra_action, "YOUTUBE"){
                    val intent = Intent(Intent.ACTION_SEARCH)
                    intent.setPackage("com.google.android.youtube")
                    intent.putExtra(SearchManager.QUERY, bind.tvSongTitle.text)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
            }.show()

        }
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
                         bind.tvTimer.text = Utils.timerFormat(time)
                     }
                     isTimerObserverSet = true
                 }

                 bind.fabRecording.setImageResource(R.drawable.ic_stop_recording)


            } else {

                durationOfRecording = bind.tvTimer.text.toString()
                bind.tvTimer.text = "Processing..."
                bind.fabRecording.setImageResource(R.drawable.ic_start_recording)

                if(!isConverterCallbackSet){
                    mainViewModel.exoRecordFinishConverting.observe(viewLifecycleOwner){ finished ->

                        if(finished){

                            bind.tvTimer.text = "Saved"
                            isRecording = false

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

    private val randColors = RandomColors()

//    private fun setTvPlaceHolderLetter(name : String,){
//
//        val color = randColors.getColor()
//
//            var char = 'X'
//
//            for(l in name.indices){
//                if(name[l].isLetter()){
//                    char = name[l]
//                    break
//                }
//            }
//
//            bind.tvPlaceholder.apply {
//                text = char.toString().uppercase()
//                setTextColor(color)
//                alpha = 0.6f
//        }
//    }


//    private fun updateUiForRadioStation(station : RadioStation){
//
//            homepageUrl = station.homepage
//
//            bind.tvName.text = station.name
//
//            if(station.favicon.isNullOrBlank()){
//
//                bind.ivIcon.visibility = View.INVISIBLE
//                setTvPlaceHolderLetter(station.name?: "")
//
//            } else {
//
//                glide
//                    .load(station.favicon)
//                    .listener(object : RequestListener<Drawable>{
//                        override fun onLoadFailed(
//                            e: GlideException?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//
//                            bind.ivIcon.visibility = View.INVISIBLE
//                            setTvPlaceHolderLetter(station.name ?: "")
//                            return true
//                        }
//
//                        override fun onResourceReady(
//                            resource: Drawable?,
//                            model: Any?,
//                            target: Target<Drawable>?,
//                            dataSource: DataSource?,
//                            isFirstResource: Boolean
//                        ): Boolean {
//                            return false
//                        }
//                    })
//                    .transition(DrawableTransitionOptions.withCrossFade())
//                    .into(bind.ivIcon)
//
//            }
//
//
//
//            if(!station.language.isNullOrBlank()){
//                bind.tvLanguage.isVisible = true
//                val languages = station.language.replace(",", ", ")
//                bind.tvLanguage.text = "Languages : $languages"
//            }
//            if(!station.tags.isNullOrBlank()){
//                bind.tvTags.isVisible = true
//                val tags = station.tags.replace(",", ", ")
//                bind.tvTags.text = "Tags : $tags"
//            }
//
//    }


    private fun updateListOfPlaylists(){

        databaseViewModel.listOfAllPlaylists.observe(viewLifecycleOwner){

            listOfPlaylists = it
        }
    }


//    private fun setHomePageClickListener(){
//
//        bind.tvHomePage.setOnClickListener {
//
//            if(!homepageUrl.isNullOrBlank()) {
//                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl))
//                startActivity(webIntent)
//            }
//        }
//    }


    private fun addToPlaylistLogic(){

        AddStationToPlaylistDialog(
            requireContext(), listOfPlaylists, databaseViewModel, pixabayViewModel, glide
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
        bind.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        bind.viewPager.adapter = null
        _bind = null
        isViewPagerCallbackSet = false
    }


}