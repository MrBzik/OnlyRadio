package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.FilterCountriesAdapter
import com.example.radioplayer.databinding.DialogPickCountryBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.listOfCountries


class CountryPickerDialog(
   private val requireContext : Context,
    private val mainViewModel: MainViewModel
   )
    : AppCompatDialog(requireContext) {

    private val countries = listOfCountries

    private var _bind : DialogPickCountryBinding? = null
    private val bind get() = _bind!!

    lateinit var countryAdapter : FilterCountriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        _bind = DialogPickCountryBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        setupRecyclerView()

        setupButtonsClickListener()

        setOnAdapterClickListener()




        filterCountriesOnEditTextListener()


    }

    private fun filterCountriesOnEditTextListener(){

        bind.editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                countryAdapter.filter.filter(s)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

    }


    private fun setOnAdapterClickListener(){

        countryAdapter.setOnCountryClickListener {

            mainViewModel.searchParamCountry.postValue(it)

            cleanAndClose()
        }

    }

    private fun setupRecyclerView(){

        countryAdapter = FilterCountriesAdapter()
        countryAdapter.submitList(countries)

        bind.rvCountries.apply {

            adapter = countryAdapter
            layoutManager = LinearLayoutManager(requireContext)
        }
    }

    private fun setupButtonsClickListener(){

        bind.tvBack.setOnClickListener {

            cleanAndClose()
        }

        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamCountry.postValue("")

            cleanAndClose()
        }
    }

    private fun cleanAndClose(){
        bind.rvCountries.adapter = null
        _bind = null
        dismiss()
    }

}