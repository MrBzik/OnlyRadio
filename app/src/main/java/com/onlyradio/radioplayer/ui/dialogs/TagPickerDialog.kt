package com.onlyradio.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.FilterTagsAdapter
import com.onlyradio.radioplayer.adapters.models.serializable.TagWithGenre
import com.onlyradio.radioplayer.databinding.DialogPickTagBinding
import com.onlyradio.radioplayer.extensions.observeKeyboardState
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.onlyradio.radioplayer.utils.TAG_BY_CLASSIC
import com.onlyradio.radioplayer.utils.TAG_BY_EXPERIMENTAL
import com.onlyradio.radioplayer.utils.TAG_BY_GENRE
import com.onlyradio.radioplayer.utils.TAG_BY_MINDFUL
import com.onlyradio.radioplayer.utils.TAG_BY_ORIGIN
import com.onlyradio.radioplayer.utils.TAG_BY_PERIOD
import com.onlyradio.radioplayer.utils.TAG_BY_RELIGION
import com.onlyradio.radioplayer.utils.TAG_BY_SPECIAL
import com.onlyradio.radioplayer.utils.TAG_BY_SUB_GENRE
import com.onlyradio.radioplayer.utils.TAG_BY_TALK
import com.onlyradio.radioplayer.utils.tagsListByGenre
import com.onlyradio.radioplayer.utils.tagsListByOrigin
import com.onlyradio.radioplayer.utils.tagsListByPeriod
import com.onlyradio.radioplayer.utils.tagsListBySubGenre
import com.onlyradio.radioplayer.utils.tagsListByTalk
import com.onlyradio.radioplayer.utils.tagsListClassics
import com.onlyradio.radioplayer.utils.tagsListExperimental
import com.onlyradio.radioplayer.utils.tagsListMindful
import com.onlyradio.radioplayer.utils.tagsListOther
import com.onlyradio.radioplayer.utils.tagsListReligion
import com.onlyradio.radioplayer.utils.tagsListSpecial
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TagPickerDialog (
    private val requireContext : Context,
    private val mainViewModel: MainViewModel,
    private val searchDialogsViewModel: SearchDialogsViewModel,
    private val handleNewParams : () -> Unit
)
    : BaseDialog<DialogPickTagBinding>(
    requireContext,
    DialogPickTagBinding::inflate,

) {

    private lateinit var tagAdapter : FilterTagsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupRecyclerView()
        setupButtons()
        setupAdapterClickListener()
        setEditTextAdapterFilter()
        handleKeyboardToggle()
        setSwitchExactMatch()

        adjustDialogHeight(bind.clTagPickDialog)

        observeTagsFlow()

//        lifecycleScope.launch {
//            delay(4000)
//            searchDialogsViewModel.generateExcludedTags()
//        }

    }


    private fun observeTagsFlow(){

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED){

                searchDialogsViewModel.tagsListFlow.collectLatest {

                    tagAdapter.submitList(it)
                }
            }
        }
    }

    private fun setSwitchExactMatch(){

        bind.switchMatchExact.isChecked = mainViewModel.isTagExact

        tagAdapter.isExactMatch = mainViewModel.isTagExact

        bind.switchMatchExact.setOnCheckedChangeListener { _, isChecked ->

            mainViewModel.isTagExact = isChecked
            tagAdapter.isExactMatch = isChecked
            searchDialogsViewModel.tagAdapterPosition = bind.recyclerView.layoutManager?.onSaveInstanceState()
            bind.recyclerView.adapter = null
            bind.recyclerView.adapter = tagAdapter
            searchDialogsViewModel.tagAdapterPosition?.let {
                bind.recyclerView.layoutManager?.onRestoreInstanceState(it)
            }
        }

    }


    private fun handleKeyboardToggle (){

        bind.root.observeKeyboardState(
            {
                bind.tvTitle.visibility = View.GONE
                bind.tvSwitchSearchExact.visibility = View.GONE
                bind.switchMatchExact.visibility = View.GONE

            }, {
                bind.tvTitle.visibility = View.VISIBLE
                bind.tvSwitchSearchExact.visibility = View.VISIBLE
                bind.switchMatchExact.visibility = View.VISIBLE
                bind.editText.clearFocus()

            }, {
                bind.editText.requestFocus()

            }
        )


//        observeKeyboardState(bind.root, )
    }



    private fun setupRecyclerView(){

        tagAdapter = FilterTagsAdapter()
        tagAdapter.apply {


//            submitList(searchDialogsViewModel.tagsList)
            defaultTextColor = ContextCompat.getColor(requireContext, R.color.unselected_genre_color)
            selectedTextColor = ContextCompat.getColor(requireContext, R.color.selected_genre_color)
            openingDrawable = R.drawable.tags_expand
            closingDrawable = R.drawable.tags_shrink
        }


        bind.recyclerView.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext)
            searchDialogsViewModel.tagAdapterPosition?.let {
                layoutManager?.onRestoreInstanceState(it)
            }
        }



    }


    private fun setEditTextAdapterFilter(){

        bind.editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tagAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun setupAdapterClickListener(){

        tagAdapter.setOnTagClickListener { tag, position ->

            if(tag is TagWithGenre.Genre){

                searchDialogsViewModel.updateTagsFlow(position)

//                if(tag.isOpened) {
//
//                    removeTagsSubList(tag.genre, position)
//
//                } else {
//
//                    addTagsSublist(tag.genre, position)
//                }


            } else if(tag is TagWithGenre.Tag) {

                handleNewParams()

                mainViewModel.searchParamTag.postValue(tag.tag)

                dismiss()

            }
        }
    }



//    private fun addTagsSublist(genre : String, position: Int){
//
//        val listToAdd = getList(genre)
//
//        searchDialogsViewModel.tagsList.addAll(position+1, listToAdd)
//
//        (searchDialogsViewModel.tagsList[position] as TagWithGenre.Genre).isOpened = true
//
//        tagAdapter.submitList(searchDialogsViewModel.tagsList)
//
//
//    }
//
//
//    private fun removeTagsSubList(genre : String, position : Int)  {
//
//        val listToRemove = getList(genre)
//
//        searchDialogsViewModel.tagsList.removeAll(listToRemove)
//        (searchDialogsViewModel.tagsList[position] as TagWithGenre.Genre).isOpened = false
//        tagAdapter.submitList(searchDialogsViewModel.tagsList)
//
//
//    }


    private fun getList(genre : String) : Set<TagWithGenre> {
        return when(genre){
            TAG_BY_PERIOD -> tagsListByPeriod

            TAG_BY_GENRE -> tagsListByGenre

            TAG_BY_SUB_GENRE -> tagsListBySubGenre

            TAG_BY_MINDFUL -> tagsListMindful

            TAG_BY_CLASSIC -> tagsListClassics

            TAG_BY_EXPERIMENTAL -> tagsListExperimental

            TAG_BY_SPECIAL -> tagsListSpecial

            TAG_BY_TALK -> tagsListByTalk

            TAG_BY_RELIGION -> tagsListReligion

            TAG_BY_ORIGIN -> tagsListByOrigin

            else -> tagsListOther
        }
    }



    private fun setupButtons(){
        bind.tvClearSelection.setOnClickListener{

            handleNewParams()

            mainViewModel.searchParamTag.postValue("")

            dismiss()

        }

        bind.tvBack.setOnClickListener {

           dismiss()

        }
    }




    override fun onStop() {
        super.onStop()

        searchDialogsViewModel.tagAdapterPosition = bind.recyclerView.layoutManager?.onSaveInstanceState()

        bind.recyclerView.adapter = null
        _bind = null

    }

    private var isOpen : MutableLiveData<Boolean> = MutableLiveData()

}