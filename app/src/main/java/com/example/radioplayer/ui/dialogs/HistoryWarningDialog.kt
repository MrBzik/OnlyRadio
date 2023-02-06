package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.databinding.DialogHistoryWarningBinding

class HistoryWarningDialog (
    private val requireContext : Context,
    private val handleAccept : () -> Unit
    )
    : AppCompatDialog(requireContext) {

    lateinit var bind : DialogHistoryWarningBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 300)

        bind = DialogHistoryWarningBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)


        bind.tvAccept.setOnClickListener {

            handleAccept()
            dismiss()
        }


        bind.tvBack.setOnClickListener {

            dismiss()
        }


    }




}