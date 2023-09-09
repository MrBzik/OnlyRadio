package com.onlyradio.radioplayer.ui.dialogs


import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.adapters.FilterCountriesAdapter
import com.onlyradio.radioplayer.adapters.models.CountryWithRegion
import com.onlyradio.radioplayer.databinding.DialogPickCountryBinding
import com.onlyradio.radioplayer.extensions.observeKeyboardState
import com.onlyradio.radioplayer.ui.viewmodels.SearchDialogsViewModel
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_AFRICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_ASIA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_CENTRAL_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_EAST_EUROPE
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_MIDDLE_EAST
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_NORTH_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_OCEANIA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_SOUTH_AMERICA
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_THE_CARIBBEAN
import com.onlyradio.radioplayer.utils.COUNTRY_REGION_WEST_EUROPE
import com.onlyradio.radioplayer.utils.listOfAfrica
import com.onlyradio.radioplayer.utils.listOfAsia
import com.onlyradio.radioplayer.utils.listOfCentralAmerica
import com.onlyradio.radioplayer.utils.listOfEastEurope
import com.onlyradio.radioplayer.utils.listOfMiddleEast
import com.onlyradio.radioplayer.utils.listOfNorthAmerica
import com.onlyradio.radioplayer.utils.listOfOceania
import com.onlyradio.radioplayer.utils.listOfSouthAmerica
import com.onlyradio.radioplayer.utils.listOfTheCaribbean
import com.onlyradio.radioplayer.utils.listOfWestEurope


class CountryPickerDialog(
   private val requireContext : Context,
   private val searchDialogsViewModel : SearchDialogsViewModel,
   private val handleNewParams : (countryCode : String, countryName : String) -> Unit
)
    : BaseDialog<DialogPickCountryBinding>(
    requireContext,
    DialogPickCountryBinding::inflate
    ) {



//    private var _bind : DialogPickCountryBinding? = null
//    private val bind get() = _bind!!


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

        searchDialogsViewModel.updateCountryList(){
            searchDialogsViewModel.countriesAdapterPosition = bind.rvCountries.layoutManager?.onSaveInstanceState()
            bind.rvCountries.adapter = null
            bind.rvCountries.adapter = countryAdapter
            searchDialogsViewModel.countriesAdapterPosition?.let {
                bind.rvCountries.layoutManager?.onRestoreInstanceState(it)
            }
        }

    }

    private fun handleKeyboardToggle (){


        bind.root.observeKeyboardState(
            {
                bind.tvTitle.visibility = View.GONE

            }, {
                bind.tvTitle.visibility = View.VISIBLE
                bind.editText.clearFocus()
            }, { bind.editText.requestFocus() }
        )


//        KeyboardObserver.observeKeyboardState(bind.root, {
//
//            bind.tvTitle.visibility = View.GONE
//
//
//        }, {
//
//            bind.tvTitle.visibility = View.VISIBLE
//            bind.editText.clearFocus()
//
//
//        }, { bind.editText.requestFocus() })
    }


    private fun filterCountriesOnEditTextListener(){

//        val listener = object :TextWatcher{
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                countryAdapter.filter.filter(s)
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//            }
//        }

        bind.editText.addTextChangedListener {
            it?.let {
                countryAdapter.filter.filter(it)
            }
        }

//        bind.editText.addTextChangedListener(listener)

    }


    private fun setupRecyclerView(){

        countryAdapter = FilterCountriesAdapter()

        countryAdapter.apply {
            submitList(searchDialogsViewModel.listOfCountries)
            defaultTextColor = ContextCompat.getColor(requireContext, R.color.unselected_genre_color)
            selectedTextColor = ContextCompat.getColor(requireContext, R.color.selected_genre_color)
            openingDrawable = R.drawable.tags_expand
            closingDrawable = R.drawable.tags_shrink
        }



        bind.rvCountries.apply {

            adapter = countryAdapter
            layoutManager = LinearLayoutManager(requireContext)

            searchDialogsViewModel.countriesAdapterPosition?.let {
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

                handleNewParams(countryItem.countryCode, countryItem.countryName)


                dismiss()

            }

        }

    }


    private fun addCountriesSublist(region : String, position: Int){

        val listToAdd = getCountryList(region)
        searchDialogsViewModel.listOfCountries.addAll(position+1, listToAdd)

        (searchDialogsViewModel.listOfCountries[position] as CountryWithRegion.Region).isOpened = true
        countryAdapter.submitList(searchDialogsViewModel.listOfCountries)
        countryAdapter.notifyItemRangeInserted(position+1, listToAdd.size)

    }

    private fun removeCountriesSubList(region : String, position : Int)  {

        val listToRemove = getCountryList(region)
        searchDialogsViewModel.listOfCountries.removeAll(listToRemove)

        (searchDialogsViewModel.listOfCountries[position] as CountryWithRegion.Region).isOpened = false
        countryAdapter.submitList(searchDialogsViewModel.listOfCountries)
        countryAdapter.notifyItemRangeRemoved(position+1, listToRemove.size)

    }


    private fun getCountryList(region : String) : Set<CountryWithRegion>{

        return  when(region){
            COUNTRY_REGION_AFRICA -> listOfAfrica
            COUNTRY_REGION_ASIA -> listOfAsia
            COUNTRY_REGION_CENTRAL_AMERICA -> listOfCentralAmerica
            COUNTRY_REGION_NORTH_AMERICA -> listOfNorthAmerica
            COUNTRY_REGION_SOUTH_AMERICA -> listOfSouthAmerica
            COUNTRY_REGION_EAST_EUROPE -> listOfEastEurope
            COUNTRY_REGION_WEST_EUROPE -> listOfWestEurope
            COUNTRY_REGION_MIDDLE_EAST -> listOfMiddleEast
            COUNTRY_REGION_OCEANIA -> listOfOceania
            COUNTRY_REGION_THE_CARIBBEAN -> listOfTheCaribbean
            else -> emptySet()
        }

    }



    private fun setupButtonsClickListener(){

        bind.tvBack.setOnClickListener {

            dismiss()
        }

        bind.tvClearSelection.setOnClickListener{

            handleNewParams("", "")

            dismiss()
        }
    }


    override fun onStop() {
        super.onStop()
        searchDialogsViewModel.countriesAdapterPosition = bind.rvCountries.layoutManager?.onSaveInstanceState()
        bind.rvCountries.adapter = null
        _bind = null
    }

}