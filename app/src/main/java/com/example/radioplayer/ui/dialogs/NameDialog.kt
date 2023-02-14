package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.example.radioplayer.databinding.DialogPickNameBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class NameDialog (
    requireContext : Context,
    private val textView: TextView,
    private val mainViewModel: MainViewModel

) : AppCompatDialog(requireContext) {

    lateinit var bind : DialogPickNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {



        bind = DialogPickNameBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        bind.tvNameOfRadioStation.apply {
            if(textView.text != "Name") {
                isVisible = true
                text = "\"${textView.text}\""
            }
        }


        bind.etNewName.requestFocus()

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        bind.tvClearName.setOnClickListener {

            mainViewModel.searchParamName.postValue("")

            dismiss()
        }


        bind.tvAccept.setOnClickListener {

            val newName = bind.etNewName.text.toString()
            if(newName == "") {dismiss()}
            else {
                mainViewModel.searchParamName.postValue(newName)
                dismiss()
            }
        }


    }


}