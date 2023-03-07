package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.view.DragEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.RvAlphaAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.CountryPickerDialog
import com.example.radioplayer.ui.dialogs.NameDialog
import com.example.radioplayer.ui.dialogs.TagPickerDialog
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.listOfTags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private val allTags = listOfTags

    private var isNewSearchForAnimations = true

    private var isInitialLaunch = true

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        subscribeToStationsFlow()

        observePlaybackState()

        setAdapterLoadStateListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        listenSearchButton()

        setDragListenerForLayout()
        setDragListenerForButton()
        getFabSearchPositionIfNeeded()

        observeNoResultDetector()

    }

    private fun observeNoResultDetector(){

        mainViewModel.noResultDetection.observe(viewLifecycleOwner){noResult ->


            if(noResult){
                bind.tvResultMessage.apply {
                    val tag = mainViewModel.lastSearchTag.ifBlank { "not selected" }

                    val name = mainViewModel.lastSearchName.ifBlank { "not selected" }

                    val country = mainViewModel.searchFullCountryName.ifBlank { "not selected" }

                    val message = "No results for\n\nname: $name\ntag: $tag\ncountry: $country"
                    text = message

                }
            }   else bind.tvResultMessage.visibility = View.GONE
        }
    }



    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            it?.let {

                when{
                    it.isPlaying -> {
                        pagingRadioAdapter.currentPlaybackState = true

                        pagingRadioAdapter.updateStationPlaybackState()

                    }
                    it.isPlayEnabled -> {
                        pagingRadioAdapter.currentPlaybackState = false

                        pagingRadioAdapter.updateStationPlaybackState()

                    }
                }
            }
        }
    }



    private fun getFabSearchPositionIfNeeded(){
        bind.fabInitiateSearch.post{
            if (mainViewModel.isFabMoved) {
                bind.fabInitiateSearch.x = mainViewModel.fabX
                bind.fabInitiateSearch.y = mainViewModel.fabY
            }
            bind.fabInitiateSearch.isVisible = true
        }
    }

    private fun setDragListenerForLayout(){
            var tempX = 0f
            var tempY = 0f

        bind.root.setOnDragListener { v, event ->
            when(event.action){
                DragEvent.ACTION_DRAG_LOCATION -> {
                   tempX = event.x
                   tempY = event.y
                }
                DragEvent.ACTION_DRAG_ENDED ->{

                    bind.fabInitiateSearch.x = tempX - bind.fabInitiateSearch.width/2
                    bind.fabInitiateSearch.y = tempY - bind.fabInitiateSearch.height/2

                    mainViewModel.fabX = bind.fabInitiateSearch.x
                    mainViewModel.fabY = bind.fabInitiateSearch.y
                    mainViewModel.isFabUpdated = true
                }
            }
            true
        }
    }

    private fun setDragListenerForButton(){
        bind.fabInitiateSearch.setOnLongClickListener { view ->
            val shadow = View.DragShadowBuilder(bind.fabInitiateSearch)
            view.startDragAndDrop(null, shadow, view, 0)
            true
        }
    }


    private fun setAdapterOnClickListener(){

        pagingRadioAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, SEARCH_FROM_API)
            databaseViewModel.insertRadioStation(it)
            databaseViewModel.checkDateAndUpdateHistory(it.stationuuid)

        }
    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            pagingRadioAdapter.apply {
                defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
                selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)
            }

            itemAnimator = null

            layoutAnimation = (activity as MainActivity).layoutAnimationController

//            post {
//                scheduleLayoutAnimation()
//            }

            mainViewModel.currentRadioStation.value?.let {
              val name =  it.getString(METADATA_KEY_TITLE)
                pagingRadioAdapter.currentRadioStationName = name
            }
        }
    }

    private fun setAdapterLoadStateListener(){

        pagingRadioAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)

            {

            (activity as MainActivity).separatorLeftAnim.startLoadingAnim()
            (activity as MainActivity).separatorRightAnim.startLoadingAnim()

            }

            else {

                (activity as MainActivity).separatorLeftAnim.endLoadingAnim()
                (activity as MainActivity).separatorRightAnim.endLoadingAnim()

                if(isNewSearchForAnimations ){

                    bind.rvSearchStations.apply {

                        if(isInitialLaunch){
                            scheduleLayoutAnimation()
                            isInitialLaunch = false

                        } else {

                            post {

                                alpha = 1f
                                scrollToPosition(0)
                                startLayoutAnimation()
                            }
                        }
                    }

                    isNewSearchForAnimations = false
                }
            }
        }
    }



    private fun subscribeToStationsFlow(){

        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.stationsFlow.collectLatest {

                isNewSearchForAnimations = true

                if(!isInitialLaunch){
                    launchRecyclerOutAnim()
                }
                showLoadingResultsMessage()
                pagingRadioAdapter.submitData(it)

            }
        }
    }


    private val alphaAnim = RvAlphaAnim()

    private fun launchRecyclerOutAnim(){

        if(!bind.tvResultMessage.isVisible){

            bind.rvSearchStations.apply {

                alphaAnim.startAnim(this)

            }
        }
    }

    private fun showLoadingResultsMessage(){
        bind.tvResultMessage.apply {
            visibility = View.VISIBLE
            text = "Waiting for response from servers..."
            slideAnim(100, 0, R.anim.fade_in_anim)
        }
    }


    private fun setSearchToolbar() {

        bind.llTag.setOnClickListener {
            bind.tvTag.isPressed = true
            TagPickerDialog(requireContext(), allTags, mainViewModel).show()
        }



        bind.llName.setOnClickListener {
            bind.tvName.isPressed = true
            NameDialog(requireContext(), bind.tvName, mainViewModel).show()

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel).show()

        }

    }


    private fun setOnRefreshSearch(){

        bind.swipeRefresh.setOnRefreshListener {

            mainViewModel.initiateNewSearch()

            bind.swipeRefresh.isRefreshing = false

        }
    }

    private fun listenSearchButton(){

        bind.fabInitiateSearch.setOnClickListener {
            mainViewModel.initiateNewSearch()
        }

    }




    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

             bind.tvTag.text = if (it == "") "Tag" else it

        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){
            bind.tvName.text = if (it == "") "Name" else it
        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){

           bind.tvSelectedCountry.text = if (it == "") "Country" else it
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvSearchStations.adapter = null
        _bind = null
        isInitialLaunch = true

    }

}





