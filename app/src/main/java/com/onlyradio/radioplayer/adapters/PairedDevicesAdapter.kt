package com.onlyradio.radioplayer.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.onlyradio.radioplayer.databinding.ItemTextBinding

class PairedDevicesAdapter : RecyclerView.Adapter<PairedDevicesAdapter.DeviceViewHolder>(){

    private var devicesList: ArrayList<BluetoothDevice> = ArrayList()

    class DeviceViewHolder (val bind : ItemTextBinding) : RecyclerView.ViewHolder(bind.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        return DeviceViewHolder(
            ItemTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devicesList[position]
        holder.bind.tvText.text = device.name


        holder.itemView.setOnClickListener {

            onItemClickListener?.let { click ->

                click(device)
            }
        }
    }


    private var onItemClickListener : ((device : BluetoothDevice) -> Unit)? = null

    fun setOnItemClickListener(listener : (device : BluetoothDevice) -> Unit){
        onItemClickListener = listener
    }


    override fun getItemCount(): Int {
       return devicesList.size
    }

    fun addItems(list: MutableSet<BluetoothDevice>) {
        devicesList.clear()
        devicesList.addAll(list)
        notifyDataSetChanged()
    }

}