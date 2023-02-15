package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.R
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.viewmodels.MainViewModel

class TagPickerDialog (
    private val requireContext : Context,
    private val listOfItems : List<String>,
    private val mainViewModel: MainViewModel
    )
    : AppCompatDialog(requireContext) {

    lateinit var bind : DialogPickTagBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogPickTagBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)


        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)


        val arrayAdapter = ArrayAdapter(requireContext, R.layout.item_text, listOfItems)

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

            val newValue = parent.getItemAtPosition(position) as String

            mainViewModel.searchParamTag.postValue(newValue)

            dismiss()


        }

        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamTag.postValue("")

            dismiss()

        }

        bind.tvBack.setOnClickListener {

            dismiss()

        }


    }
}