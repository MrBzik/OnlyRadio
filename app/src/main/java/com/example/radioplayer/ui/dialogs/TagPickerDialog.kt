package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.FilterTagsAdapter
import com.example.radioplayer.adapters.models.TagWithGenre
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.fragments.RadioSearchFragment
import com.example.radioplayer.ui.fragments.RadioSearchFragment.Companion.tagsList
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.*
import com.example.radioplayer.utils.KeyboardObserver.observeKeyboardState

class TagPickerDialog (
    private val requireContext : Context,
    private val mainViewModel: MainViewModel
    )
    : AppCompatDialog(requireContext) {

    private var _bind : DialogPickTagBinding? = null
    private val bind get() = _bind!!

    private lateinit var tagAdapter : FilterTagsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogPickTagBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        setupRecyclerView()
        setupButtons()
        setupAdapterClickListener()
        setEditTextAdapterFilter()
        handleKeyboardToggle()
        setSwitchExactMatch()

    }

    private fun setSwitchExactMatch(){

        bind.switchMatchExact.isChecked = mainViewModel.isTagExact

        bind.switchMatchExact.setOnCheckedChangeListener { _, isChecked ->

            mainViewModel.isTagExact = isChecked
        }

    }


    private fun handleKeyboardToggle (){
        observeKeyboardState(bind.root, {
            bind.tvBack.visibility = View.GONE
            bind.tvClearSelection.visibility = View.GONE
            bind.tvTitle.visibility = View.GONE
            bind.tvSwitchSearchExact.visibility = View.GONE
            bind.switchMatchExact.visibility = View.GONE

        }, {
            bind.tvBack.visibility = View.VISIBLE
            bind.tvClearSelection.visibility = View.VISIBLE
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
            submitList(tagsList)
            defaultTextColor = ContextCompat.getColor(requireContext, R.color.unselected_genre_color)
            selectedTextColor = ContextCompat.getColor(requireContext, R.color.selected_genre_color)
            openingDrawable = R.drawable.tags_expand
            closingDrawable = R.drawable.tags_shrink
        }


        bind.recyclerView.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext)
            RadioSearchFragment.tagAdapterPosition?.let {
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

                mainViewModel.searchParamTag.postValue(tag.tag)

                dismiss()

            }
        }
    }

    private fun addTagsSublist(genre : String, position: Int){

        var itemCount = 0

        when(genre){
            TAG_BY_PERIOD -> {
                tagsList.addAll(position+1, tagsListByPeriod)
                itemCount = tagsListByPeriod.size
            }
            TAG_BY_GENRE -> {
                tagsList.addAll(position+1, tagsListByGenre)
                itemCount = tagsListByGenre.size

            }
            TAG_BY_MINDFUL -> {
                tagsList.addAll(position+1, tagsListMindful)
                itemCount = tagsListMindful.size

            }
            TAG_BY_CLASSIC -> {
                tagsList.addAll(position+1, tagsListClassics)
                itemCount = tagsListClassics.size
            }

            TAG_BY_EXPERIMENTAL ->{
                tagsList.addAll(position+1, tagsListExperimental)
                itemCount = tagsListExperimental.size
            }

            TAG_BY_SPECIAL ->{
                tagsList.addAll(position+1, tagsListSpecial)
                itemCount = tagsListSpecial.size
            }


            TAG_BY_TALK -> {
                tagsList.addAll(position+1, tagsListByTalk)
                itemCount = tagsListByTalk.size

            }
            TAG_BY_RELIGION -> {
                tagsList.addAll(position+1, tagsListReligion)
                itemCount = tagsListReligion.size

            }
            TAG_BY_ORIGIN -> {
                tagsList.addAll(position+1, tagsListByOrigin)
                itemCount = tagsListByOrigin.size

            }
            TAG_BY_OTHER -> {
                tagsList.addAll(position+1, tagsListOther)
                itemCount = tagsListOther.size

            }
        }

        (tagsList[position] as TagWithGenre.Genre).isOpened = true
        tagAdapter.submitList(tagsList)
        tagAdapter.notifyItemRangeInserted(position+1, itemCount)

    }

    private fun removeTagsSubList(genre : String, position : Int)  {

        var itemCount = 0

        when(genre){
            TAG_BY_PERIOD -> {
                tagsList.removeAll(tagsListByPeriod)
                itemCount = tagsListByPeriod.size
            }
            TAG_BY_GENRE -> {
                tagsList.removeAll(tagsListByGenre)
                itemCount = tagsListByGenre.size

            }
            TAG_BY_MINDFUL -> {
                tagsList.removeAll(tagsListMindful)
                itemCount = tagsListMindful.size

            }

            TAG_BY_CLASSIC -> {
                tagsList.removeAll( tagsListClassics)
                itemCount = tagsListClassics.size
            }

            TAG_BY_EXPERIMENTAL ->{
                tagsList.removeAll( tagsListExperimental)
                itemCount = tagsListExperimental.size
            }

            TAG_BY_SPECIAL ->{
                tagsList.removeAll( tagsListSpecial)
                itemCount = tagsListSpecial.size
            }


            TAG_BY_TALK -> {
                tagsList.removeAll(tagsListByTalk)
                itemCount = tagsListByTalk.size

            }
            TAG_BY_RELIGION -> {
                tagsList.removeAll(tagsListReligion)
                itemCount = tagsListReligion.size

            }
            TAG_BY_ORIGIN -> {
                tagsList.removeAll(tagsListByOrigin)
                itemCount = tagsListByOrigin.size

            }
            TAG_BY_OTHER -> {
                tagsList.removeAll(tagsListOther)
                itemCount = tagsListOther.size

            }
        }

        (tagsList[position] as TagWithGenre.Genre).isOpened = false
        tagAdapter.submitList(tagsList)
        tagAdapter.notifyItemRangeRemoved(position+1, itemCount)

    }



    private fun setupButtons(){
        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamTag.postValue("")

            dismiss()

        }

        bind.tvBack.setOnClickListener {

           dismiss()

        }
    }




    override fun onStop() {
        super.onStop()

        RadioSearchFragment.tagAdapterPosition = bind.recyclerView.layoutManager?.onSaveInstanceState()

        bind.recyclerView.adapter = null
        _bind = null

    }


    private var isOpen : MutableLiveData<Boolean> = MutableLiveData()





}