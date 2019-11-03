package com.h.raspberrysensor.core.services

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService

sealed class BLEState {
    object Started : BLEState()
    data class DeviceFound(val device: BluetoothDevice) : BLEState()
    object Stopped : BLEState()
}

sealed class BLEGattState {
    object Connected : BLEGattState()
    data class ServicesDiscovered(val services: List<BluetoothGattService>) : BLEGattState()
    data class CharacteristicRead(val characteristic: BluetoothGattCharacteristic) : BLEGattState()
    object Disconnected : BLEGattState()
}