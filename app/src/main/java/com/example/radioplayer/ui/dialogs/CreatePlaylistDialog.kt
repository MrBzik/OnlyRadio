package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
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

    lateinit var imageSelected : String

    lateinit var listOfPlaylistNames : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogCreatePlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        listOfPlaylistNames = listOfPlaylists.map{
            it.playlistName
        }

        setupRecycleView()

        nameTextChangeTextListener()

        acceptButtonClickListener()

        searchImagesTextChangeListener()

        observeImagesForAdapter()

        setAdapterItemClickListener()

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

                if(listOfPlaylistNames.contains(bind.etPlaylistName.text.toString())){
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
            else if(listOfPlaylistNames.contains(nameField)) {
                Toast.makeText(requireContext, "Name already taken", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if (bind.ivSelectedImage.drawable == null){
                Toast.makeText(requireContext, "No image selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            else

                databaseViewModel.insertNewPlayList(Playlist(nameField, imageSelected))
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

        setAdapterLoadStateListener()
    }

    private fun setAdapterLoadStateListener(){

        imageAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)
                bind.progressBar.isVisible = true


            else {
                bind.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setAdapterItemClickListener(){

        imageAdapter.setOnClickListener {

            glide.load(it.previewURL).into(bind.ivSelectedImage)

            imageSelected = it.previewURL

        }


    }

}