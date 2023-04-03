package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.FilterCountriesAdapter
import com.example.radioplayer.databinding.DialogHistorySettingsBinding
import com.example.radioplayer.databinding.DialogPickCountryBinding
import com.example.radioplayer.ui.fragments.RadioSearchFragment
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.KeyboardObserver
import com.example.radioplayer.utils.listOfCountries


class CountryPickerDialog(
   private val requireContext : Context,
   private val mainViewModel: MainViewModel
)
    : BaseDialog<DialogPickCountryBinding>(
    requireContext,
    DialogPickCountryBinding::inflate
    ) {



//    private var _bind : DialogPickCountryBinding? = null
//    private val bind get() = _bind!!

    private val countries = listOfCountries

    lateinit var countryAdapter : FilterCountriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

//        _bind = DialogPickCountryBinding.inflate(layoutInflater)


        super.onCreate(savedInstanceState)

//        setContentView(bind.root)

        setupRecyclerView()

        setupButtonsClickListener()

        setOnAdapterClickListener()

        handleKeyboardToggle()

        filterCountriesOnEditTextListener()

        adjustDialogHeight(bind.clCountryPickDialog)



    }

    private fun handleKeyboardToggle (){

        KeyboardObserver.observeKeyboardState(bind.root, {

            bind.tvTitle.visibility = View.GONE


        }, {

            bind.tvTitle.visibility = View.VISIBLE
            bind.editText.clearFocus()




        }, { bind.editText.requestFocus() })
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

        countryAdapter.setOnCountryClickListener { code, name ->

            mainViewModel.searchParamCountry.postValue(code)
            mainViewModel.searchFullCountryName = name

            dismiss()
        }

    }

    private fun setupRecyclerView(){

        countryAdapter = FilterCountriesAdapter()
        countryAdapter.submitList(countries)

        bind.rvCountries.apply {

            adapter = countryAdapter
            layoutManager = LinearLayoutManager(requireContext)

            RadioSearchFragment.countriesAdapterPosition?.let {
                layoutManager?.onRestoreInstanceState(it)
            }

        }
    }

    private fun setupButtonsClickListener(){

        bind.tvBack.setOnClickListener {

            dismiss()
        }

        bind.tvClearSelection.setOnClickListener{

            mainViewModel.searchParamCountry.postValue("")
            mainViewModel.searchFullCountryName = ""
            dismiss()
        }
    }


    override fun onStop() {
        super.onStop()
        RadioSearchFragment.countriesAdapterPosition =
            bind.rvCountries.layoutManager?.onSaveInstanceState()
        bind.rvCountries.adapter = null
        _bind = null
    }

}