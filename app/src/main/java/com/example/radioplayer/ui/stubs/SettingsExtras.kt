package com.example.radioplayer.ui.stubs

import com.example.radioplayer.R
import com.example.radioplayer.databinding.StubSettingsExtrasBinding
import com.example.radioplayer.exoPlayer.RadioService
import com.example.radioplayer.ui.viewmodels.SettingsViewModel

class SettingsExtras() {


    private lateinit var bindExtras : StubSettingsExtrasBinding
    private lateinit var settingsViewModel: SettingsViewModel

    fun setFields(viewModel: SettingsViewModel){
        settingsViewModel = viewModel
    }

    fun updateBinding(bind : StubSettingsExtrasBinding){
        bindExtras = bind
    }


    fun setExtrasLogic(){

        setPlaybackSpeedButtons()
        setLinkClickListener()
        setReverbClickListeners()
        setVirtualizerClickListeners()
    }



    private fun setPlaybackSpeedButtons(){

        updatePlaybackSpeedDisplayValue()
        updatePlaybackPitchDisplayValue()
        updateLinkIcon()

        bindExtras.fabSpeedMinus.setOnClickListener {

            if(RadioService.playbackSpeedRadio > 10){
                RadioService.playbackSpeedRadio -= 10
                settingsViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }

        }

        bindExtras.fabSpeedPlus.setOnClickListener {
            if(RadioService.playbackSpeedRadio < 200){
                RadioService.playbackSpeedRadio += 10
                settingsViewModel.updateRadioPlaybackSpeed()
                updatePlaybackSpeedDisplayValue()
            }
        }

        bindExtras.fabPitchMinus.setOnClickListener {

            if(RadioService.playbackPitchRadio > 10){
                RadioService.playbackPitchRadio -= 10
                settingsViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }

        bindExtras.fabPitchPlus.setOnClickListener {
            if(RadioService.playbackPitchRadio < 200){
                RadioService.playbackPitchRadio += 10
                settingsViewModel.updateRadioPlaybackPitch()
                updatePlaybackPitchDisplayValue()
            }
        }
    }


    private fun updatePlaybackSpeedDisplayValue(){

        bindExtras.tvPlaybackSpeedValue.text = " ${RadioService.playbackSpeedRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bindExtras.tvPlaybackPitchValue.text = " ${RadioService.playbackSpeedRadio}%"

    }

    private fun updatePlaybackPitchDisplayValue(){

        bindExtras.tvPlaybackPitchValue.text = " ${RadioService.playbackPitchRadio}%"
        if(RadioService.isSpeedPitchLinked)
            bindExtras.tvPlaybackSpeedValue.text = " ${RadioService.playbackPitchRadio}%"

    }

    private fun updateLinkIcon(){

        bindExtras.ivLink.apply {
            if(RadioService.isSpeedPitchLinked)
                setImageResource(R.drawable.link)
            else setImageResource(R.drawable.link_off)
        }
    }

    private fun setLinkClickListener(){
        bindExtras.ivLink.setOnClickListener {
            RadioService.isSpeedPitchLinked = !RadioService.isSpeedPitchLinked
            updateLinkIcon()
        }
    }

    private fun setReverbClickListeners(){

        setReverbName()

        bindExtras.fabPrevReverb.setOnClickListener {

            if(RadioService.reverbMode == 0){
                RadioService.reverbMode = 6
            } else {
                RadioService.reverbMode -= 1
            }
            setReverbName()

            settingsViewModel.changeReverbMode()
        }

        bindExtras.fabNextReverb.setOnClickListener {

            if(RadioService.reverbMode == 6){
                RadioService.reverbMode = 0
            } else {
                RadioService.reverbMode += 1
            }
            setReverbName()

            settingsViewModel.changeReverbMode()

        }
    }

    private fun setReverbName(){

        bindExtras.tvReverbValue.text = when(RadioService.reverbMode){
            0 -> "Reverb: none"
            1 -> "Large hall"
            2 -> "Medium hall"
            3 -> "Large room"
            4 -> "Medium room"
            5 -> "Small room"
            else -> "Plate"
        }
    }


    private fun setVirtualizerClickListeners(){

        bindExtras.switchWideRange.apply{
            isChecked = RadioService.isVirtualizerEnabled
            setOnCheckedChangeListener { _, isChecked ->

                RadioService.isVirtualizerEnabled = isChecked
                settingsViewModel.changeVirtualizerLevel()
            }
        }
    }


}