package com.example.microbitcontroller.ui

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.microbitcontroller.ble.BleScanner
import com.example.microbitcontroller.ble.MicrobitBleManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MicrobitViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private val bleScanner = BleScanner(bluetoothAdapter)
    private val bleManager = MicrobitBleManager(application)

    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _pin12AnalogValue = MutableStateFlow(0)
    private val _pin13AnalogValue = MutableStateFlow(0)

    val uiState: StateFlow<MicrobitUiState> = combine(
        combine(bleScanner.isScanning, bleScanner.scannedDevices, ::Pair),
        combine(bleManager.status, bleManager.connectedDeviceName, bleManager.pinStates, ::Triple),
        combine(_pin12AnalogValue, _pin13AnalogValue, _errorMessage, ::Triple)
    ) { (isScanning, scannedDevices), (status, deviceName, pinStates), (analog12, analog13, error) ->
        MicrobitUiState(
            isScanning = isScanning,
            scannedDevices = scannedDevices,
            connectionStatus = status,
            connectedDeviceName = deviceName,
            pinStates = pinStates,
            pin12AnalogValue = analog12,
            pin13AnalogValue = analog13,
            errorMessage = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MicrobitUiState()
    )

    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _errorMessage.value = "Bluetooth está desativado no aparelho."
            return
        }
        _errorMessage.value = null
        bleScanner.startScan()
    }

    fun stopScanning() {
        bleScanner.stopScan()
    }

    fun connectToDevice(device: BluetoothDevice) {
        bleScanner.stopScan()
        bleManager.connect(device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun togglePin(pinIndex: Int) {
        val currentState = uiState.value.pinStates[pinIndex] ?: false
        bleManager.setPinState(pinIndex, !currentState)
    }

    /**
     * Envia um valor analógico (0 a 1024) para um pino (ex: pino 12 ou 13)
     */
    fun setPinAnalogValue(pinIndex: Int = 12, value: Int, periodUs: Int = 20000) {
        val clamped = value.coerceIn(0, 1024)
        if (pinIndex == 12) {
            _pin12AnalogValue.value = clamped
        } else if (pinIndex == 13) {
            _pin13AnalogValue.value = clamped
        }
        bleManager.setPinAnalogValue(pinIndex, clamped, periodUs)
    }

    fun setPin(pinIndex: Int, isHigh: Boolean) {
        bleManager.setPinState(pinIndex, isHigh)
    }

    override fun onCleared() {
        super.onCleared()
        bleScanner.stopScan()
        bleManager.disconnect()
    }
}
