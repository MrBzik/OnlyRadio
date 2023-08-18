package com.onlyradio.radioplayer.ui.stubs

import android.view.View
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.StubNoResultMessageBinding
import com.onlyradio.radioplayer.ui.animations.slideAnim
import com.onlyradio.radioplayer.ui.dialogs.BITRATE_0
import com.onlyradio.radioplayer.ui.dialogs.BITRATE_MAX
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import java.util.Locale

class NoResultMessage (
   private val initiateNewSearch : () -> Unit,
   private val postInitiateNewSearch : () -> Unit
        ) {

     var isNoResultClick = false

     var isNoResultClickLogicSet = false


     fun generateMessage(bind : StubNoResultMessageBinding, mainViewModel : MainViewModel){
        bind.apply {

            if(mainViewModel.lastSearchTag.isBlank()){
                llTag.visibility = View.GONE
            } else {
                llTag.visibility = View.VISIBLE
                val tagExact = if(mainViewModel.isTagExact) "(${bind.root.resources.getString(R.string.exact)})" else ""
                tvTag.text = "${mainViewModel.lastSearchTag} $tagExact"
            }

            if(mainViewModel.lastSearchName.isBlank()){
                llName.visibility = View.GONE
            } else {
                llName.visibility = View.VISIBLE
                val nameExact = if(mainViewModel.isNameExact) "(${bind.root.resources.getString(R.string.exact)})" else ""
                tvName.text = "${mainViewModel.lastSearchName} $nameExact"
            }

            if(mainViewModel.searchFullCountryName.isBlank()){
                llCountry.visibility = View.GONE
            } else {
                llCountry.visibility = View.VISIBLE
                tvCountry.text = mainViewModel.searchFullCountryName
            }

            if(mainViewModel.isSearchFilterLanguage){
                llLanguage.visibility = View.VISIBLE
                tvLanguage.text = Locale.getDefault().displayLanguage
            } else {
                llLanguage.visibility = View.GONE
            }

            if(mainViewModel.minBitrateOld == BITRATE_0){
                llBitrateMin.visibility = View.GONE
            } else {
                llBitrateMin.visibility = View.VISIBLE
                tvBitrateMin.text = "${mainViewModel.minBitrateOld} kbps"
            }

            if(mainViewModel.maxBitrateOld == BITRATE_MAX){
                llBitrateMax.visibility = View.GONE
            } else {
                llBitrateMax.visibility = View.VISIBLE
                tvBitrateMax.text = "${mainViewModel.maxBitrateOld} kbps"
            }

            llRootLayout.visibility = View.VISIBLE
            llRootLayout.slideAnim(350, 0, R.anim.fade_in_anim)
        }


        if(!isNoResultClickLogicSet) setNoResultClickLogic(bind, mainViewModel)
    }


    private fun setNoResultClickLogic(bind : StubNoResultMessageBinding, mainViewModel: MainViewModel){

        isNoResultClickLogicSet = true

        bind.apply {

            llTag.setOnClickListener {
                if(mainViewModel.isFullAutoSearch) postInitiateNewSearch()

                mainViewModel.searchParamTag.postValue("")
                llTag.visibility = View.INVISIBLE
            }

            llName.setOnClickListener {
                if(mainViewModel.isFullAutoSearch) postInitiateNewSearch()

                isNoResultClick = true
                mainViewModel.searchParamName.postValue("")
                llName.visibility = View.INVISIBLE
            }

            llCountry.setOnClickListener {
                if(mainViewModel.isFullAutoSearch) postInitiateNewSearch()
                mainViewModel.searchParamCountry.postValue("")
                mainViewModel.searchFullCountryName = ""
                llCountry.visibility = View.INVISIBLE
            }

            llLanguage.setOnClickListener {
                llLanguage.visibility = View.INVISIBLE
                mainViewModel.isSearchFilterLanguage = false

                initiateNewSearch()
            }

            llBitrateMin.setOnClickListener {
                llBitrateMin.visibility = View.INVISIBLE
                mainViewModel.minBitrateNew = BITRATE_0

                initiateNewSearch()
            }

            llBitrateMax.setOnClickListener {
                llBitrateMax.visibility = View.INVISIBLE
                mainViewModel.maxBitrateNew = BITRATE_MAX

                initiateNewSearch()
            }
        }
    }
}