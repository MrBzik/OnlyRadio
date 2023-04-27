package com.example.radioplayer.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher

import android.view.View
import androidx.core.content.ContextCompat

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.FilterCountriesAdapter
import com.example.radioplayer.adapters.models.CountryWithRegion
import com.example.radioplayer.adapters.models.TagWithGenre

import com.example.radioplayer.databinding.DialogPickCountryBinding
import com.example.radioplayer.ui.fragments.RadioSearchFragment.Companion.countriesAdapterPosition
import com.example.radioplayer.ui.fragments.RadioSearchFragment.Companion.listOfCountries
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.*


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




    private fun setupRecyclerView(){

        countryAdapter = FilterCountriesAdapter()

        countryAdapter.apply {
            submitList(listOfCountries)
            defaultTextColor = ContextCompat.getColor(requireContext, R.color.unselected_genre_color)
            selectedTextColor = ContextCompat.getColor(requireContext, R.color.selected_genre_color)
            openingDrawable = R.drawable.tags_expand
            closingDrawable = R.drawable.tags_shrink
        }



        bind.rvCountries.apply {

            adapter = countryAdapter
            layoutManager = LinearLayoutManager(requireContext)

            countriesAdapterPosition?.let {
                layoutManager?.onRestoreInstanceState(it)
            }

        }
    }


    private fun setOnAdapterClickListener(){

        countryAdapter.setOnCountryClickListener { countryItem, position ->


            if(countryItem is CountryWithRegion.Region){

                if(countryItem.isOpened) {

                    removeCountriesSubList(countryItem.region, position)

                } else {

                    addCountriesSublist(countryItem.region, position)
                }


            } else if(countryItem is CountryWithRegion.Country) {

                mainViewModel.searchParamCountry.postValue(countryItem.countryCode)
                mainViewModel.searchFullCountryName = countryItem.countryName

                dismiss()

            }

        }

    }


    private fun addCountriesSublist(region : String, position: Int){

        var itemCount = 0

        when(region){
            COUNTRY_REGION_AFRICA -> {
                listOfCountries.addAll(position+1, listOfAfrica)
                itemCount = listOfAfrica.size
            }
            COUNTRY_REGION_ASIA -> {
                listOfCountries.addAll(position+1, listOfAsia)
                itemCount = listOfAsia.size

            }

            COUNTRY_REGION_CENTRAL_AMERICA -> {
                listOfCountries.addAll(position+1, listOfCentralAmerica)
                itemCount = listOfCentralAmerica.size
            }

            COUNTRY_REGION_NORTH_AMERICA -> {
                listOfCountries.addAll(position+1, listOfNorthAmerica)
                itemCount = listOfNorthAmerica.size

            }
            COUNTRY_REGION_SOUTH_AMERICA -> {
                listOfCountries.addAll(position+1, listOfSouthAmerica)
                itemCount = listOfSouthAmerica.size
            }

            COUNTRY_REGION_EAST_EUROPE ->{
                listOfCountries.addAll(position+1, listOfEastEurope)
                itemCount = listOfEastEurope.size
            }

            COUNTRY_REGION_WEST_EUROPE ->{
                listOfCountries.addAll(position+1, listOfWestEurope)
                itemCount = listOfWestEurope.size
            }


            COUNTRY_REGION_MIDDLE_EAST -> {
                listOfCountries.addAll(position+1, listOfMiddleEast)
                itemCount = listOfMiddleEast.size

            }
            COUNTRY_REGION_OCEANIA -> {
                listOfCountries.addAll(position+1, listOfOceania)
                itemCount = listOfOceania.size

            }
            COUNTRY_REGION_THE_CARIBBEAN -> {
                listOfCountries.addAll(position+1, listOfTheCaribbean)
                itemCount = listOfTheCaribbean.size

            }
        }

        (listOfCountries[position] as CountryWithRegion.Region).isOpened = true
        countryAdapter.submitList(listOfCountries)
        countryAdapter.notifyItemRangeInserted(position+1, itemCount)

    }

    private fun removeCountriesSubList(region : String, position : Int)  {

        var itemCount = 0

        when(region){
            COUNTRY_REGION_AFRICA -> {
                listOfCountries.removeAll(listOfAfrica)
                itemCount = listOfAfrica.size
            }
            COUNTRY_REGION_ASIA -> {
                listOfCountries.removeAll(listOfAsia)
                itemCount = listOfAsia.size
            }

            COUNTRY_REGION_CENTRAL_AMERICA -> {
                listOfCountries.removeAll(listOfCentralAmerica)
                itemCount = listOfCentralAmerica.size
            }

            COUNTRY_REGION_NORTH_AMERICA -> {
                listOfCountries.removeAll(listOfNorthAmerica)
                itemCount = listOfNorthAmerica.size

            }
            COUNTRY_REGION_SOUTH_AMERICA -> {
                listOfCountries.removeAll(listOfSouthAmerica)
                itemCount = listOfSouthAmerica.size
            }

            COUNTRY_REGION_EAST_EUROPE ->{
                listOfCountries.removeAll(listOfEastEurope)
                itemCount = listOfEastEurope.size
            }

            COUNTRY_REGION_WEST_EUROPE ->{
                listOfCountries.removeAll(listOfWestEurope)
                itemCount = listOfWestEurope.size
            }


            COUNTRY_REGION_MIDDLE_EAST -> {
                listOfCountries.removeAll( listOfMiddleEast)
                itemCount = listOfMiddleEast.size

            }
            COUNTRY_REGION_OCEANIA -> {
                listOfCountries.removeAll(listOfOceania)
                itemCount = listOfOceania.size

            }
            COUNTRY_REGION_THE_CARIBBEAN -> {
                listOfCountries.removeAll(listOfTheCaribbean)
                itemCount = listOfTheCaribbean.size

            }
        }

        (listOfCountries[position] as CountryWithRegion.Region).isOpened = false
        countryAdapter.submitList(listOfCountries)
        countryAdapter.notifyItemRangeRemoved(position+1, itemCount)

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
        countriesAdapterPosition = bind.rvCountries.layoutManager?.onSaveInstanceState()
        bind.rvCountries.adapter = null
        _bind = null
    }

}