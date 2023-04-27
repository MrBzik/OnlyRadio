package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.radioplayer.databinding.DialogPickNameAutoBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class NameAutoDialog (
    requireContext : Context,
    private val mainViewModel: MainViewModel) : BaseDialog<DialogPickNameAutoBinding>(
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
                mainViewModel.searchParamName.postValue(newName)
            }
        }
    }


    private var timer : Timer? = null

    private var handler = Handler(Looper.getMainLooper())

    private val runnable = kotlinx.coroutines.Runnable {
        mainViewModel.searchParamName.postValue(bind.etNewName.text.toString())
    }


    private fun setEditTextChangedListener(){

        bind.etNewName.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
//                        timer?.cancel()
                handler.removeCallbacks(runnable)

                s?.let{ newName ->
                    if(newName.isBlank() || newName.length > 2){

                        handler.postDelayed(runnable, 1000)

//                        timer = Timer()
//                        timer?.schedule(object : TimerTask(){
//                            override fun run() {
//                                mainViewModel.searchParamName.postValue(newName.toString())
//                            }
//                        }, 1000)
                    }
                }
            }
        })
    }



    private fun setEditTextChangeListener(){

        bind.etNewName.addTextChangedListener {

            it?.let {

                val newName = it.toString()

                if(newName.isBlank() || newName.length > 2){

                    lifecycleScope.launch(Dispatchers.IO){

                        delay(1000)

                        mainViewModel.searchParamName.postValue(newName)
                    }
                }
            }
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