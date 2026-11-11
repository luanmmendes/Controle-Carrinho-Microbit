package com.example.microbitcontroller.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class BleScanner(private val bluetoothAdapter: BluetoothAdapter?) {

    private val scanner = bluetoothAdapter?.bluetoothLeScanner
    private val handler = Handler(Looper.getMainLooper())

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BleDeviceModel>>(emptyList())
    val scannedDevices: StateFlow<List<BleDeviceModel>> = _scannedDevices.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result ?: return
            val device = result.device
            val name = result.scanRecord?.deviceName ?: device.name ?: "Desconhecido"

            // Aceita dispositivos que tenham o nome micro:bit ou que anunciem dados
            val deviceModel = BleDeviceModel(
                name = name,
                address = device.address,
                rssi = result.rssi,
                device = device
            )

            val currentList = _scannedDevices.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.address == device.address }

            if (existingIndex >= 0) {
                currentList[existingIndex] = deviceModel
            } else {
                currentList.add(deviceModel)
            }

            _scannedDevices.value = currentList
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
        }
    }

    fun startScan(timeoutMillis: Long = 10000L) {
        if (scanner == null || _isScanning.value) return

        _scannedDevices.value = emptyList()
        _isScanning.value = true

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        // Opcional: Filtros podem ser adicionados aqui se desejado
        val filters = mutableListOf<ScanFilter>()

        scanner.startScan(filters, settings, scanCallback)

        // Parar automaticamente após o timeout
        handler.postDelayed({
            stopScan()
        }, timeoutMillis)
    }

    fun stopScan() {
        if (!_isScanning.value || scanner == null) return
        try {
            scanner.stopScan(scanCallback)
        } catch (_: Exception) {}
        _isScanning.value = false
    }
}
