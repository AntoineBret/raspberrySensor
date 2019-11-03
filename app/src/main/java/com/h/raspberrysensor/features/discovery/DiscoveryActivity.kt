package com.h.raspberrysensor.features.discovery

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.h.raspberrysensor.R
import com.h.raspberrysensor.di.Injectable
import kotlinx.android.synthetic.main.activity_discovery.*
import javax.inject.Inject


class DiscoveryActivity : AppCompatActivity(), Injectable, LeDeviceListAdapter.OnDeviceSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DiscoveryViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discovery)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DiscoveryViewModel::class.java)

        viewModel.getDeviceLiveData().observe(this, Observer<List<BluetoothDevice>> { devices ->
            devices?.let {
                updateList(it)
            }
        })

        initRecyclerView()
        viewModel.getScanLeDevice()
    }

    private fun initRecyclerView() {
        devicesRecyclerView.adapter = LeDeviceListAdapter(this)
        devicesRecyclerView.apply {
            setHasFixedSize(true)
            val linearLayout = LinearLayoutManager(context)
            layoutManager = linearLayout
        }
    }

    private fun updateList(devices: List<BluetoothDevice>) {
        (devicesRecyclerView.adapter as LeDeviceListAdapter).submitList(devices)
    }

    override fun onDeviceSelected(device: BluetoothDevice) {
        val returnIntent = Intent()
        returnIntent.putExtra("device", device)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
