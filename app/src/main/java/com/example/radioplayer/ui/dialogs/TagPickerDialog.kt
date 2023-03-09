package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.doOnLayout
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.FilterTagsAdapter
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.*
import com.example.radioplayer.utils.KeyboardObserver.observeKeyboardState

class TagPickerDialog (
    private val requireContext : Context,
    private val listOfItems : List<String>,
    private val mainViewModel: MainViewModel
    )
    : AppCompatDialog(requireContext) {

    private var _bind : DialogPickTagBinding? = null
    private val bind get() = _bind!!

    private lateinit var tagAdapter : FilterTagsAdapter
    private val tagsList = ArrayList<String>()

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

    }

    private fun handleKeyboardToggle (){
        observeKeyboardState(bind.root, {
            bind.tvBack.visibility = View.GONE
            bind.tvClearSelection.visibility = View.GONE
            bind.tvTitle.visibility = View.GONE

        }, {
            bind.tvBack.visibility = View.VISIBLE
            bind.tvClearSelection.visibility = View.VISIBLE
            bind.tvTitle.visibility = View.VISIBLE
            bind.editText.clearFocus()

        }, {
            bind.editText.requestFocus()

        })
    }


    private fun setupRecyclerView(){

        tagAdapter = FilterTagsAdapter()
        fillInitialTagList()
        tagAdapter.submitList(tagsList)


        bind.recyclerView.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext)
        }
    }

    private fun fillInitialTagList() {

        tagsList.apply {

            addAll(listOfTags)

            add(TAG_BY_PERIOD)
            addAll(byPeriodTags)

            add(TAG_BY_GENRE)
            addAll(byGenreTags)

            add(TAG_BY_SPECIAL)
            addAll(specialTags)

            add(TAG_BY_TALK)
            addAll(talkNewsTags)

            add(TAG_BY_RELIGION)
            addAll(religionTags)

            add(TAG_BY_ORIGIN)
            addAll(byOriginTags)

            add(TAG_BY_OTHER)
            addAll(otherTags)

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

            if(tag.contains("---")){

               val itemCount = removeTagsSubList(tag)

                tagAdapter.submitList(tagsList)
                tagAdapter.notifyItemRangeRemoved(position+1, itemCount)

            } else {

                mainViewModel.searchParamTag.postValue(tag)

                dismiss()
            }
        }
    }

    private fun removeTagsSubList(label : String) : Int {

        when(label){
            TAG_BY_PERIOD -> {
                tagsList.removeAll(byPeriodTags)
                return byPeriodTags.size
            }
            TAG_BY_GENRE -> {
                tagsList.removeAll(byGenreTags)
                return byGenreTags.size
            }
            TAG_BY_SPECIAL -> {
                tagsList.removeAll(specialTags)
                return specialTags.size
            }
            TAG_BY_TALK -> {
                tagsList.removeAll(talkNewsTags)
                return talkNewsTags.size
            }
            TAG_BY_RELIGION -> {
                tagsList.removeAll(religionTags)
                return religionTags.size
            }
            TAG_BY_ORIGIN -> {
                tagsList.removeAll(byOriginTags)
                return byOriginTags.size
            }
            TAG_BY_OTHER -> {
                tagsList.removeAll(otherTags)
                return otherTags.size
            }

            else -> return 0
        }
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
        bind.recyclerView.adapter = null
        _bind = null

    }


    private var isOpen : MutableLiveData<Boolean> = MutableLiveData()





}