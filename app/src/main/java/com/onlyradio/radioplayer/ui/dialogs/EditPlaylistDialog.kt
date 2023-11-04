package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.PagingPixabayAdapter
import com.onlyradio.radioplayer.data.local.entities.Playlist
import com.onlyradio.radioplayer.databinding.DialogEditPlaylistBinding
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.extensions.observeKeyboardState
import com.onlyradio.radioplayer.ui.viewmodels.DatabaseViewModel
import com.onlyradio.radioplayer.ui.viewmodels.PixabayViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class EditPlaylistDialog (
    private val requireContext : Context,
    var listOfPlaylists : List<Playlist>,
    private var currentPlaylistName : String,
    private var currentPlaylistPosition : Int,
    private val databaseViewModel: DatabaseViewModel,
    private val pixabayViewModel: PixabayViewModel,
    private val glide : RequestManager,
    private val deletePlaylist : (Boolean) -> Unit
) : BaseDialog<DialogEditPlaylistBinding>
    (requireContext, DialogEditPlaylistBinding::inflate) {

    lateinit var imageAdapter : PagingPixabayAdapter

    private var imageSelected = ""

    lateinit var currentPlaylist : Playlist

    lateinit var listOfPlaylistNames : List<String>

//    private var isFocusHandlingNeeded = true
//    private var isFocusRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listOfPlaylistNames = listOfPlaylists.map{
            it.playlistName
        }

        setTitleAndEditTextField()

        setupRecycleView()

        handleKeyboardToggle()
//        setEditTextsFocusChangeListeners()

        editTextChangeTextListener()

        acceptButtonClickListener()

        searchImagesTextChangeListener()

        observeImagesForAdapter()

        setAdapterItemClickListener()

        setOnDeleteClickListener()


        bind.tvBack.setOnClickListener {
            dismiss()
        }

        adjustDialogHeight(bind.clDialogEditPlaylist)

    }



//    private fun setEditTextsFocusChangeListeners(){
//
//        bind.etSearchQuery.setOnFocusChangeListener { v, hasFocus ->
//            if(hasFocus && isFocusHandlingNeeded){
//
//                isFocusRequested = true
//
//            }
//            else if(!hasFocus && isFocusRequested){
//
//                v.requestFocus()
//                isFocusRequested = false
//
//            }
//        }
//
//        bind.etPlaylistName.setOnFocusChangeListener { v, hasFocus ->
//            if(hasFocus && isFocusHandlingNeeded){
//                isFocusRequested = true
//            }
//            else if(!hasFocus && isFocusRequested){
//                v.requestFocus()
//                isFocusRequested = false
//            }
//        }
//
//    }

    private fun handleKeyboardToggle(){

        bind.root.observeKeyboardState(
            {
                bind.tvTitle.visibility = View.GONE
                bind.tvDelete.visibility = View.GONE

//            isFocusHandlingNeeded = false
            },{
                bind.tvTitle.visibility = View.VISIBLE
                bind.tvDelete.visibility = View.VISIBLE

//            isFocusHandlingNeeded = true
                bind.etSearchQuery.clearFocus()
                bind.etPlaylistName.clearFocus()
            }, {}
        )

    }


    private fun setOnDeleteClickListener(){

        bind.tvDelete.setOnClickListener {

            deletePlaylist(true)

            dismiss()
        }
    }

    private fun setTitleAndEditTextField(){


        bind.etPlaylistName.setText(currentPlaylistName)

        currentPlaylist = listOfPlaylists[currentPlaylistPosition]
        glide
            .load(currentPlaylist.coverURI)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(bind.ivSelectedImage)
        imageSelected = currentPlaylist.coverURI
    }


    private fun observeImagesForAdapter(){

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                pixabayViewModel.imagesFlow.collectLatest {
                    imageAdapter.submitData(it)
                }
            }
        }
    }

    private fun searchImagesTextChangeListener(){

        bind.etSearchQuery.addTextChangedListener {

            pixabayViewModel.setImageSearch(it.toString())

        }

    }



    private fun editTextChangeTextListener(){

        bind.etPlaylistName.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(listOfPlaylistNames.contains(bind.etPlaylistName.text.toString()) &&
                bind.etPlaylistName.text.toString() != currentPlaylistName
                ){
                    bind.etPlaylistName.setTextColor(requireContext.getColor(R.color.color_changed_on_interaction))
                } else{
                    bind.etPlaylistName.setTextColor(requireContext.getColor(R.color.search_text_color))
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    private fun acceptButtonClickListener(){

        bind.tvAccept.setOnClickListener {

            val nameField = bind.etPlaylistName.text.toString()

            if(listOfPlaylistNames.contains(nameField) &&
                nameField != currentPlaylistName
            ) {
                requireContext.makeToast(R.string.playlist_name_taken)
                return@setOnClickListener
            }


            else {

                if (imageSelected != currentPlaylist.coverURI)
                    databaseViewModel.editPlaylistCover(currentPlaylistName, imageSelected)

                if (nameField != currentPlaylistName && nameField.isNotBlank()) {
                    databaseViewModel.editOldCrossRefWithPlaylist(currentPlaylistName, nameField)
                    databaseViewModel.editPlaylistName(currentPlaylistName, nameField)
                    databaseViewModel.currentPlaylistName.postValue(nameField)
                }

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

            glide
                .load(it.previewURL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.ivSelectedImage)

            imageSelected = it.previewURL

        }
    }


    override fun onStop() {
        super.onStop()
        bind.rvImages.adapter = null
        _bind = null

    }


}