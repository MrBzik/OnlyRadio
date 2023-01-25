package com.example.radioplayer.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CursorAdapter
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.R
import com.example.radioplayer.adapters.PagingRadioAdapter
import com.example.radioplayer.databinding.FragmentRadioSearchBinding
import com.example.radioplayer.ui.DialogPicker
import com.example.radioplayer.ui.MainActivity
import com.example.radioplayer.ui.viewmodels.MainViewModel
import com.example.radioplayer.utils.listOfTags
import com.hbb20.countrypicker.models.CPCountry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RadioSearchFragment : Fragment() {

    lateinit var bind : FragmentRadioSearchBinding

    lateinit var mainViewModel : MainViewModel

    lateinit var toggle : ActionBarDrawerToggle

    private var selectedCountry = ""
    private var selectedTag = ""

    private val allTags = listOfTags

    lateinit var tagsAdapter : ArrayAdapter<String>

    @Inject
    lateinit var pagingRadioAdapter : PagingRadioAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind = FragmentRadioSearchBinding.inflate(inflater, container, false)

        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()





        toggle = ActionBarDrawerToggle((activity as MainActivity), bind.drawerLayout, R.string.open, R.string.close )
        bind.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setSearchDrawer()


        mainViewModel = (activity as MainActivity).mainViewModel


        setRecycleView()

        setAdapterLoadStateListener()


        pagingRadioAdapter.setOnClickListener {

            mainViewModel.playOrToggleStation(it, true)
            mainViewModel.newRadioStation.postValue(it)
        }


        observeStations()

    }


    override fun onResume() {
        super.onResume()

        tagsAdapter = ArrayAdapter(requireContext(), R.layout.tags_dropdown_item, allTags)
        bind.autoComplTvTags.setAdapter(tagsAdapter)

    }


    private fun setRecycleView(){

        bind.rvSearchStations.apply {

            adapter = pagingRadioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setAdapterLoadStateListener(){

        pagingRadioAdapter.addLoadStateListener {

            if (it.refresh is LoadState.Loading ||
                it.append is LoadState.Loading)
                bind.loadStationsProgressBar.isVisible = true
            else {
                bind.loadStationsProgressBar.visibility = View.GONE
            }

        }

    }


    private fun observeStations(){
        viewLifecycleOwner.lifecycleScope.launch{
            mainViewModel.stationsFlow.collectLatest {
                pagingRadioAdapter.submitData(it)
            }
        }
    }


    private fun setSearchDrawer(){


        bind.autoComplTvTags.setOnItemClickListener{ adapter, _, position, _ ->
            selectedTag = adapter.getItemAtPosition(position) as String
        }


        bind.btnAcceptName.setOnClickListener {
            bind.etName.text.apply {
                if(this.isEmpty()) {
                    bind.tvChosenName.text = "none"
                } else {
                    bind.tvChosenName.text = this
                    bind.etName.setText("")
                }
            }
        }

        bind.countryPicker.cpViewHelper.cpViewConfig.viewTextGenerator = { cpCountry: CPCountry ->
            "${cpCountry.name} (${cpCountry.alpha2})"
        }
        bind.countryPicker.cpViewHelper.refreshView()

        bind.countryPicker.cpViewHelper.selectedCountry.observe(viewLifecycleOwner){ code ->

            selectedCountry = code?.alpha2 ?: ""


        }

        bind.tvTestingTags.setOnClickListener {

            DialogPicker(requireContext(), listOfTags, it as TextView).show()

        }



        bind.btnSearch.setOnClickListener {


               val name = bind.tvChosenName.text.toString()


               val bundle = Bundle().apply {


                       putString("TAG", selectedTag)


                       putString("COUNTRY", selectedCountry)


                   if(name == "none"){
                       putString("NAME", "")
                   } else {
                       putString("NAME", name)
                   }

                   putBoolean("SEARCH_TOP", false)

               }
                mainViewModel.isNewSearch = true
                mainViewModel.setSearchBy(bundle)
                bind.rvSearchStations.scrollToPosition(0)
        }
    }


    private fun setupMenu(){

        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.your_menu, menu)
                menuInflater.inflate(R.menu.search_toolbar, menu)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                toggle.onOptionsItemSelected(menuItem)
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

}

