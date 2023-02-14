package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
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
import com.example.radioplayer.databinding.DialogEditPlaylistBinding

import com.example.radioplayer.ui.viewmodels.DatabaseViewModel
import com.example.radioplayer.ui.viewmodels.PixabayViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject


@ExperimentalCoroutinesApi
@FlowPreview
class EditPlaylistDialog (
    private val requireContext : Context,
    var listOfPlaylists : List<Playlist>,
    private var currentPlaylistName : String,
    private var currentPlaylistCover : ImageView,
    private val databaseViewModel: DatabaseViewModel,
    private val pixabayViewModel: PixabayViewModel,
    private val glide : RequestManager,
    private val deletePlaylist : () -> Unit

) : AppCompatDialog(requireContext) {

    lateinit var bind : DialogEditPlaylistBinding

    lateinit var imageAdapter : PagingPixabayAdapter

    private var imageSelected = ""

    lateinit var listOfPlaylistNames : List<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogEditPlaylistBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

        listOfPlaylistNames = listOfPlaylists.map{
            it.playlistName
        }

        setTitleAndEditTextField()

        setupRecycleView()

        nameTextChangeTextListener()

        acceptButtonClickListener()

        searchImagesTextChangeListener()

        observeImagesForAdapter()

        setAdapterItemClickListener()

        setOnDeleteClickListener()

        bind.tvBack.setOnClickListener {
            bind.rvImages.adapter = null
            dismiss()
        }
    }

    private fun setOnDeleteClickListener(){

        bind.tvDelete.setOnClickListener {
            RemovePlaylistDialog(requireContext, currentPlaylistName){
                deletePlaylist()
            }.show()
            bind.rvImages.adapter = null
            dismiss()
        }
    }

    private fun setTitleAndEditTextField(){

        bind.tvNameOfPlaylist.text = "\"$currentPlaylistName\""
        bind.etPlaylistName.setText(currentPlaylistName)
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

                if(listOfPlaylistNames.contains(bind.etPlaylistName.text.toString()) &&
                bind.etPlaylistName.text.toString() != currentPlaylistName
                ){
                    bind.etPlaylistName.setTextColor(Color.RED)
                } else{
                    bind.etPlaylistName.setTextColor(requireContext.getColor(R.color.color_non_interactive))
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun acceptButtonClickListener(){

        bind.tvEdit.setOnClickListener {

            var nameField = bind.etPlaylistName.text.toString()

            if(listOfPlaylistNames.contains(nameField) &&
                nameField != currentPlaylistName
            ) {
                Toast.makeText(requireContext, "Name already taken", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            else {

                if (imageSelected.isEmpty()){ /*DO NOTHING*/ } else {
                    databaseViewModel.editPlaylistCover(currentPlaylistName, imageSelected)
                }
                if (nameField == currentPlaylistName || nameField.isEmpty()) {
                   glide.load(imageSelected).into(currentPlaylistCover)

                } else {
                    databaseViewModel.editOldCrossRefWithPlaylist(currentPlaylistName, nameField)
                    databaseViewModel.editPlaylistName(currentPlaylistName, nameField)
                    databaseViewModel.currentPlaylistName.postValue(nameField)
                }
                bind.rvImages.adapter = null
                dismiss()

            }
        }
    }

    private fun setupRecycleView(){

        imageAdapter = PagingPixabayAdapter(glide)

        bind.rvImages.apply {

            adapter = imageAdapter
            layoutManager = GridLayoutManager(requireContext, 3)
            setHasFixedSize(true)

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