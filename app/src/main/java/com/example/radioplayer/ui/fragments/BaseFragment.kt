package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.radioplayer.R
import com.example.radioplayer.adapters.BaseAdapter
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel

abstract class BaseFragment<VB: ViewBinding>(
        private val bindingInflater : (inflater : LayoutInflater) -> VB
)
    : Fragment() {

     var _bind : VB? = null

     val bind : VB
     get() = _bind!!

    val databaseViewModel : DatabaseViewModel by lazy {
        (activity as MainActivity).databaseViewModel
    }
    val mainViewModel: MainViewModel by lazy {
        (activity as MainActivity).mainViewModel
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

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//    }



    fun setAdapterValues(utils : BaseAdapter){

        utils.apply {

            defaultTextColor = ContextCompat.getColor(requireContext(), R.color.default_text_color)
            selectedTextColor = ContextCompat.getColor(requireContext(), R.color.selected_text_color)

            defaultSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.default_secondary_text_color)
            selectedSecondaryTextColor = ContextCompat.getColor(requireContext(), R.color.selected_secondary_text_color)
            alpha = requireContext().resources.getInteger(R.integer.radio_text_placeholder_alpha).toFloat()/10
            titleSize = mainViewModel.stationsTitleSize

        }
    }


}