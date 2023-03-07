package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import com.example.radioplayer.databinding.DialogPickNameBinding
import com.example.radioplayer.databinding.DialogRenameRecordingBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class RenameRecordingDialog (
    private val requireContext : Context,
    private val oldName : String,
    private val handleResult : (String) -> Unit

) : AppCompatDialog(requireContext) {


    private var _bind : DialogRenameRecordingBinding? = null
    private val bind get() = _bind!!

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogRenameRecordingBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.TOP)


        bind.etNewName.setText(oldName)

        bind.etNewName.requestFocus()

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)


        bind.tvBack.setOnClickListener {

            dismiss()
        }


        bind.tvAccept.setOnClickListener {

            val newName = bind.etNewName.text.toString()

            if(newName.isBlank()){
               Toast.makeText(requireContext, "Name is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(newName == oldName) {
                dismiss()
            } else {

                handleResult(newName)
                dismiss()
            }
        }
    }


    override fun onStop() {
        super.onStop()
        _bind = null

    }

}