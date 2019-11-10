package com.h.raspberrysensor.core.services

import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.STATE_CONNECTED
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.content.Context
import com.h.raspberrysensor.RaspberrySensorApp
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SCAN_PERIOD: Long = 10

class BluetoothService @Inject constructor(
    private val raspberrySensorApp: RaspberrySensorApp
) {

    private var mScanning: Boolean = false
    private val bleStateSubject = PublishSubject.create<BLEState>()
    private val bleGattStateSubject = PublishSubject.create<BLEGattState>()

    private var connectionState = STATE_DISCONNECTED

    private var bluetoothGatt: BluetoothGatt? = null

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = raspberrySensorApp.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    //leScan return a callback with 3 parameters, then inform bleStateSubject that the scanner have found a device
    private val leScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        Timber.d(device.toString())
        bleStateSubject.onNext(BLEState.DeviceFound(device))
    }

    fun checkBluethoothEnabled(): Single<Boolean> = Single.just(bluetoothAdapter?.isEnabled ?: false)

    fun connectToBluetoothDevice(address: String): Single<BluetoothDevice> {
        return Single.fromCallable {
            bluetoothAdapter?.getRemoteDevice(address) ?: throw IllegalStateException("Bluetooth adapter is null")
        }
    }

    fun stopScan() {
        bluetoothAdapter?.stopLeScan(leScanCallback)
    }

    fun scanLeDevice(): Observable<BLEState> {
        return Observable.just(false)
            // Observable emits a signal every 10sec
            .delay(SCAN_PERIOD, TimeUnit.SECONDS)
            //Boolean begin with true as soon as function is called, so bluetooth start scanning for 10sec
            .startWith(true)
            .map {
                if (it) {
                    //if scan has started by an action, start LeScan
                    mScanning = true
                    val hasStarted = bluetoothAdapter?.startLeScan(leScanCallback)
                    Timber.d(hasStarted.toString())
                    //Inform BLEState that the scanner is running
                    BLEState.Started
                } else {
                    //if SCAN_PERIOD has ended, stop LeScan
                    mScanning = false
                    bluetoothAdapter?.stopLeScan(leScanCallback)
                    //Inform BLEState that the scanner has stopped
                    BLEState.Stopped
                }
            }
            //Create an observable with both
            .mergeWith(bleStateSubject)
    }

    //Connect to GATT Server of selected device
    fun connectGattServer(device: BluetoothDevice): Observable<BLEGattState> {
        bluetoothGatt = device.connectGatt(raspberrySensorApp, false, gattCallback)
        return bleGattStateSubject
    }

    fun readServiceCharacteristic(service: BluetoothGattService): Completable {
        return Completable.fromAction {
            service.characteristics.forEach { characteristic ->
                bluetoothGatt?.readCharacteristic(characteristic)
            }
        }
    }

    // Various callback methods defined by the BLE API.
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionState = STATE_CONNECTED
                    Timber.i("Connected to GATT server.")
                    Timber.i(String.format("Attempting to start service discovery: " + bluetoothGatt?.discoverServices()))
                    bleGattStateSubject.onNext(BLEGattState.Connected)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionState = STATE_DISCONNECTED
                    Timber.i("Disconnected from GATT server.")
                    bleGattStateSubject.onNext(BLEGattState.Disconnected)
                }
            }
        }

        // New services discovered
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    bleGattStateSubject.onNext(BLEGattState.ServicesDiscovered(gatt.services))
                }
                else -> Timber.w("onServicesDiscovered received: $status")
            }
        }

        // Result of a characteristic read operation
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            val stringStatus = when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    bleGattStateSubject.onNext(BLEGattState.CharacteristicRead(characteristic))
                    Timber.d("onCharacteristicRead success")
                    status
                }
                BluetoothGatt.GATT_FAILURE -> BluetoothGatt.GATT_FAILURE::class.simpleName
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION::class.simpleName
                BluetoothGatt.GATT_INVALID_OFFSET -> BluetoothGatt.GATT_INVALID_OFFSET::class.simpleName
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH::class.simpleName
                BluetoothGatt.GATT_READ_NOT_PERMITTED -> BluetoothGatt.GATT_READ_NOT_PERMITTED::class.simpleName
                else -> "unknown status $status"
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                gatt.disconnect()
                Timber.d("onCharacteristicRead failure: $stringStatus")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
        }
    }
}