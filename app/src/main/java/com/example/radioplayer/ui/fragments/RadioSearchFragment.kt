package com.example.radioplayer.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE
import android.util.Log
import android.view.DragEvent
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.AlphaFadeOutAnim
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.dialogs.*
import com.example.radioplayer.utils.*
import com.example.radioplayer.utils.Constants.SEARCH_FROM_API
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class RadioSearchFragment : BaseFragment<FragmentRadioSearchBinding>(
    FragmentRadioSearchBinding::inflate
) {


    private var isNewSearchForAnimations = true

    private var isInitialLaunch = true

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter


    companion object {

        var tagAdapterPosition : Parcelable? = null
        var countriesAdapterPosition : Parcelable? = null


        val tagsList : ArrayList<TagWithGenre> by lazy { ArrayList<TagWithGenre>().apply {


                add(TagWithGenre.Genre(TAG_BY_PERIOD))
                addAll(tagsListByPeriod)

                add(TagWithGenre.Genre(TAG_BY_SPECIAL))
                addAll(tagsListSpecial)

                add(TagWithGenre.Genre(TAG_BY_GENRE))
                addAll(tagsListByGenre)

                add(TagWithGenre.Genre(TAG_BY_SUB_GENRE))
                addAll(tagsListBySubGenre)

                add(TagWithGenre.Genre(TAG_BY_CLASSIC))
                addAll(tagsListClassics)

                add(TagWithGenre.Genre(TAG_BY_MINDFUL))
                addAll(tagsListMindful)

                add(TagWithGenre.Genre(TAG_BY_EXPERIMENTAL))
                addAll(tagsListExperimental)

                add(TagWithGenre.Genre(TAG_BY_TALK))
                addAll(tagsListByTalk)

                add(TagWithGenre.Genre(TAG_BY_RELIGION))
                addAll(tagsListReligion)

                add(TagWithGenre.Genre(TAG_BY_ORIGIN))
                addAll(tagsListByOrigin)

                add(TagWithGenre.Genre(TAG_BY_OTHER))
                addAll(tagsListOther)
            }
        }
    }


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

        setToolbar()


        setSearchParamsFabClickListener()
    }

    private fun setSearchParamsFabClickListener(){

        bind.fabSearchOrder.setOnClickListener {
            SearchParamsDialog(requireContext(), mainViewModel).show()
        }

    }



    private fun setToolbar(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){
            bind.viewToolbar.setBackgroundResource(R.drawable.toolbar_search_vector)
            val color = ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)
//            val statusBar = ContextCompat.getColor(requireContext(), R.color.status_bar_search_fragment)

            (activity as MainActivity).apply {
                window.navigationBarColor = color
                window.statusBarColor = color
            }

//            (bind.tvTag as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_tag)
//                )
//                setStrokeWidth(3.5f)
//            }
//
//            (bind.tvName as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_name)
//                )
//                setStrokeWidth(3.5f)
//            }
//
//            (bind.tvSelectedCountry as TextViewOutlined).apply {
//                setColors(
//                    ContextCompat.getColor(requireContext(), R.color.text_button_search_country)
//                )
//                setStrokeWidth(3.5f)
//            }

