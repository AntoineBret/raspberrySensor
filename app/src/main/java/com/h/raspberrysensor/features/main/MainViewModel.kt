package com.h.raspberrysensor.features.main

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.h.raspberrysensor.core.services.BLEGattState
import com.h.raspberrysensor.core.services.BluetoothService
import com.h.raspberrysensor.core.services.PreferenceService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val bluetoothService: BluetoothService,
    private val preferenceService: PreferenceService
) : ViewModel() {

    private val mainStateData: MutableLiveData<MainState> = MutableLiveData()
    private var bluetoothDevice: BluetoothDevice? = null
    private val disposable = CompositeDisposable()

    private val deviceServiceLiveData: MutableLiveData<List<BluetoothGattService>> =
        MutableLiveData()
    private val deviceServiceList = mutableListOf<BluetoothGattService>()

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    /**
     * Ask to bluetoothService if bluetooth is enabled, if yes, subscribe to values
     * */
    fun viewResumed() {
        bluetoothService.checkBluethoothEnabled()
            .map { MainState.BluetoothEnabled(it) }
            .subscribe({ views ->
                mainStateData.value = views
                loadBluetoothDevice()
            }) { throwable ->
                Timber.e(throwable)
            }.addTo(disposable)
    }

    private fun loadBluetoothDevice() {
        val address = preferenceService.getBluetoothDeviceAddress()
        address?.let {
            bluetoothService.connectToBluetoothDevice(it)
                .map { device ->
                    bluetoothDevice = device
                    MainState.BluetootDeviceLoaded(device)
                }
                .subscribe({ device ->
                    mainStateData.value = device
                }) { throwable ->
                    Timber.e(throwable)
                }.addTo(disposable)
        }
    }

    fun getMainState(): LiveData<MainState> {
        return mainStateData
    }

    fun deviceClicked() {
        connectGatt(
            bluetoothDevice ?: throw IllegalStateException("BluetoothDevice is null or empty")
        )
    }

    fun getDeviceResult(device: BluetoothDevice) {
        preferenceService.saveBluetoothDeviceAddress(device.address)
        connectGatt(device)
    }

    private fun connectGatt(device: BluetoothDevice) {
        bluetoothService.connectGattServer(device)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ state ->
                when (state) {
                    BLEGattState.Connected -> Timber.d("BLEGatt connected")
                    is BLEGattState.ServicesDiscovered -> {
                        deviceServiceList.clear()
                        deviceServiceList.addAll(state.services)
                        deviceServiceLiveData.value = deviceServiceList
                        state.services.forEach { readCharacteristics(it) }
                    }
                    is BLEGattState.CharacteristicRead -> {
                        Timber.d("Value : %s", state.characteristic.getStringValue(0))
                    }
                    BLEGattState.Disconnected -> Timber.d("BLEGatt disconnected")
                }
            }) { throwable ->
                Timber.e(throwable)
            }.addTo(disposable)
    }

    private fun readCharacteristics(service: BluetoothGattService) {
        bluetoothService.readServiceCharacteristic(service)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("readCharacteristics success")
            }) { throwable ->
                Timber.e(throwable)
            }.addTo(disposable)
    }

    fun getDeviceServiceLiveData(): MutableLiveData<List<BluetoothGattService>> =
        deviceServiceLiveData
}