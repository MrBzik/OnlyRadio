package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.R
import com.example.radioplayer.databinding.DialogHistorySettingsBinding
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.Constants.HISTORY_30_DATES
import com.example.radioplayer.utils.Constants.HISTORY_3_DATES
import com.example.radioplayer.utils.Constants.HISTORY_7_DATES
import com.example.radioplayer.utils.Constants.HISTORY_NEVER_CLEAN
import com.example.radioplayer.utils.Constants.HISTORY_ONE_DAY

class HistorySettingsDialog (
    private val requireContext : Context,
    private val handleChoice : (String) -> Unit
    )
    : AppCompatDialog(requireContext) {

    private var _bind : DialogHistorySettingsBinding? = null
    private val bind get() = _bind!!

    private val listOfOptions = listOf(
        HISTORY_ONE_DAY, HISTORY_3_DATES, HISTORY_7_DATES, HISTORY_30_DATES, HISTORY_NEVER_CLEAN
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogHistorySettingsBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        window?.setGravity(Gravity.TOP)

        val arrayAdapter = ArrayAdapter(requireContext, R.layout.item_text, listOfOptions)

        bind.listViewOptions.adapter = arrayAdapter


        bind.listViewOptions.setOnItemClickListener { parent, view, position, id ->

            val newValue = parent.getItemAtPosition(position) as String

            handleChoice(newValue)

            dismiss()

        }

        bind.tvBack.setOnClickListener {

            dismiss()
        }


    }



    override fun onStop() {
        super.onStop()
        bind.listViewOptions.adapter = null
        _bind = null
    }

}