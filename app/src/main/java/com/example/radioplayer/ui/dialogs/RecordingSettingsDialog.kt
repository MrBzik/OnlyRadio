package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle

import android.view.Gravity
import android.view.WindowManager
import android.widget.ArrayAdapter

import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.SelectingOptionAdapter

import com.example.radioplayer.databinding.DialogRecordingSettingsBinding

import com.example.radioplayer.utils.Constants.RECORDING_QUALITY_PREF



class RecordingSettingsDialog (
    private val listOfOptions : List<String>,
    private val recordingQualityPref : SharedPreferences,
    private val requireContext : Context,
    private val updateTvValue : () -> Unit
    )
    : BaseDialog<DialogRecordingSettingsBinding>
    (requireContext, DialogRecordingSettingsBinding::inflate) {

    private var newOption : Int? = null


    private lateinit var optionsAdapter : SelectingOptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRecyclerView()

        setAdapterClicklListener()

        setButtonsListeners()

        adjustDialogHeight(bind.clRecordingSettingsDialog)

    }


    private fun setButtonsListeners(){

        bind.tvAccept.setOnClickListener {

            newOption?.let {

                val newValue = (it+1).toFloat() /10
                recordingQualityPref.edit().putFloat(RECORDING_QUALITY_PREF, newValue).commit()
                updateTvValue()
            }

            dismiss()
        }

        bind.tvBack.setOnClickListener {

            dismiss()
        }

    }

    private fun setAdapterClicklListener(){

        optionsAdapter.setOnItemClickListener { newOption ->

            this.newOption = newOption
            setTvQualityText(newOption)
        }

    }

    private fun setupRecyclerView(){

        optionsAdapter = SelectingOptionAdapter(listOfOptions)
        val recQuality = recordingQualityPref.getFloat(RECORDING_QUALITY_PREF, 0.4f)
        val toInt = (recQuality*10).toInt() -1
        setTvQualityText(toInt)
        optionsAdapter.currentOption = toInt

        bind.rvOptions.apply {
            adapter = optionsAdapter
            layoutManager = LinearLayoutManager(requireContext)
            setHasFixedSize(true)
        }
    }


    private fun setTvQualityText(value : Int)  {

        when(value){
            0 -> {
                bind.tvQualitySetting.text = "(1 minute ~ 400kb)"

            }

            1  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 600kb)"

            }

            2  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 800kb)"

            }

            3  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 1mb)"

            }

            4  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 1.2mb)"

            }

            5  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 1.5mb)"

            }

            6  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 1.7mb)"

            }

            7  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 2mb)"

            }

            8  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 2.3mb)"

            }

           9  -> {
                bind.tvQualitySetting.text = "(1 minute ~ 2.5mb)"

            }
        }
    }



    override fun onStop() {
        super.onStop()
        bind.rvOptions.adapter = null
        _bind = null
    }

}