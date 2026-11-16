package com.example.microbitcontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.microbitcontroller.ble.BleConnectionStatus
import com.example.microbitcontroller.ui.theme.LedActive
import com.example.microbitcontroller.ui.theme.LedInactive
import java.util.Locale
import kotlin.Int

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinControlScreen(
    deviceName: String?,
    status: BleConnectionStatus,
    pinStates: Map<Int, Boolean>,
    pin12AnalogValue: Int = 0,
    pin12AnalogInput: String = "0",
    pin13AnalogValue: Int = 0,
    pin13AnalogInput: String = "0",
    onPin12InputChange: (String) -> Unit = {},
    onApplyPin12Analog: (Int?) -> Unit = {},
    onPin13InputChange: (String) -> Unit = {},
    onApplyPin13Analog: (Int?) -> Unit = {},
    onTogglePin: (Int) -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deviceName ?: "micro:bit Conectado") },
                navigationIcon = {
                    IconButton(onClick = onDisconnect) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Desconectar")
                    }
                },
                actions = {
                    TextButton(onClick = onDisconnect) {
                        Text("Desconectar", color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (status) {
                        BleConnectionStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                        BleConnectionStatus.CONFIGURING_PINS -> MaterialTheme.colorScheme.secondaryContainer
                        BleConnectionStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Status da Conexão",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = when (status) {
                                BleConnectionStatus.READY -> "Pronto (Pinos Ativos)"
                                BleConnectionStatus.CONFIGURING_PINS -> "Configurando Pinos via BLE..."
                                BleConnectionStatus.DISCOVERING_SERVICES -> "Descobrindo Serviços GATT..."
                                BleConnectionStatus.CONNECTING -> "Conectando..."
                                BleConnectionStatus.ERROR -> "Erro na Conexão BLE"
                                BleConnectionStatus.DISCONNECTED -> "Desconectado"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Text(
                text = "Saídas Analógicas (PWM 0-1024)",
                style = MaterialTheme.typography.titleLarge
            )

            // Pino 12: Controle Analógico (PWM 0-1024)
            AnalogPinControlCard(
                pinName = "Pino P12",
                value = pin12AnalogValue,
                inputText = pin12AnalogInput,
                isEnabled = status == BleConnectionStatus.READY,
                onInputChange = onPin12InputChange,
                onApply = { onApplyPin12Analog(null) },
                onSliderChange = { newValue ->
                    val intVal = newValue.toInt()
                    onPin12InputChange(intVal.toString())
                    onApplyPin12Analog(intVal)
                }
            )

            // Pino 13: Controle Analógico (PWM 0-1024)
            AnalogPinControlCard(
                pinName = "Pino P13",
                value = pin13AnalogValue,
                inputText = pin13AnalogInput,
                isEnabled = status == BleConnectionStatus.READY,
                onInputChange = onPin13InputChange,
                onApply = { onApplyPin13Analog(null) },
                onSliderChange = { newValue ->
                    val intVal = newValue.toInt()
                    onPin13InputChange(intVal.toString())
                    onApplyPin13Analog(intVal)
                }
            )

            Text(
                text = "Controle de Saídas Digitais (GPIO)",
                style = MaterialTheme.typography.titleLarge
            )

            // Pino 14: Habilita o carro ou puxa o freio de mão
            val isP14High = pinStates[14] ?: false
            PinControlCard(
                pinName = "Pino P14",
                isHigh = isP14High,
                isEnabled = status == BleConnectionStatus.READY,
                onToggle = { onTogglePin(14) }
            )

            // Pino 15: Sentido Ré ou Frente
            val isP15High = pinStates[15] ?: false
            PinControlCard(
                pinName = "Pino P15",
                isHigh = isP15High,
                isEnabled = status == BleConnectionStatus.READY,
                onToggle = { onTogglePin(15) }
            )
        }
    }
}

@Composable
fun PinControlCard(
    pinName: String,
    isHigh: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Indicador visual de LED
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isHigh) LedActive else LedInactive)
                )

                Column {
                    Text(text = pinName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (isHigh) "Nível Alto (3.3V)" else "Nível Baixo (0V)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Switch(
                checked = isHigh,
                onCheckedChange = onToggle,
                enabled = isEnabled
            )
        }
    }
}

@Composable
fun AnalogPinControlCard(
    pinName: String,
    value: Int,
    inputText: String,
    isEnabled: Boolean,
    onInputChange: (String) -> Unit,
    onApply: () -> Unit,
    onSliderChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (value > 0) LedActive else LedInactive)
                    )

                    Column {
                        Text(text = pinName, style = MaterialTheme.typography.titleMedium)
                        val dutyPercent = (value * 100f / 1024f).toInt()
                        Text(
                            text = "PWM: $value / 1024 ($dutyPercent%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = "$value",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Slider para ajuste fino/rápido
            Slider(
                value = value.coerceIn(0, 1024).toFloat(),
                onValueChange = onSliderChange,
                valueRange = 0f..1024f,
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { novoTexto ->
                        if (novoTexto.isEmpty() || novoTexto.all { it.isDigit() }) {
                            onInputChange(novoTexto)
                        }
                    },
                    label = { Text("Valor (0 a 1024)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = isEnabled,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = onApply,
                    enabled = isEnabled,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Gravar")
                }
            }
        }
    }
}
