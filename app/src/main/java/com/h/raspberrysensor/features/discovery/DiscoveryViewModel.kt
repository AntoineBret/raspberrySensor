package com.h.raspberrysensor.features.discovery

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.h.raspberrysensor.core.services.BLEState
import com.h.raspberrysensor.core.services.BluetoothService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class DiscoveryViewModel @Inject constructor(private val bluetoothService: BluetoothService) : ViewModel() {

    private val disposable = CompositeDisposable()
    private val deviceLiveData: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    private val deviceList = mutableListOf<BluetoothDevice>()

    override fun onCleared() {
        super.onCleared()
        bluetoothService.stopScan()
        disposable.dispose()
    }

    /**
     * When bluetooth is enable, start scanning
     */
    fun getScanLeDevice() {
        bluetoothService.scanLeDevice()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ state ->
                when (state) {
                    BLEState.Started -> Timber.d("scan started")
                    is BLEState.DeviceFound -> {
                        if (!deviceList.contains(state.device)) {
                            deviceList.add(state.device)
                            deviceLiveData.value = deviceList
                        }
                    }
                    BLEState.Stopped -> Timber.d("scan stopped")
                }
            }) { throwable ->
                Timber.e(throwable)
            }.addTo(disposable)
    }

    fun getDeviceLiveData(): LiveData<List<BluetoothDevice>> = deviceLiveData
}

