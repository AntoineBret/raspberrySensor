package com.h.raspberrysensor.features.discovery

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.h.raspberrysensor.R
import com.h.raspberrysensor.RaspberrySensorApp
import com.h.raspberrysensor.utils.getBluetoothMajorClassString
import com.h.raspberrysensor.utils.inflate
import kotlinx.android.synthetic.main.view_discovery_device_service.view.*

class LeDeviceListAdapter(val actions: OnDeviceSelectedListener, val context: Context) :
    ListAdapter<BluetoothDevice, LeDeviceListAdapter.LeDeviceListViewHolder>
        (BluetoothDeviceDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeDeviceListViewHolder {
        return LeDeviceListViewHolder(parent)
    }

    override fun onBindViewHolder(holder: LeDeviceListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: List<BluetoothDevice>?) {
        super.submitList(if (list != null) ArrayList(list) else null)
    }

    interface OnDeviceSelectedListener {
        fun onDeviceSelected(device: BluetoothDevice)
    }

    inner class LeDeviceListViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.view_discovery_device_service)) {

        init {
            itemView.setOnClickListener {
                actions.onDeviceSelected(this@LeDeviceListAdapter.getItem(adapterPosition)) }
        }

        fun bind(viewItem: BluetoothDevice) {
            itemView.tv_device.text = context.resources.getString(R.string.bluetooth_device_address, viewItem.address.toString())
            itemView.tv_device_name.text = context.resources.getString(R.string.bluetooth_device_name, viewItem.name)
            itemView.tv_device_bluetoothclass.text = context.resources.getString(R.string.bluetooth_device_class, getBluetoothMajorClassString(viewItem.bluetoothClass.majorDeviceClass))

            itemView.tv_device_name.visibility = if (viewItem.name != null && viewItem.name.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    class BluetoothDeviceDiffUtilCallback : DiffUtil.ItemCallback<BluetoothDevice>() {
        override fun areItemsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: BluetoothDevice, newItem: BluetoothDevice): Boolean {
            return oldItem.name == newItem.name
        }
    }
}
