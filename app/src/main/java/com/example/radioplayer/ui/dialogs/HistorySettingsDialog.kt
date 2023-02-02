package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.databinding.DialogHistorySettingsBinding
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class HistorySettingsDialog (
    private val requireContext : Context,
    private val handleChoice : (String) -> Unit
    )
    : AppCompatDialog(requireContext) {

    lateinit var bind : DialogHistorySettingsBinding

    private val listOfOptions = listOf(
        "One day",
        "3 dates",
        "7 dates",
        "30 dates",
        "Never clean"
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogHistorySettingsBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        val arrayAdapter = ArrayAdapter(requireContext, android.R.layout.simple_list_item_1, listOfOptions)

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




}