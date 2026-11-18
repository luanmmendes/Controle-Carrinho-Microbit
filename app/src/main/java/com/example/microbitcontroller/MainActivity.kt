package com.example.microbitcontroller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.microbitcontroller.ble.BleConnectionStatus
import com.example.microbitcontroller.ui.MicrobitViewModel
import com.example.microbitcontroller.ui.screens.PinControlView
import com.example.microbitcontroller.ui.screens.ScanScreen
import com.example.microbitcontroller.ui.theme.MicrobitTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MicrobitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MicrobitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Permissões necessárias conforme a versão do Android
                    val requiredPermissions = remember {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            arrayOf(
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT
                            )
                        } else {
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN
                            )
                        }
                    }

                    var hasPermissions by remember {
                        mutableStateOf(
                            requiredPermissions.all {
                                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                            }
                        )
                    }

                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissionsMap ->
                        val allGranted = permissionsMap.values.all { it }
                        hasPermissions = allGranted
                        if (allGranted) {
                            viewModel.startScanning()
                        } else {
                            Toast.makeText(
                                context,
                                "Permissões de Bluetooth são necessárias para buscar e conectar ao micro:bit.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    // Exibir mensagens de erro se houver
                    LaunchedEffect(uiState.errorMessage) {
                        uiState.errorMessage?.let {
                            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Navegação baseada no estado da conexão
                    val isConnectedOrConnecting = uiState.connectionStatus != BleConnectionStatus.DISCONNECTED &&
                            uiState.connectionStatus != BleConnectionStatus.ERROR
                    if (isConnectedOrConnecting) {
                        PinControlView(
                            onPinAction = { pin, isHigh -> viewModel.setPin(pin, isHigh) },
                            FBController = {pin,isHigh -> viewModel.setPin(pin,isHigh)},
                            setVelocidade = { pin, value -> viewModel.setPinAnalogValue(pin, value) },
                            setCurvatura ={pin,value -> viewModel.setPinAnalogValue(13,value)}
                            )
                    } else {
                        ScanScreen(
                            isScanning = uiState.isScanning,
                            scannedDevices = uiState.scannedDevices,
                            onStartScan = {
                                if (hasPermissions) {
                                    viewModel.startScanning()
                                } else {
                                    permissionLauncher.launch(requiredPermissions)
                                }
                            },
                            onStopScan = {
                                viewModel.stopScanning()
                            },
                            onConnectDevice = { device ->
                                viewModel.connectToDevice(device)
                            }
                        )
                    }
                }
            }
        }
    }
}
