package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import com.example.radioplayer.databinding.DialogPickNameAutoBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class NameAutoDialog (
    requireContext : Context,
    private val mainViewModel: MainViewModel,
    private val initiateSearch : () -> Unit
    ) : BaseDialog<DialogPickNameAutoBinding>(
    requireContext,
    DialogPickNameAutoBinding::inflate
    ) {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setEditText()

        setEditTextChangedListener()

        setSwitchMatchExactListener()

        removeDim()

        removeTopPadding()

    }

    private fun setSwitchMatchExactListener(){

        bind.switchMatchExact.isChecked = mainViewModel.isNameExact

        bind.switchMatchExact.setOnCheckedChangeListener { _, isChecked ->

            mainViewModel.isNameExact = isChecked

            val newName = bind.etNewName.text.toString()

            if(newName.isNotBlank()){
                initiateSearch()
            }
        }
    }


    private var handler = Handler(Looper.getMainLooper())

    private val runnable = kotlinx.coroutines.Runnable {

        initiateSearch()

//        newName?.let { editable ->
//
//        }
    }


//    private var newName : Editable? = null

    private fun setEditTextChangedListener(){

        bind.etNewName.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(newName: Editable?) {
                handler.removeCallbacks(runnable)

//                newName = s

                newName?.let{ name ->

                    mainViewModel.searchParamName.postValue(name.toString())
                    handler.postDelayed(runnable, 1000)


//                    if(newName.isBlank() || newName.length > 2){
//                    }
                }
            }
        })
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