package com.example.radioplayer.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.example.radioplayer.R
import com.example.radioplayer.adapters.BaseAdapter
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.HistoryViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.ui.viewmodels.RecordingsViewModel
import com.example.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.example.radioplayer.ui.viewmodels.SettingsViewModel
import com.example.radioplayer.utils.Constants

abstract class BaseFragment<VB: ViewBinding>(
        private val bindingInflater : (inflater : LayoutInflater) -> VB
)
    : Fragment() {


     var _bind : VB? = null

     val bind : VB
     get() = _bind!!

    val favViewModel : DatabaseViewModel by viewModels()
    val historyViewModel : HistoryViewModel by viewModels()
    val searchDialogsViewModels : SearchDialogsViewModel by viewModels()

    val mainViewModel: MainViewModel by lazy {
        (activity as MainActivity).mainViewModel
    }

    val recordingsViewModel : RecordingsViewModel by lazy {
        (activity as MainActivity).recordingsViewModel
    }

    val settingsViewModel : SettingsViewModel by lazy {
        (activity as MainActivity).settingsViewModel
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _bind = bindingInflater(inflater)

        ViewCompat.setTransitionName(bind.root, bind.javaClass.name)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSystemBarsColor()
    }

    fun setAdapterValues(utils : BaseAdapter){

        utils.apply {

            defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
            selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)

            defaultSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.default_secondary_text_color)
            selectedSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.selected_secondary_text_color)
            alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
            titleSize = settingsViewModel.stationsTitleSize

        }
    }

    private fun setSystemBarsColor(){

        if(MainActivity.uiMode == Configuration.UI_MODE_NIGHT_NO){

            var isToHandle = true

            val color = when(mainViewModel.currentFragment){

                Constants.FRAG_SEARCH -> ContextCompat.getColor(requireContext(), R.color.nav_bar_search_fragment)
                Constants.FRAG_FAV -> ContextCompat.getColor(requireContext(), R.color.nav_bar_fav_fragment)
                Constants.FRAG_HISTORY -> ContextCompat.getColor(requireContext(), R.color.nav_bar_history_frag)
                Constants.FRAG_REC -> ContextCompat.getColor(requireContext(), R.color.nav_bar_rec_frag)
                else -> {
                    isToHandle = false
                    0
                }
            }

            if(isToHandle){
                (activity as MainActivity).apply {
                    window.navigationBarColor = color
                    window.statusBarColor = color
                }
            }

        }
    }
}