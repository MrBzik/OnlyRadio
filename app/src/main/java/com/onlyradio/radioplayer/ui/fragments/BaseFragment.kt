package com.onlyradio.radioplayer.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.onlyradio.radioplayer.databinding.FragmentSettingsBinding
import com.onlyradio.radioplayer.ui.MainActivity
import com.onlyradio.radioplayer.ui.delegates.SystemBars
import com.onlyradio.radioplayer.ui.delegates.SystemBarsImp
import com.onlyradio.radioplayer.ui.viewmodels.DatabaseViewModel
import com.onlyradio.radioplayer.ui.viewmodels.HistoryViewModel
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.RecordingsViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SettingsViewModel


abstract class BaseFragment<VB: ViewBinding>(
        private val bindingInflater : (inflater : LayoutInflater) -> VB
)
    : Fragment(), SystemBars by SystemBarsImp() {

     var _bind : VB? = null

     val bind : VB
     get() = _bind!!

    val favViewModel : DatabaseViewModel by lazy {
        (activity as MainActivity).favViewModel
    }

    val historyViewModel : HistoryViewModel by lazy {
        (activity as MainActivity).historyViewModel
    }

    val mainViewModel: MainViewModel by lazy {
        (activity as MainActivity).mainViewModel
    }

    val recordingsViewModel : RecordingsViewModel by lazy {
        (activity as MainActivity).recordingsViewModel
    }

    val settingsViewModel : SettingsViewModel by lazy {
        (activity as MainActivity).settingsViewModel
    }

    val searchDialogsViewModels : SearchDialogsViewModel by lazy {
        (activity as MainActivity).searchDialogsViewModel
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

        if(bind !is FragmentSettingsBinding){
            setSystemBarsColor(requireContext(), mainViewModel.currentFragment)
        }
    }
}