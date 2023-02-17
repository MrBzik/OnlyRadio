package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.databinding.DialogHistoryWarningBinding

class HistoryWarningDialog (
    private val requireContext : Context,
    private val handleAccept : () -> Unit
    )
    : AppCompatDialog(requireContext) {

    private var _bind : DialogHistoryWarningBinding? = null
    private val bind get() = _bind!!


    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogHistoryWarningBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.TOP)

        bind.tvAccept.setOnClickListener {

            handleAccept()
            _bind = null
            dismiss()
        }


        bind.tvBack.setOnClickListener {
            _bind = null
            dismiss()
        }


    }




}