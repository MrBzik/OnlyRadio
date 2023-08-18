package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.databinding.DialogRenameRecordingBinding

class RenameRecordingDialog (
    private val requireContext : Context,
    private val oldName : String,
    private val handleResult : (String) -> Unit

) : BaseDialog<DialogRenameRecordingBinding>
    (requireContext, DialogRenameRecordingBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bind.etNewName.setText(oldName)

        bind.etNewName.requestFocus()

        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        bind.tvBack.setOnClickListener {

            dismiss()
        }


        bind.tvAccept.setOnClickListener {

            val newName = bind.etNewName.text.toString()

            if(newName.isBlank()){
                Toast.makeText(requireContext,
                    requireContext.resources.getString(R.string.playlist_name_empty),
                    Toast.LENGTH_SHORT).show()
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