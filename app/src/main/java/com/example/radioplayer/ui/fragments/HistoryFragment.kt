package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.HistoryDatesAdapter
import com.example.radioplayer.adapters.PagingHistoryAdapter
import com.example.radioplayer.data.local.entities.HistoryDate
import com.example.radioplayer.databinding.FragmentHistoryBinding
import com.example.radioplayer.exoPlayer.isPlayEnabled
import com.example.radioplayer.exoPlayer.isPlaying
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.animations.BounceEdgeEffectFactory
import com.example.radioplayer.utils.Constants.SEARCH_FROM_HISTORY
import com.example.radioplayer.utils.SpinnerExt
import com.example.radioplayer.utils.Utils.fromDateToString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.sql.Date
import java.text.DateFormat
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(
    FragmentHistoryBinding::inflate
) {


    private var currentDate = ""

    private lateinit var datesAdapter : HistoryDatesAdapter

    @Inject
    lateinit var glide : RequestManager

    private val dateFormat = DateFormat.getDateInstance()

    private var isInitialLoad = true

    @Inject
    lateinit var historyAdapter: PagingHistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateCurrentDate()

        setupRecyclerView()

        observePlaybackState()

        setupAdapterClickListener()

        subscribeToHistory()

        setDatesSpinnerSelectListener()

        observeListOfDates()

        setTvSelectDateClickListener()

//        setSpinnerOpenCloseObserver()

    setSpinnerOpenCloseListener()

        setRvChildrenAttachListener()

    }

    private fun setRvChildrenAttachListener(){

        bind.rvHistory.apply {

            addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener{

                override fun onChildViewAttachedToWindow(view: View) {
                    if(isInitialLoad){


                        scheduleLayoutAnimation()

                        isInitialLoad = false

                    }

                }

                override fun onChildViewDetachedFromWindow(view: View) {

                }
            })


        }

    }


    private fun setSpinnerOpenCloseListener(){

        bind.spinnerDates.setSpinnerEventsListener( object : SpinnerExt.OnSpinnerEventsListener{
            override fun onSpinnerOpened(spinner: Spinner?) {
               bind.tvSelectDate.
                   setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playlists_arrow_shrink,0)
            }

            override fun onSpinnerClosed(spinner: Spinner?) {
                bind.tvSelectDate.
                setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_playlists_arrow_expand,0)
            }
        })


    }


    private fun setTvSelectDateClickListener(){

        bind.tvSelectDate.setOnClickListener {

            bind.spinnerDates.performClick()
        }
    }

    private fun setupDatesSpinner(list : List<HistoryDate>){

        datesAdapter = HistoryDatesAdapter(list, requireContext())
        bind.spinnerDates.adapter = datesAdapter
        datesAdapter.selectedColor = ContextCompat.getColor(requireContext(), R.color.color_non_interactive)



    }


    private fun observeListOfDates(){

        databaseViewModel.listOfDates.observe(viewLifecycleOwner){

            val testFull = mutableListOf<HistoryDate>()

            testFull.addAll(it)

            val sublist = listOf(
                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),
//                HistoryDate("18 of March, 2023", System.currentTimeMillis()),

            )

//            testFull.addAll(sublist)

            setupDatesSpinner(testFull)

            val pos = testFull.indexOfFirst { historyDate ->
                historyDate.time == databaseViewModel.selectedDate
            }

            datesAdapter.selectedItemPosition = pos
            setSliderHeaderText(databaseViewModel.selectedDate)

            if(pos == -1)
                bind.spinnerDates.setSelection(0)
            else
                bind.spinnerDates.setSelection(pos+1)
        }
    }


    private fun setDatesSpinnerSelectListener(){

        bind.spinnerDates.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

               val item = datesAdapter.getItem(position) as HistoryDate
                databaseViewModel.selectedDate = item.time
                datesAdapter.selectedItemPosition = position

                setSliderHeaderText(item.time)

                if(position == 0)
                    databaseViewModel.updateHistory.postValue(true)
                 else
                    databaseViewModel.updateHistory.postValue(false)

                isInitialLoad = true

//                bind.rvHistory.apply {
//                    post{
//                        scheduleLayoutAnimation()
//                    }
//                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    private fun setSliderHeaderText(time : Long){
        if(time == 0L) bind.tvSliderHeader.text = "History: All"
        else bind.tvSliderHeader.text = dateFormat.format(time)
    }

    private fun observePlaybackState(){
        mainViewModel.playbackState.observe(viewLifecycleOwner){
            it?.let {

                when{
                    it.isPlaying -> {
                        historyAdapter.currentPlaybackState = true

                            historyAdapter.updateStationPlaybackState()

                    }
                    it.isPlayEnabled -> {
                        historyAdapter.currentPlaybackState = false

                            historyAdapter.updateStationPlaybackState()

                    }
                }
            }
        }
    }


    private fun setupRecyclerView (){


        bind.rvHistory.apply {

            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            edgeEffectFactory = BounceEdgeEffectFactory()

            setHasFixedSize(true)

            itemAnimator = null

            historyAdapter.apply {
                defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
                selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)

                    mainViewModel.currentRadioStation.value?.let {
                        val id =  it.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
                        currentRadioStationID = id
                    }

            }

            layoutAnimation = (activity as MainActivity).layoutAnimationController

//            post {
//                    scheduleLayoutAnimation()
//                }

        }

    }

    private fun subscribeToHistory(){

        viewLifecycleOwner.lifecycleScope.launch{

            databaseViewModel.historyFlow.collectLatest {

                historyAdapter.currentDate = currentDate
                historyAdapter.submitData(it)

            }
        }

    }

    private fun setupAdapterClickListener(){

        historyAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, SEARCH_FROM_HISTORY)

        }
    }




    private fun updateCurrentDate(){

        val time = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.time = Date(time)
        val parsedDate = fromDateToString(calendar)
        currentDate =  parsedDate

    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind.rvHistory.adapter = null
        _bind = null
    }

}