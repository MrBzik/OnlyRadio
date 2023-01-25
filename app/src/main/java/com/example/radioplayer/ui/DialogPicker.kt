package com.example.radioplayer.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.databinding.PickTagDialogBinding

class DialogPicker (
    private val requireContext : Context,
    private val listOfItems : List<String>,
    private val textView : TextView
    )
    : AppCompatDialog(requireContext) {

    lateinit var bind : PickTagDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = PickTagDialogBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        val arrayAdapter = ArrayAdapter(requireContext, android.R.layout.simple_list_item_1, listOfItems)

        bind.listView.adapter = arrayAdapter

        bind.editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                arrayAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        bind.listView.setOnItemClickListener { parent, view, position, id ->

            textView.text = parent.getItemAtPosition(position) as String

            dismiss()

        }

        bind.tvClearSelection.setOnClickListener{
            dismiss()
        }

    }




}