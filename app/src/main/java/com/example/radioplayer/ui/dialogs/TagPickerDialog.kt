package com.example.radioplayer.ui.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.FilterTagsAdapter
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.animations.slideAnim
import com.example.radioplayer.ui.fragments.RadioSearchFragment
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.example.radioplayer.utils.*
import com.example.radioplayer.utils.KeyboardObserver.observeKeyboardState

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
        observeKeyboardState(bind.root, {
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

        })
    }



    private fun setupRecyclerView(){

        tagAdapter = FilterTagsAdapter()
        tagAdapter.apply {
            submitList(searchDialogsViewModel.tagsList)
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

                if(tag.isOpened) {

                    removeTagsSubList(tag.genre, position)

                } else {

                    addTagsSublist(tag.genre, position)
                }


            } else if(tag is TagWithGenre.Tag) {

                handleNewParams()

                mainViewModel.searchParamTag.postValue(tag.tag)

                dismiss()

            }
        }
    }


    private fun addTagsSublist(genre : String, position: Int){

        val listToAdd = getList(genre)

        searchDialogsViewModel.tagsList.addAll(position+1, listToAdd)

        (searchDialogsViewModel.tagsList[position] as TagWithGenre.Genre).isOpened = true
        tagAdapter.submitList(searchDialogsViewModel.tagsList)
        tagAdapter.notifyItemRangeInserted(position+1, listToAdd.size)

    }

    private fun removeTagsSubList(genre : String, position : Int)  {

        val listToRemove = getList(genre)

        searchDialogsViewModel.tagsList.removeAll(listToRemove)

        (searchDialogsViewModel.tagsList[position] as TagWithGenre.Genre).isOpened = false
        tagAdapter.submitList(searchDialogsViewModel.tagsList)
        tagAdapter.notifyItemRangeRemoved(position+1, listToRemove.size)

    }


    private fun getList(genre : String) : Set<TagWithGenre.Tag> {
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