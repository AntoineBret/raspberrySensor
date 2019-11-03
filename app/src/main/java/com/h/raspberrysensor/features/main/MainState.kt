package com.h.raspberrysensor.features.main

import android.bluetooth.BluetoothDevice

sealed class MainState {
    data class BluetoothEnabled(val isEnabled: Boolean) : MainState()
    data class BluetootDeviceLoaded(val bluetoothDevice: BluetoothDevice) : MainState()

}