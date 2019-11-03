package com.h.raspberrysensor.features.main

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.h.raspberrysensor.const.Constants.Companion.DEVICE_RESULT
import com.h.raspberrysensor.di.Injectable
import com.h.raspberrysensor.features.discovery.DiscoveryActivity
import com.h.raspberrysensor.utils.getBluetoothMajorClassString
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), Injectable, DeviceServiceListAdapter.DeviceServiceSelectedListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.h.raspberrysensor.R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        viewModel.getDeviceServiceLiveData().observe(this, Observer<List<BluetoothGattService>> { services ->
            services?.let {
                updateList(it)
            }
        })

        initRecyclerView()

        viewModel.getMainState().observe(this, Observer<MainState> {
            render(it)
        })

        viewModel.viewResumed()

        /**
         * Ask permissions (only for first co)
         **/
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            101
        )

        fab_device.setOnClickListener {
            startActivityForResult(Intent(this, DiscoveryActivity::class.java), 100)
        }

        deviceGroup.setOnClickListener {
            viewModel.deviceClicked()
        }
    }

    private fun initRecyclerView() {
        deviceServiceRecyclerView.adapter = DeviceServiceListAdapter(this)
        deviceServiceRecyclerView.apply {
            setHasFixedSize(true)
            val linearLayout = LinearLayoutManager(context)
            layoutManager = linearLayout
        }
    }

    private fun updateList(services: List<BluetoothGattService>) {
        (deviceServiceRecyclerView.adapter as DeviceServiceListAdapter).submitList(services)
    }

    /**
     * When app launch, check if the bluetooth is enabled, if not, ask to user to start it
     */
    private fun render(mainState: MainState) {
        when (mainState) {
            is MainState.BluetoothEnabled -> {
                if (!mainState.isEnabled) {
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, DEVICE_RESULT)
                }
            }
            is MainState.BluetootDeviceLoaded -> {
                displayDevice(mainState.bluetoothDevice)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DEVICE_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data?.getParcelableExtra<BluetoothDevice>("device") ?: throw IllegalStateException("BluetoothDevice is null or empty")
                displayDevice(result)
                viewModel.getDeviceResult(result)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    private fun displayDevice(device: BluetoothDevice) {
        tv_device.text = device.address
        tv_device_name.text = device.name
        tv_device_bluetoothclass.text = getBluetoothMajorClassString(device.bluetoothClass.majorDeviceClass)
    }

    override fun onDeviceServiceSelected(service: BluetoothGattService) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
