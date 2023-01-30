package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.example.radioplayer.R
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogCreatePlaylistBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel

class CreatePlaylistDialog (
   private val requireContext : Context,
   var listOfPlaylists : List<Playlist>,
   private val databaseViewModel: DatabaseViewModel

) : AppCompatDialog(requireContext) {

    lateinit var bind : DialogCreatePlaylistBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogCreatePlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)


        bind.tvBack.setOnClickListener {
            dismiss()
        }

        bind.etPlaylistName.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(listOfPlaylists.contains(Playlist(bind.etPlaylistName.text.toString()))){
                    bind.etPlaylistName.setTextColor(Color.RED)
                } else{
                    bind.etPlaylistName.setTextColor(requireContext.getColor(R.color.EXPANDtext))
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        bind.tvAccept.setOnClickListener {

            val nameField = bind.etPlaylistName.text.toString()

            if(nameField.isEmpty()) {
                Toast.makeText(requireContext, "Name is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(listOfPlaylists.contains(Playlist(nameField))) {
                Toast.makeText(requireContext, "Name already taken", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else

                databaseViewModel.insertNewPlayList(Playlist(nameField))
                bind.etPlaylistName.text.clear()
                dismiss()
        }


    }

}