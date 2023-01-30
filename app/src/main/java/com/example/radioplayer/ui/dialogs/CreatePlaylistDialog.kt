package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingPixabayAdapter
import com.example.radioplayer.data.local.entities.Playlist
import com.example.radioplayer.databinding.DialogCreatePlaylistBinding
import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class CreatePlaylistDialog (
   private val requireContext : Context,
   var listOfPlaylists : List<Playlist>,
   private val databaseViewModel: DatabaseViewModel,
   private val pixabayViewModel: PixabayViewModel,
   private val glide : RequestManager

) : AppCompatDialog(requireContext) {

    lateinit var bind : DialogCreatePlaylistBinding

    lateinit var imageAdapter : PagingPixabayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogCreatePlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        setupRecycleView()

        nameTextChangeTextListener()

        acceptButtonClickListener()

        searchImagesTextChangeListener()

        observeImagesForAdapter()

        bind.tvBack.setOnClickListener {
            dismiss()
        }
    }


    private fun observeImagesForAdapter(){

        lifecycleScope.launch {
            pixabayViewModel.imagesFlow.collectLatest {
                imageAdapter.submitData(it)
            }
        }


    }

    private fun searchImagesTextChangeListener(){

        bind.etSearchQuery.addTextChangedListener {

            pixabayViewModel.setImageSearch(it.toString())

        }

    }



    private fun nameTextChangeTextListener(){

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

    }

    private fun acceptButtonClickListener(){

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
            bind.etPlaylistName.text?.clear()
            dismiss()
        }


    }

    private fun setupRecycleView(){

        imageAdapter = PagingPixabayAdapter(glide)

        bind.rvImages.apply {

            adapter = imageAdapter
            layoutManager = GridLayoutManager(requireContext, 3)

        }


    }

}