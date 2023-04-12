package com.example.radioplayer.bluetooth

import android.bluetooth.BluetoothDevice

data class BluetoothUiState (
    val pairedDevices : List<BluetoothDevice> = emptyList(),
    val scannedDevices : List<BluetoothDevice> = emptyList()
        )