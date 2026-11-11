package com.example.microbitcontroller.ui.screens

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.microbitcontroller.ble.BleDeviceModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    isScanning: Boolean,
    scannedDevices: List<BleDeviceModel>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnectDevice: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar micro:bit") },
                actions = {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 12.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        IconButton(onClick = onStopScan) {
                            Icon(Icons.Default.Refresh, contentDescription = "Parar Busca")
                        }
                    } else {
                        IconButton(onClick = onStartScan) {
                            Icon(Icons.Default.Refresh, contentDescription = "Buscar Dispositivos")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { if (isScanning) onStopScan() else onStartScan() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Bluetooth, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanning) "Parar Busca" else "Buscar Dispositivos micro:bit")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (scannedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isScanning) "Buscando placas próximas..." else "Nenhum micro:bit encontrado.\nClique em Buscar para iniciar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(scannedDevices) { item ->
                        DeviceCard(
                            deviceModel = item,
                            onConnect = { onConnectDevice(item.device) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    deviceModel: BleDeviceModel,
    onConnect: () -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = deviceModel.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = deviceModel.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Sinal: ${deviceModel.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = onConnect) {
                Text("Conectar")
            }
        }
    }
}
