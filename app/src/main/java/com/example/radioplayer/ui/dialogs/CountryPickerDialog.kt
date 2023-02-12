package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.CountryAdapter
import com.example.radioplayer.databinding.DialogPickCountryBinding
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.listOfCountries


class CountryPickerDialog(
   private val requireContext : Context,
    private val mainViewModel: MainViewModel
   )
    : AppCompatDialog(requireContext) {

    private val counties = listOfCountries

    lateinit var bind : DialogPickCountryBinding

    lateinit var countryAdapter : CountryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        bind = DialogPickCountryBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(bind.root)

        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

        setupRecyclerView()

        setupButtonsClickListener()

        setOnAdapterClickListener()

        countryAdapter.differ.submitList(counties)

        filterCountriesOnEditTextListener()


    }

    private fun filterCountriesOnEditTextListener(){

        bind.editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val newList = counties.filter { country ->

                    country.countryName.contains(s.toString(), ignoreCase = true)
                }
                countryAdapter.differ.submitList(newList)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


    }


    private fun setOnAdapterClickListener(){

        countryAdapter.setOnCountryClickListener {

            mainViewModel.searchParamCountry.postValue(it)

            bind.rvCountries.adapter = null

            dismiss()
        }

    }

    private fun setupRecyclerView(){

        countryAdapter = CountryAdapter()

        bind.rvCountries.apply {

            adapter = countryAdapter
            layoutManager = LinearLayoutManager(requireContext)
        }
    }

    private fun setupButtonsClickListener(){

        bind.tvBack.setOnClickListener {

            bind.rvCountries.adapter = null
            dismiss()
        }

        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamCountry.postValue("Country")

            bind.rvCountries.adapter = null
            dismiss()
        }
    }

}