//            bind.tvSeparatorFirst.visibility = View.GONE
//            bind.tvSeparatorSecond.visibility = View.GONE


        }

        else {
            bind.viewToolbar.setBackgroundColor(Color.BLACK)
        }
    }


    private fun observeNoResultDetector(){

        mainViewModel.noResultDetection.observe(viewLifecycleOwner){noResult ->

            if(noResult){

                val tagExact = if(mainViewModel.isTagExact) "(Exact)" else ""
                val nameExact = if(mainViewModel.isNameExact) "(Exact)" else ""

                val tag = if(mainViewModel.lastSearchTag.isBlank()) ""
                else "tag $tagExact: ${mainViewModel.lastSearchTag}\n\n"

                val name = if(mainViewModel.lastSearchName.isBlank()) ""
                else "name $nameExact: ${mainViewModel.lastSearchName}\n\n"

                val country = if(mainViewModel.searchFullCountryName.isBlank()) ""
                else "country: ${mainViewModel.searchFullCountryName}\n\n"

                val language = if(!mainViewModel.isSearchFilterLanguage) ""
                else "Language: ${Locale.getDefault().displayLanguage}\n\n"

                val bitrateMin = if(mainViewModel.minBitrateOld == BITRATE_0) ""
                else "Min bitrate: ${mainViewModel.minBitrateOld} kbps\n\n"

                val bitrateMax = if(mainViewModel.maxBitrateOld == BITRATE_MAX) ""
                else "Max bitrate: ${mainViewModel.maxBitrateOld} kbps"

                val message = "No results for\n\n\n$tag$name$country$language$bitrateMin$bitrateMax"

                bind.tvResultMessage.text = message


            }   else bind.tvResultMessage.visibility = View.INVISIBLE
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
        bind.fabInitiateSearch.doOnLayout {
            if (mainViewModel.isFabMoved || mainViewModel.isFabUpdated) {
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

            Log.d("CHECKTAGS", it.url.toString())



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

                defaultSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.default_secondary_text_color)
                selectedSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.selected_secondary_text_color)

                alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10

                separatorDefault = ContextCompat.getColor(requireContext(), R.color.station_bottom_separator_default)

            }

            itemAnimator = null

            layoutAnimation = (activity as MainActivity).layoutAnimationController


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

            (activity as MainActivity).startSeparatorsLoadAnim()

            }

            else {

                (activity as MainActivity).endSeparatorsLoadAnim()


                if(isNewSearchForAnimations ){

                    bind.rvSearchStations.apply {

                        if(isInitialLaunch){
                            scheduleLayoutAnimation()
                            isInitialLaunch = false

                        } else {
//                            alpha = 1f
                            scrollToPosition(0)
                            startLayoutAnimation()


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

//                if(!isInitialLaunch){
//                    launchRecyclerOutAnim()
//                }

                if(!isInitialLaunch){
                    showLoadingResultsMessage()
                }



                pagingRadioAdapter.submitData(it)

            }
        }
    }


//    private val alphaAnim = AlphaFadeOutAnim(1f, 100)
//
//    private fun launchRecyclerOutAnim(){
//
//        if(!bind.tvResultMessage.isVisible){
//
//            bind.rvSearchStations.apply {
//
//                alphaAnim.startAnim(this)
//
//            }
//        }
//    }

    private fun showLoadingResultsMessage(){
        bind.tvResultMessage.apply {
            visibility = View.VISIBLE
            text = "Waiting for response from servers..."
            slideAnim(100, 0, R.anim.fade_in_anim)
        }
    }


    private fun setSearchToolbar() {

        bind.tvTag.setOnClickListener {
           TagPickerDialog(requireContext(), mainViewModel).show()
        }



        bind.tvName.setOnClickListener {

            NameDialog(requireContext(), mainViewModel).show()

        }

        bind.tvSelectedCountry.setOnClickListener {

            CountryPickerDialog(requireContext(), mainViewModel).show()

        }
    }


    private fun setOnRefreshSearch(){

        bind.swipeRefresh.setOnRefreshListener {

           val check = mainViewModel.initiateNewSearch()

            bind.swipeRefresh.isRefreshing = false

            clearAdapter(check)
        }
    }

    private fun listenSearchButton(){

        bind.fabInitiateSearch.setOnClickListener {
          val check =  mainViewModel.initiateNewSearch()
            clearAdapter(check)
        }
    }

    private fun clearAdapter(check : Boolean){
        if(check){
            lifecycleScope.launch {
                pagingRadioAdapter.submitData(PagingData.empty())
            }
        }
    }


    private fun setSearchParamsObservers(){

        mainViewModel.searchParamTag.observe(viewLifecycleOwner){

            (bind.tvTag as TextView).text  = if (it == "") "Tag" else it

        }

        mainViewModel.searchParamName.observe(viewLifecycleOwner){
            (bind.tvName as TextView).text = if (it == "") "Name" else it
        }

        mainViewModel.searchParamCountry.observe(viewLifecycleOwner){


            (bind.tvSelectedCountry as TextView).text = if (it == "") "Country" else it

//            if(it.isBlank()){
//
//                bind.tvSelectedCountry.visibility = View.INVISIBLE
//                bind.ivCountry.visibility = View.VISIBLE
//            } else {
//                bind.tvSelectedCountry.text = it
//                bind.tvSelectedCountry.visibility = View.VISIBLE
//                bind.ivCountry.visibility = View.INVISIBLE
//
//            }


        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvSearchStations.adapter = null
        _bind = null
        isInitialLaunch = true

    }

}





