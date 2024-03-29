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
import com.onlyradio.radioplayer.databinding.DialogCreatePlaylistBinding
import com.onlyradio.radioplayer.extensions.makeToast
import com.onlyradio.radioplayer.extensions.observeKeyboardState
import com.onlyradio.radioplayer.ui.viewmodels.DatabaseViewModel
import com.onlyradio.radioplayer.ui.viewmodels.PixabayViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


const val NO_IMAGE = "no image"


class CreatePlaylistDialog (
   private val requireContext : Context,
   var listOfPlaylists : List<Playlist>,
   private val databaseViewModel: DatabaseViewModel,
   private val pixabayViewModel: PixabayViewModel,
   private val glide : RequestManager,
   private val insertStationInPlaylist : ((String) -> Unit)?

) : BaseDialog<DialogCreatePlaylistBinding>
    (requireContext, DialogCreatePlaylistBinding::inflate) {

    lateinit var imageAdapter : PagingPixabayAdapter

    private var imageSelected : String = NO_IMAGE

    lateinit var listOfPlaylistNames : List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listOfPlaylistNames = listOfPlaylists.map{
            it.playlistName
        }

        setupRecycleView()

        nameTextChangeTextListener()

        handleKeyboardToggle()
//        setEditTextsFocusChangeListeners()

        acceptButtonClickListener()

        searchImagesTextChangeListener()

        observeImagesForAdapter()

        setAdapterItemClickListener()

        bind.tvBack.setOnClickListener {

            dismiss()
        }

        adjustDialogHeight(bind.clDialogCreatePlaylist)

    }



    private fun handleKeyboardToggle(){

        bind.root.observeKeyboardState(
            {
                bind.tvTitle.visibility = View.GONE
            },{
                bind.tvTitle.visibility = View.VISIBLE
                bind.etSearchQuery.clearFocus()
                bind.etPlaylistName.clearFocus()
            }, {}
        )
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



    private fun nameTextChangeTextListener(){

        bind.etPlaylistName.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(listOfPlaylistNames.contains(bind.etPlaylistName.text.toString())){
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

            if(nameField.isBlank()) {
                requireContext.makeToast(R.string.playlist_name_empty)
                return@setOnClickListener
            }
            else if(listOfPlaylistNames.contains(nameField)) {
                requireContext.makeToast(R.string.playlist_name_taken)
                return@setOnClickListener
            }
            else if (imageSelected == NO_IMAGE){
                requireContext.makeToast(R.string.playlist_no_image)
                return@setOnClickListener
            }


                databaseViewModel.insertNewPlayList(Playlist(nameField, imageSelected))
                insertStationInPlaylist?.let {
                    it(nameField)
                }
            bind.etPlaylistName.text?.clear()

            dismiss()
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