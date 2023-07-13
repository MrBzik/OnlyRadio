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
import com.example.radioplayer.databinding.FragmentPlayerBinding
import com.example.radioplayer.databinding.FragmentSettingsBinding
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.delegates.SystemBars
import com.example.radioplayer.ui.delegates.SystemBarsImp
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

    val searchDialogsViewModels : SearchDialogsViewModel by viewModels()

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