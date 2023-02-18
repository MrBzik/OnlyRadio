package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.FilterTagsAdapter
import com.example.radioplayer.databinding.DialogPickTagBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.KeyboardEditText
import java.security.Key

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




//        bind.editText.setOnFocusChangeListener { v, hasFocus ->
//            if(hasFocus){
//                bind.tvBack.visibility = View.GONE
//                bind.tvClearSelection.visibility = View.GONE
//            }
//        }
//
//        bind.editText.setOnClickListener {
//
//                bind.tvBack.visibility = View.GONE
//                bind.tvClearSelection.visibility = View.GONE
//
//        }
//
//        bind.editText.listener = object : KeyboardEditText.Listener{
//            override fun onImeBack(editText: KeyboardEditText) {
//                bind.tvBack.visibility = View.VISIBLE
//                bind.tvClearSelection.visibility = View.VISIBLE
//            }
//        }




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

           dismiss()

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



}