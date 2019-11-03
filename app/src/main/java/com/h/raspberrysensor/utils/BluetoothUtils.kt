package com.h.raspberrysensor.utils

import android.bluetooth.BluetoothClass

fun getBluetoothMajorClassString(major: Int): String {
    return when (major) {
        BluetoothClass.Device.Major.MISC -> "MISC"
        BluetoothClass.Device.Major.COMPUTER -> "COMPUTER"
        BluetoothClass.Device.Major.PHONE -> "PHONE"
        BluetoothClass.Device.Major.NETWORKING -> "NETWORKING"
        BluetoothClass.Device.Major.AUDIO_VIDEO -> "AUDIO_VIDEO"
        BluetoothClass.Device.Major.PERIPHERAL -> "PERIPHERAL"
        BluetoothClass.Device.Major.IMAGING -> "IMAGING"
        BluetoothClass.Device.Major.WEARABLE -> "WEARABLE"
        BluetoothClass.Device.Major.TOY -> "TOY"
        BluetoothClass.Device.Major.HEALTH -> "HEALTH"
        BluetoothClass.Device.Major.UNCATEGORIZED -> "UNCATEGORIZED"

        else -> "UNCATEGORIZED"
    }
}