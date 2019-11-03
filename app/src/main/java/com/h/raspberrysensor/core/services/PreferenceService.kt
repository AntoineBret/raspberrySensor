package com.h.raspberrysensor.core.services

import android.content.SharedPreferences
import javax.inject.Inject

private const val BLUETOOTH_DEVICE_KEY = "BLUETOOTH_DEVICE"

class PreferenceService @Inject constructor(private val sharedPreferences: SharedPreferences) {

    fun saveBluetoothDeviceAddress(address: String) {
        with(sharedPreferences.edit()) {
            putString(BLUETOOTH_DEVICE_KEY, address)
            commit()
        }
    }

    fun getBluetoothDeviceAddress(): String? {
        return sharedPreferences.getString(BLUETOOTH_DEVICE_KEY, null)
    }
}