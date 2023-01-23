package com.example.radioplayer.ui.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.RadioStation
import com.example.radioplayer.databinding.FragmentStationDetailsBinding
import com.example.radioplayer.exoPlayer.toRadioStation
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants.SPLIT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StationDetailsFragment : Fragment()
{
    lateinit var bind : FragmentStationDetailsBinding

    lateinit var mainViewModel: MainViewModel
    lateinit var databaseViewModel: DatabaseViewModel

    private var homepageUrl : String? = null

    @Inject
    lateinit var glide : RequestManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(
            this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_stationDetailsFragment_to_radioSearchFragment2)
                    (activity as MainActivity).fabAddToFav.visibility = View.GONE
                    (activity as MainActivity).tvExpandHide.setText(R.string.Expand)
                }
            })
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentStationDetailsBinding.inflate(inflater, container, false)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)


        mainViewModel = (activity as MainActivity).mainViewModel
        databaseViewModel = (activity as MainActivity).databaseViewModel


        mainViewModel.newRadioStation.observe(viewLifecycleOwner){





        }




        mainViewModel.currentRadioStation.observe(viewLifecycleOwner){

            var homeTagsLang = ""

            it?.description?.apply {

                bind.tvName.text = title
                bind.tvCountry.text = subtitle
                glide.load(iconUri).into(bind.ivIcon)
                homeTagsLang = description.toString()

            }

              val array =  homeTagsLang.split(SPLIT)

              homepageUrl = array[0]

              val tags = array[1].replace(",", ", ")

              val language = array[2]

                  bind.tvTags.text = "Tags : $tags"

                  bind.tvLanguage.text = "Language : $language"

        }

        bind.fabStationHomePage.setOnClickListener {

            if(homepageUrl != "null") {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(homepageUrl))
                startActivity(webIntent)

            }

        }



    }


}