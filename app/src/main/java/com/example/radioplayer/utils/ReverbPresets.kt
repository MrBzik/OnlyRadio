package com.example.radioplayer.utils

import android.media.audiofx.EnvironmentalReverb


fun setPreset(reverb : EnvironmentalReverb, mode : Int){

    when(mode){
        0 -> return

        // large hall
        1 -> {
            reverb.roomLevel = -1000
            reverb.roomHFLevel = -600
            reverb.decayTime = 1800
            reverb.decayHFRatio = 700
            reverb.reflectionsLevel = -2000
            reverb.reflectionsDelay = 30
            reverb.reverbLevel = -1400
            reverb.reverbDelay = 60
            reverb.diffusion = 1000
            reverb.density = 1000
        }

        // medium hall
        2 -> {
            reverb.roomLevel = -1000
            reverb.roomHFLevel = -600
            reverb.decayTime = 1800
            reverb.decayHFRatio = 700
            reverb.reflectionsLevel = -1300
            reverb.reflectionsDelay = 15
            reverb.reverbLevel = -800
            reverb.reverbDelay = 30
            reverb.diffusion = 1000
            reverb.density = 1000
        }

        // large room
        3 -> {
            reverb.roomLevel = -1000
            reverb.roomHFLevel = -600
            reverb.decayTime = 1500
            reverb.decayHFRatio = 830
            reverb.reflectionsLevel = -1600
            reverb.reflectionsDelay = 5
            reverb.reverbLevel = -1000
            reverb.reverbDelay = 40
            reverb.diffusion = 1000
            reverb.density = 1000
        }

        // medium room
        4 -> {

            reverb.roomLevel = -1000
            reverb.roomHFLevel = -600
            reverb.decayTime = 1300
            reverb.decayHFRatio = 830
            reverb.reflectionsLevel = -1000
            reverb.reflectionsDelay = 20
            reverb.reverbLevel = -200
            reverb.reverbDelay = 20
            reverb.diffusion = 1000
            reverb.density = 1000
        }

        // small room
        5 -> {
            reverb.roomLevel = -1000
            reverb.roomHFLevel = -600
            reverb.decayTime = 1100
            reverb.decayHFRatio = 830
            reverb.reflectionsLevel = -400
            reverb.reflectionsDelay = 5
            reverb.reverbLevel = 500
            reverb.reverbDelay = 10
            reverb.diffusion = 1000
            reverb.density = 1000
        }

        // plate
        6 -> {
            reverb.roomLevel = -1000
            reverb.roomHFLevel = -200
            reverb.decayTime = 1300
            reverb.decayHFRatio = 900
            reverb.reflectionsLevel = 0
            reverb.reflectionsDelay = 2
            reverb.reverbLevel = 0
            reverb.reverbDelay = 10
            reverb.diffusion = 1000
            reverb.density = 750
        }
    }
}




