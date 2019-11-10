package com.h.raspberrysensor.features.main

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.h.raspberrysensor.R
import com.h.raspberrysensor.utils.inflate
import kotlinx.android.synthetic.main.view_gatt_service.view.*

class DeviceServiceListAdapter(val actions: DeviceServiceSelectedListener, val context: Context) :
    ListAdapter<BluetoothGattService, DeviceServiceListAdapter.DeviceServiceListViewHolder>
        (BluetoothGattServiceDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceServiceListViewHolder {
        return DeviceServiceListViewHolder(parent)
    }

    override fun onBindViewHolder(holderService: DeviceServiceListViewHolder, position: Int) {
        holderService.bind(getItem(position))
    }

    override fun submitList(list: List<BluetoothGattService>?) {
        super.submitList(if (list != null) ArrayList(list) else null)
    }

    interface DeviceServiceSelectedListener {
        fun onDeviceServiceSelected(service: BluetoothGattService)
    }

    inner class DeviceServiceListViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.view_gatt_service)) {

        init {
            itemView.setOnClickListener {
                actions.onDeviceServiceSelected(
                    this@DeviceServiceListAdapter.getItem(
                        adapterPosition
                    )
                )
            }
        }

        fun bind(viewItem: BluetoothGattService) {
            itemView.tv_gatt_uuid.text =
                context.resources.getString(R.string.gatt_uuid, viewItem.uuid.toString())
            itemView.tv_gatt_characteristic_discovered.text = context.resources.getString(
                R.string.gatt_characteristic_discovered,
                viewItem.characteristics.size.toString()
            )
            viewItem.characteristics.forEach {
                itemView.tv_gatt_characteristic_id.text = context.resources.getString(R.string.gatt_characteristic_id, it.properties.toString())
            }
            itemView.tv_gatt_status.text =
                context.resources.getString(R.string.gatt_status, viewItem.type.toString())
        }
    }

    class BluetoothGattServiceDiffUtilCallback : DiffUtil.ItemCallback<BluetoothGattService>() {
        override fun areItemsTheSame(
            oldItem: BluetoothGattService,
            newItem: BluetoothGattService
        ): Boolean {
            return oldItem.uuid == newItem.uuid
        }

        override fun areContentsTheSame(
            oldItem: BluetoothGattService,
            newItem: BluetoothGattService
        ): Boolean {
            return oldItem.uuid == newItem.uuid
        }
    }
}
