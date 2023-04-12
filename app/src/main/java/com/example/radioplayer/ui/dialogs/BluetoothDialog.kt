package com.example.radioplayer.ui.dialogs

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.radioplayer.adapters.PairedDevicesAdapter
import com.example.radioplayer.bluetooth.ConnectingDeviceReceiver
import com.example.radioplayer.databinding.DialogBluetoothBinding
import com.example.radioplayer.ui.viewmodels.BluetoothViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class BluetoothDialog (
    private val requireContext : Context,
    private val viewModel : BluetoothViewModel,
//    private val onStartScan : () -> Unit,
//    private val onStopScan : () -> Unit
    )
    : BaseDialog<DialogBluetoothBinding>(
    requireContext, DialogBluetoothBinding::inflate
    )


{

    lateinit var pairedDevicesAdapter : PairedDevicesAdapter

    lateinit var a2dp : BluetoothA2dp
    private var device: BluetoothDevice? = null

    lateinit var headset : BluetoothProfile

    private val bluetoothManager by lazy {
        requireContext.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled : Boolean
        get() = bluetoothAdapter?.isEnabled == true


        @SuppressLint("MissingPermission")
        private val connectingDeviceReceiver = ConnectingDeviceReceiver { deviceToDisconnect ->

              disconnect(deviceToDisconnect)

        }


        @SuppressLint("MissingPermission")
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adjustDialogHeight(bind.clRoot)

        setRecyclerView()

        setAdapterClickListener()


        bind.tvStartScan.setOnClickListener {

//           viewModel.startScan()

            bluetoothAdapter?.bondedDevices?.let {

                pairedDevicesAdapter.addItems(it)


            }

        }

        bind.tvStopScan.setOnClickListener {

            device?.let{
                disConnectUsingBluetoothA2dp(it)
            }
        }



        bind.tvBack.setOnClickListener {
            dismiss()
        }

            val filter = IntentFilter()
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
           context.registerReceiver(
            connectingDeviceReceiver,
           filter
        )




//        lifecycleScope.launch {
//
//            viewModel.state.collectLatest { state ->
//
//                pairedDevicesAdapter.addItems(state.pairedDevices.toMutableSet())
//
//            }
//        }
    }

    private fun setRecyclerView(){

        pairedDevicesAdapter = PairedDevicesAdapter()

        bind.rvPairedDevices.apply {

            layoutManager = LinearLayoutManager(context)
            adapter = pairedDevicesAdapter

        }
    }

    private fun setAdapterClickListener(){


        pairedDevicesAdapter.setOnItemClickListener { deviceToConnect ->

            device = deviceToConnect

            bluetoothAdapter?.getProfileProxy(
                requireContext,
                object : BluetoothProfile.ServiceListener{
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {

                        a2dp = proxy as BluetoothA2dp

//                        headset = proxy

                        try{
                            a2dp.javaClass
                                .getMethod("connect", BluetoothDevice::class.java)
                                .invoke(a2dp, device)
                        } catch (e : Exception) {
                            Log.d("CHECKTAGS", e.stackTraceToString())
                        }

                    }

                    override fun onServiceDisconnected(profile: Int) {

                        device?.let {
                            disConnectUsingBluetoothA2dp(it)
                        }



                    }
                }, BluetoothProfile.A2DP
            )

        }


    }

    @SuppressLint("MissingPermission")
    private fun disconnect(device: BluetoothDevice) {

        val serviceListener: BluetoothProfile.ServiceListener = object :
            BluetoothProfile.ServiceListener {
            override fun onServiceDisconnected(profile: Int) {}

            @SuppressLint("DiscouragedPrivateApi")
            override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {


                val disconnect = BluetoothA2dp::class.java.getDeclaredMethod(
                    "disconnect",
                    BluetoothDevice::class.java
                )
                disconnect.isAccessible = true
                disconnect.invoke(proxy, device)
                bluetoothAdapter?.closeProfileProxy(profile, proxy)
            }
        }

        lifecycleScope.launch {
            while (true){
                delay(50)
                bluetoothAdapter?.getProfileProxy(context, serviceListener, BluetoothProfile.A2DP)
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun isBluetoothHeadsetConnected(): Boolean {
        return bluetoothAdapter?.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothAdapter.STATE_CONNECTING
    }


    @SuppressLint("MissingPermission")
    private fun disConnectUsingBluetoothA2dp(deviceToConnect: BluetoothDevice){

//        try {


            Log.d("CHECKTAGS", "disconnecting : ${deviceToConnect.name}")


            a2dp.javaClass.getMethod(
                "disconnect",
                BluetoothDevice::class.java
            ).invoke(a2dp, deviceToConnect)
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, a2dp)

//            headset.javaClass.getMethod(
//                "disconnect",
//                BluetoothDevice::class.java
//            ).invoke(headset, deviceToConnect)
//            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.HEADSET, headset)

//           val devices = bluetoothManager.getConnectedDevices(BluetoothProfile.A2DP)



//        } catch (e: Exception) {
//            Log.d("CHECKTAGS", "error?")
//            Log.d("CHECKTAGS", e.printStackTrace().toString())
//            e.printStackTrace()
//        }
    }

    override fun onStop() {
        super.onStop()
        bind.rvPairedDevices.adapter = null
        context.unregisterReceiver(connectingDeviceReceiver)
        _bind = null

    }

}