package com.onlyradio.radioplayer.ui.delegates

import android.view.Gravity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.Fade
import androidx.transition.Slide
import com.onlyradio.radioplayer.R
import com.onlyradio.radioplayer.exoPlayer.RadioService
import com.onlyradio.radioplayer.extensions.navigate
import com.onlyradio.radioplayer.ui.fragments.FavStationsFragment
import com.onlyradio.radioplayer.ui.fragments.HistoryFragment
import com.onlyradio.radioplayer.ui.fragments.RadioSearchFragment
import com.onlyradio.radioplayer.ui.fragments.RecordingDetailsFragment
import com.onlyradio.radioplayer.ui.fragments.RecordingsFragment
import com.onlyradio.radioplayer.ui.fragments.SettingsFragment
import com.onlyradio.radioplayer.ui.fragments.StationDetailsFragment
import com.onlyradio.radioplayer.ui.viewmodels.MainViewModel
import com.onlyradio.radioplayer.utils.Constants

interface Navigation {

    fun initialNavigation()

    fun handleNavigationToFragments(itemId : Int)

    fun handleNavigationWithDetailsFragment(itemId: Int)

}


class NavigationImpl (
    private val supportFragmentManager: FragmentManager,
    private val mainViewModel: MainViewModel
    ) : Navigation {

   private val radioSearchFragment : RadioSearchFragment by lazy { RadioSearchFragment() }
   private val favStationsFragment : FavStationsFragment by lazy { FavStationsFragment() }
   private val historyFragment : HistoryFragment by lazy { HistoryFragment() }
   private val recordingsFragment : RecordingsFragment by lazy { RecordingsFragment() }
   private val settingsFragment : SettingsFragment by lazy { SettingsFragment() }

   private val stationDetailsFragment : StationDetailsFragment by lazy { StationDetailsFragment().apply {
        enterTransition = Slide(Gravity.BOTTOM)
        exitTransition = Slide(Gravity.BOTTOM)
        }
    }

    private val recordingDetailsFragment : RecordingDetailsFragment by lazy { RecordingDetailsFragment().apply {
        enterTransition = Slide(Gravity.BOTTOM)
        exitTransition = Slide(Gravity.BOTTOM)
        }
    }


    override fun initialNavigation() {
        if(mainViewModel.isInitialLaunchOfTheApp){
            mainViewModel.isInitialLaunchOfTheApp = false

            supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, radioSearchFragment)
                addToBackStack(null)
                commit()
            }
        }
    }

    override fun handleNavigationToFragments(itemId : Int)  {

        if(itemId != R.id.mi_radioSearchFragment){
            mainViewModel.updateIsToPlayLoadAnim(false)
        }

        supportFragmentManager.popBackStack()

        getFragment(itemId).apply {
            exitTransition = null
            supportFragmentManager.navigate(this)
        }

        if(mainViewModel.isInDetailsFragment.value == true)
           mainViewModel.updateIsInDetails(false)

    }



    override fun handleNavigationWithDetailsFragment(itemId: Int) {

        if(mainViewModel.isInDetailsFragment.value == false) {
            mainViewModel.updateIsInDetails(true)

            getFragment(itemId).exitTransition = Fade()

            mainViewModel.updateIsToPlayLoadAnim(false)

            mainViewModel.onNavigationWithDetails(true)

            supportFragmentManager.beginTransaction().apply {

                if(!RadioService.isFromRecording){
                    replace(R.id.flFragment, stationDetailsFragment)
                } else {
                    replace(R.id.flFragment, recordingDetailsFragment)
                }
                addToBackStack(null)
                commit()
            }
        }

        else {

            handleNavigationToFragments(itemId)
        }
    }

    private fun getFragment(id : Int) : Fragment {

        return when(id) {

            R.id.mi_radioSearchFragment -> {
                mainViewModel.currentFragment = Constants.FRAG_SEARCH
                radioSearchFragment

            }
            R.id.mi_favStationsFragment -> {
                mainViewModel.currentFragment = Constants.FRAG_FAV
                favStationsFragment

            }
            R.id.mi_historyFragment -> {
                mainViewModel.currentFragment = Constants.FRAG_HISTORY
                historyFragment

            }
            R.id.mi_recordingsFragment -> {
                mainViewModel.currentFragment = Constants.FRAG_REC
                recordingsFragment
            }

            else -> {
                mainViewModel.currentFragment = Constants.FRAG_OPTIONS
                settingsFragment
            }
        }
    }
}

