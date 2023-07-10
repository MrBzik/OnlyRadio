package com.example.radioplayer.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.radioplayer.R

fun FragmentManager.navigate(fragment : Fragment){
    beginTransaction().apply {
        setCustomAnimations(R.anim.blank_anim, android.R.anim.fade_out)
        replace(R.id.flFragment, fragment)
        addToBackStack(null)
        commit()
    }
}