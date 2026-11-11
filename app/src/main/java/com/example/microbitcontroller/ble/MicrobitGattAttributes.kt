package com.example.microbitcontroller.ble

import java.util.UUID

/**
 * UUIDs oficiais do Pin I/O Service do BBC micro:bit
 * Documentação: https://tech.microbit.org/hardware/powersupply/ / Lancaster Bluetooth Profile
 */
object MicrobitGattAttributes {
    // Pin I/O Service UUID
    val PIN_SERVICE_UUID: UUID = UUID.fromString("E95D127B-251D-470A-A062-FA1922DFA9A8")

    // Características:
    // Pin I/O Configuration: Bitmask 24-bit (0 = Saída, 1 = Entrada)
    val PIN_IO_CONFIG_UUID: UUID = UUID.fromString("E95DB9FE-251D-470A-A062-FA1922DFA9A8")

    // Pin AD Configuration: Bitmask 24-bit (0 = Digital, 1 = Analógico)
    val PIN_AD_CONFIG_UUID: UUID = UUID.fromString("E95D5899-251D-470A-A062-FA1922DFA9A8")

    // Pin Data: Leitura/Escrita dos estados [PinNumber, Value]
    val PIN_DATA_UUID: UUID = UUID.fromString("E95D8D00-251D-470A-A062-FA1922DFA9A8")

    // PWM Control: Configuração de frequência e duty cycle
    val PWM_CONTROL_UUID: UUID = UUID.fromString("E95DD822-251D-470A-A062-FA1922DFA9A8")
}
