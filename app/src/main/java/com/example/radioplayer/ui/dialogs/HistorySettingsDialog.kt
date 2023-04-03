package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.SelectingOptionAdapter
import com.example.radioplayer.databinding.DialogHistorySettingsBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.utils.Constants.HISTORY_15_DATES
import com.example.radioplayer.utils.Constants.HISTORY_21_DATES
import com.example.radioplayer.utils.Constants.HISTORY_30_DATES
import com.example.radioplayer.utils.Constants.HISTORY_3_DATES
import com.example.radioplayer.utils.Constants.HISTORY_7_DATES
import com.example.radioplayer.utils.Constants.HISTORY_ONE_DAY


class HistorySettingsDialog (
    private val listOfOptions : List<String>,
    private val initialOption : Int,
    private val requireContext : Context,
    private val databaseViewModel: DatabaseViewModel,
    private val handleChoice : (Int) -> Unit
    )
    : BaseDialog<DialogHistorySettingsBinding>
    (requireContext, DialogHistorySettingsBinding::inflate) {


    private lateinit var historyOptionsAdapter : SelectingOptionAdapter

    private var newOption : Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        setupRecyclerView()

        setAdapterClickListener()

        setButtonsClickListeners()

        adjustDialogHeight(bind.clHistorySettingDialog)

    }

    private fun setButtonsClickListeners(){
        bind.tvAccept.setOnClickListener {

            newOption?.let { newOption ->

                if(newOption != initialOption) {
                    val newValue = positionToValue(newOption)
                    handleChoice(newValue)
                    databaseViewModel.setHistoryOptionsPref(newValue)
                    if(initialOption > newOption){
                        databaseViewModel.compareDatesWithPrefAndCLeanIfNeeded(null)
                    }
                }
            }
            dismiss()
        }

        bind.tvBack.setOnClickListener {

            dismiss()
        }


    }

    private fun positionToValue(position : Int) : Int {

       return when(position){

            0 ->  HISTORY_ONE_DAY
            1 -> HISTORY_3_DATES
            2 -> HISTORY_7_DATES
            3 -> HISTORY_15_DATES
            4 -> HISTORY_21_DATES
           else -> HISTORY_30_DATES

        }

    }


    private fun setAdapterClickListener(){

        historyOptionsAdapter.setOnItemClickListener { position ->

            newOption = position

            if(position < initialOption){
                bind.tvWarning.visibility = View.VISIBLE
            } else
                bind.tvWarning.visibility = View.INVISIBLE

        }


    }



    private fun setupRecyclerView(){

        historyOptionsAdapter = SelectingOptionAdapter(listOfOptions)
        historyOptionsAdapter.currentOption = initialOption
        bind.rvListOfOptions.apply {
            adapter = historyOptionsAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }


    override fun onStop() {
        super.onStop()
        bind.rvListOfOptions.adapter = null
        _bind = null
    }

}