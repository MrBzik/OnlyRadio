package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.FilterTagsAdapter
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel

class TagPickerDialog (
    private val requireContext : Context,
    private val listOfItems : List<String>,
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

    }

    private fun cleanAndClose(){
        bind.recyclerView.adapter = null
        _bind = null
        dismiss()
    }


    private fun setupRecyclerView(){

        tagAdapter = FilterTagsAdapter()
        tagAdapter.submitList(listOfItems)

        bind.recyclerView.apply {
            adapter = tagAdapter
            layoutManager = LinearLayoutManager(requireContext)
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

        tagAdapter.setOnTagClickListener { tag ->

            mainViewModel.searchParamTag.postValue(tag)

            cleanAndClose()

        }

    }

    private fun setupButtons(){
        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamTag.postValue("")

            cleanAndClose()

        }

        bind.tvBack.setOnClickListener {

            cleanAndClose()

        }
    }

}