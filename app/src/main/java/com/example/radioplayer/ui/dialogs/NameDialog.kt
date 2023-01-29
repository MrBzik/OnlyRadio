package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
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


        bind.tvTitle.text = if(textView.text == "Name") "Name of radiostation"
                            else "Name of radiostation (${textView.text})"



        bind.tvClearName.setOnClickListener {

            mainViewModel.searchParamName.postValue("Name")

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