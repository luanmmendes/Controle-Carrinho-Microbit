package com.example.microbitcontroller.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.Queue

enum class BleConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    DISCOVERING_SERVICES,
    CONFIGURING_PINS,
    READY,
    ERROR
}

@SuppressLint("MissingPermission")
class MicrobitBleManager(private val context: Context) {

    companion object {
        private const val TAG = "MicrobitBleManager"
    }

    private var bluetoothGatt: BluetoothGatt? = null

    private val _status = MutableStateFlow(BleConnectionStatus.DISCONNECTED)
    val status: StateFlow<BleConnectionStatus> = _status.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    // Estados dos pinos digitais P14, P15, P12, P13 (true = HIGH / false = LOW)
    private val _pinStates = MutableStateFlow(mapOf(14 to false, 15 to false, 12 to false, 13 to false))
    val pinStates: StateFlow<Map<Int, Boolean>> = _pinStates.asStateFlow()

    // Fila sequencial de operações GATT
    private val operationQueue: Queue<() -> Unit> = LinkedList()
    private var isBusy = false

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d(TAG, "onConnectionStateChange: status=$status, newState=$newState")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                _status.value = BleConnectionStatus.ERROR
                cleanup()
                return
            }

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _status.value = BleConnectionStatus.DISCOVERING_SERVICES
                    Log.i(TAG, "Conectado com sucesso. Iniciando descoberta de serviços...")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Dispositivo desconectado.")
                    _status.value = BleConnectionStatus.DISCONNECTED
                    cleanup()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Serviços descobertos com sucesso.")
                val service = gatt.getService(MicrobitGattAttributes.PIN_SERVICE_UUID)
                if (service != null) {
                    _status.value = BleConnectionStatus.CONFIGURING_PINS
                    configurePinsAsDigitalOutputs()
                } else {
                    Log.e(TAG, "Pin I/O Service não encontrado no micro:bit!")
                    _status.value = BleConnectionStatus.ERROR
                }
            } else {
                Log.e(TAG, "Falha na descoberta de serviços: $status")
                _status.value = BleConnectionStatus.ERROR
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d(TAG, "onCharacteristicWrite: UUID=${characteristic.uuid}, status=$status")
            isBusy = false
            executeNext()
        }
    }

    fun connect(device: BluetoothDevice) {
        _connectedDeviceName.value = device.name ?: device.address
        _status.value = BleConnectionStatus.CONNECTING

        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
    }

    fun disconnect() {
        bluetoothGatt?.let { gatt ->
            try {
                gatt.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "Erro ao desconectar: ${e.message}")
            }
        }
    }

    private fun cleanup() {
        operationQueue.clear()
        isBusy = false
        try {
            bluetoothGatt?.close()
        } catch (_: Exception) {}
        bluetoothGatt = null
        _connectedDeviceName.value = null
        // Reseta estados locais
        _pinStates.value = mapOf(14 to false, 15 to false, 12 to false, 13 to false)
    }

    private fun enqueue(operation: () -> Unit) {
        synchronized(operationQueue) {
            operationQueue.add(operation)
            if (!isBusy) {
                executeNext()
            }
        }
    }

    private fun executeNext() {
        synchronized(operationQueue) {
            val next = operationQueue.poll()
            if (next != null) {
                isBusy = true
                next()
            } else {
                isBusy = false
                // Se a fila esvaziou e estávamos configurando, o micro:bit está pronto para uso
                if (_status.value == BleConnectionStatus.CONFIGURING_PINS) {
                    _status.value = BleConnectionStatus.READY
                    Log.i(TAG, "micro:bit configurado e pronto para receber comandos!")
                }
            }
        }
    }

    /**
     * Configura P0, P1 e P2 como saídas digitais:
     * - Pin I/O Config: Bitmask 24-bits -> 0 = Saída, 1 = Entrada
     * - Pin AD Config: Bitmask 24-bits -> 0 = Digital, 1 = Analógico
     */
    private fun configurePinsAsDigitalOutputs() {
        // Passo 1: Configurar direção I/O
        enqueue {
            val service = bluetoothGatt?.getService(MicrobitGattAttributes.PIN_SERVICE_UUID)
            val ioConfig = service?.getCharacteristic(MicrobitGattAttributes.PIN_IO_CONFIG_UUID)
            if (ioConfig != null) {
                writeCharacteristicInternal(ioConfig, byteArrayOf(0x00, 0x00, 0x00))
            } else {
                Log.w(TAG, "Característica PIN_IO_CONFIG não encontrada.")
                isBusy = false
                executeNext()
            }
        }

        // Passo 2: Configurar modo Digital
        enqueue {
            val service = bluetoothGatt?.getService(MicrobitGattAttributes.PIN_SERVICE_UUID)
            val adConfig = service?.getCharacteristic(MicrobitGattAttributes.PIN_AD_CONFIG_UUID)
            if (adConfig != null) {
                writeCharacteristicInternal(adConfig, byteArrayOf(0x00, 0x00, 0x00))
            } else {
                Log.w(TAG, "Característica PIN_AD_CONFIG não encontrada.")
                isBusy = false
                executeNext()
            }
        }
    }

    /**
     * Altera o estado do pino (P0, P1, P2) para HIGH (1) ou LOW (0)
     */
    fun setPinState(pinIndex: Int, isHigh: Boolean) {
        enqueue {
            val service = bluetoothGatt?.getService(MicrobitGattAttributes.PIN_SERVICE_UUID)
            val pinData = service?.getCharacteristic(MicrobitGattAttributes.PIN_DATA_UUID)
            if (pinData != null) {
                val valueByte: Byte = if (isHigh) 0x01 else 0x00
                val payload = byteArrayOf(pinIndex.toByte(), valueByte)

                writeCharacteristicInternal(pinData, payload)

                // Atualiza o estado visual local
                val updatedMap = _pinStates.value.toMutableMap()
                updatedMap[pinIndex] = isHigh
                _pinStates.value = updatedMap
            } else {
                Log.w(TAG, "Característica PIN_DATA não encontrada.")
                isBusy = false
                executeNext()
            }
        }
    }

    /**
     * Envia um valor analógico (PWM) entre 0 e 1024 para um pino específico (por padrão o pino 12).
     *
     * No protocolo GATT oficial do BBC micro:bit (Pin I/O Service), a escrita analógica é feita
     * através da característica PWM Control (UUID: E95DD822-251D-470A-A062-FA1922DFA9A8).
     *
     * Estrutura do payload (7 bytes Little-Endian):
     * - Byte 0: Número do pino (uint8, ex: 12)
     * - Bytes 1-2: Valor analógico (uint16, 0 a 1024)
     * - Bytes 3-6: Período do PWM em microssegundos (uint32, padrão: 20000us = 50Hz)
     *
     * @param pinIndex Índice do pino (ex: 12)
     * @param value Valor analógico entre 0 e 1024
     * @param periodUs Período da modulação em microssegundos (padrão de 20.000µs / 50Hz)
     */
    fun setPinAnalogValue(pinIndex: Int = 12, value: Int, periodUs: Int = 20000) {
        val clampedValue = value.coerceIn(0, 1024)

        enqueue {
            val service = bluetoothGatt?.getService(MicrobitGattAttributes.PIN_SERVICE_UUID)
            val pwmControl = service?.getCharacteristic(MicrobitGattAttributes.PWM_CONTROL_UUID)

            if (pwmControl != null) {
                val payload = byteArrayOf(
                    pinIndex.toByte(),
                    (clampedValue and 0xFF).toByte(),
                    ((clampedValue shr 8) and 0xFF).toByte(),
                    (periodUs and 0xFF).toByte(),
                    ((periodUs shr 8) and 0xFF).toByte(),
                    ((periodUs shr 16) and 0xFF).toByte(),
                    ((periodUs shr 24) and 0xFF).toByte()
                )

                writeCharacteristicInternal(pwmControl, payload)
                Log.d(TAG, "Enviado valor analógico $clampedValue (PWM) para o pino $pinIndex")
            } else {
                Log.w(TAG, "Característica PWM_CONTROL não encontrada no micro:bit.")
                isBusy = false
                executeNext()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun writeCharacteristicInternal(
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray
    ) {
        val gatt = bluetoothGatt
        if (gatt == null) {
            isBusy = false
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                characteristic,
                payload,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
        } else {
            characteristic.value = payload
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            gatt.writeCharacteristic(characteristic)
        }
    }
}
