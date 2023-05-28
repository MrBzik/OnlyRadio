package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.example.radioplayer.databinding.DialogPickNameBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class NameDialog (
    requireContext : Context,
    private val mainViewModel: MainViewModel,
    private val handleNewParams : () -> Unit
    )
    : BaseDialog<DialogPickNameBinding>(
    requireContext,
    DialogPickNameBinding::inflate
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setEditText()

        setButtonsClickListeners()

        setSwitchMatchExactListener()


    }

    private fun setSwitchMatchExactListener(){

        bind.switchMatchExact.isChecked = mainViewModel.isNameExact

        bind.switchMatchExact.setOnCheckedChangeListener { _, isChecked ->

            mainViewModel.isNameExact = isChecked
        }
    }


    private fun setButtonsClickListeners(){

        bind.tvClearName.setOnClickListener {

            handleNewParams()

            mainViewModel.searchParamName.postValue("")

            dismiss()
        }


        bind.tvAccept.setOnClickListener {

            val newName = bind.etNewName.text.toString()

                handleNewParams()
                mainViewModel.searchParamName.postValue(newName)

            dismiss()
        }

    }

    private fun setEditText(){

        bind.etNewName.setText(mainViewModel.searchParamName.value)

        bind.etNewName.requestFocus()

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onStop() {
        super.onStop()
        _bind = null

    }

}