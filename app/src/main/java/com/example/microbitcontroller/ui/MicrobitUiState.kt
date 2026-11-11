package com.example.microbitcontroller.ui

import com.example.microbitcontroller.ble.BleConnectionStatus
import com.example.microbitcontroller.ble.BleDeviceModel

data class MicrobitUiState(
    val isScanning: Boolean = false,
    val scannedDevices: List<BleDeviceModel> = emptyList(),
    val connectionStatus: BleConnectionStatus = BleConnectionStatus.DISCONNECTED,
    val connectedDeviceName: String? = null,
    val pinStates: Map<Int, Boolean> = mapOf(14 to false, 15 to false, 12 to false, 13 to false),
    val pin12AnalogValue: Int = 0,
    val pin12AnalogInput: String = "0",
    val pin13AnalogValue: Int = 0,
    val pin13AnalogInput: String = "0",
    val pinAccumulators: Map<Int, Double> = emptyMap(),
    val pinIncrementInputs: Map<Int, String> = emptyMap(),
    val errorMessage: String? = null
)
