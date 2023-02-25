package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.CountryPickerDialog
import com.example.radioplayer.ui.dialogs.NameDialog
import com.example.radioplayer.ui.dialogs.TagPickerDialog
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import com.example.radioplayer.utils.listOfTags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private val allTags = listOfTags

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter

    private var checkInitialLaunch = true
    private var isNewSearch = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setSearchParamsObservers()

        setSearchToolbar()

        setRecycleView()

        observePlaybackState()

        setAdapterLoadStateListener()

        setAdapterOnClickListener()

        setOnRefreshSearch()

        listenSearchButton()

        subscribeToStationsFlow()

        setDragListenerForLayout()
        setDragListenerForButton()
        getFabSearchPositionIfNeeded()

//        setLayoutAnimationController()
//
//        setRecyclerChildrenAttachListener()


//
        observeRecyclerAnimations()
    }

    private fun setLayoutAnimationController (){

        bind.rvSearchStations.layoutAnimation = (activity as MainActivity).layoutAnimationController

    }

    private var isNewSearchForAnimations = true

    private fun setRecyclerChildrenAttachListener(){

        bind.rvSearchStations.addOnChildAttachStateChangeListener(object :RecyclerView.OnChildAttachStateChangeListener{

            override fun onChildViewAttachedToWindow(view: View) {

                if(isNewSearchForAnimations){

                    bind.rvSearchStations.apply {

                    Log.d("CHECKTAGS", "onChil: ${System.currentTimeMillis()}")
                        post {
                            Log.d("CHECKTAGS", "onPost: ${System.currentTimeMillis()}")
                            startLayoutAnimation()
                            scrollToPosition(0)

                        }
                    }

                }

                isNewSearchForAnimations = false
                    mainViewModel.isSearchAnimationToPlay = false
            }

            override fun onChildViewDetachedFromWindow(view: View) {

            }
        })

    }

    private fun observeRecyclerAnimations(){

        bind.rvSearchStations.layoutAnimationListener = object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                    Log.d("CHECKTAGS", "animSt: ${System.currentTimeMillis()}")
            }

            override fun onAnimationEnd(animation: Animation?) {

                Log.d("CHECKTAGS", "animEd: ${System.currentTimeMillis()}")
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
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


                if(isNewSearch){
//                 handleScrollUpOnNewSearch()
                 isNewSearch = false
                }
            }
        }
    }

    private fun handleScrollUpOnNewSearch() = viewLifecycleOwner.lifecycleScope.launch {


        delay(100)
        bind.rvSearchStations.smoothScrollToPosition(0)
    }



    private fun subscribeToStationsFlow(){

        viewLifecycleOwner.lifecycleScope.launch {

            mainViewModel.stationsFlow.collectLatest {

                isNewSearchForAnimations = true

                    if(checkInitialLaunch){
                        checkInitialLaunch = false
                    } else {
                        isNewSearch = true
                    }

                Log.d("CHECKTAGS", "onSubm: ${System.currentTimeMillis()}")
                pagingRadioAdapter.submitData(it)

            }
        }
    }


    private fun setSearchToolbar() {


        bind.tvTag.setOnClickListener {

          TagPickerDialog(requireContext(), allTags, mainViewModel).apply {
              show()

          }
        }


        bind.tvName.setOnClickListener {

            NameDialog(requireContext(), it as TextView, mainViewModel).show()

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel).show()

        }

    }


    private fun setOnRefreshSearch(){

        bind.swipeRefresh.setOnRefreshListener {

            initiateNewSearch()

            bind.swipeRefresh.isRefreshing = false

        }
    }

    private fun listenSearchButton(){


        bind.fabInitiateSearch.setOnClickListener {
            initiateNewSearch()
        }

    }


    private fun initiateNewSearch(){

        val name = bind.tvName.text.toString()

        val tag = bind.tvTag.text.toString()

        val country = bind.tvSelectedCountry.text.toString()

        val bundle = Bundle().apply {


            if(tag == "Tag") {
                putString("TAG", "")
            } else {
                putString("TAG", tag)
            }

           if(country == "Country"){
               putString("COUNTRY", "")
           } else {
               putString("COUNTRY", country)
           }

            if(name == "Name"){
                putString("NAME", "")
            } else {
                putString("NAME", name)
            }

        }
        mainViewModel.isNewSearch = true
       val check = mainViewModel.setSearchBy(bundle)

//
//        launchRecyclerOutAnim(check)


    }

//    private fun launchRecyclerOutAnim(isValid : Boolean){
//        if(isValid){
//            bind.rvSearchStations.apply {
//                layoutAnimation = layoutAnimationControllerOut
//                startLayoutAnimation()
//
//            }
//        }
//
//
//    }


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
        checkInitialLaunch = true
        bind.rvSearchStations.adapter = null
        _bind = null
    }

}





