package com.onlyradio.radioplayer.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class ConnectingDeviceReceiver(
    private val connectingDevice: (BluetoothDevice) -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        when(intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {

                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                device?.let(connectingDevice)
            }

        }
    }
}