package com.example.microbitcontroller.ble

import android.bluetooth.BluetoothDevice

data class BleDeviceModel(
    val name: String,
    val address: String,
    val rssi: Int,
    val device: BluetoothDevice
)
