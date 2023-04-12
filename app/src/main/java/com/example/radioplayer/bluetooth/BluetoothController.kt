package com.example.radioplayer.bluetooth

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {

//    val scannedDevices : StateFlow<List<BluetoothDevice>>
    val pairedDevices : StateFlow<List<BluetoothDevice>>

    fun startDiscovery()
    fun stopDiscovery()

//    fun release()

}