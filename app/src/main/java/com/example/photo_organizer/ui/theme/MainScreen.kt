package com.example.photo_organizer.ui

import android.Manifest
import com.example.photo_organizer.ui.theme.DeviceRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.photo_organizer.viewmodel.MainViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.accompanist.permissions.*
import androidx.compose.material3.Text // For Material Design 3
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Icon // Material 3 Icon
import androidx.compose.material3.MaterialTheme // Correct M3 import
import androidx.compose.material3.IconButton // Import Material 3 IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    // Assuming viewModel.bluetoothManager.devicesListState is already a State object
    val devices by viewModel.bluetoothManager.devicesListState.observeAsState(emptyList())
    var showingPermissions by remember { mutableStateOf(false) }

    val bluetoothConnectPermission =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_CONNECT else Manifest.permission.BLUETOOTH
    val bluetoothScanPermission =
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) Manifest.permission.BLUETOOTH_SCAN else Manifest.permission.ACCESS_FINE_LOCATION

    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            bluetoothConnectPermission,
            bluetoothScanPermission,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    val deviceList = viewModel.devices.map { it } // Pair(name, addr)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Bluetooth File Transfer",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.startScan() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Scan")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Discovered devices", style = MaterialTheme.typography.titleMedium)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(deviceList.size) { idx ->
                val (name, addr) = deviceList[idx]
                DeviceRow(
                    name = name,
                    address = addr,
                    onConnect = { viewModel.pairAndConnect(addr) })
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        @OptIn(ExperimentalPermissionsApi::class)
        @Composable
        fun MainScreen(viewModel: MainViewModel) {
            // ...
            Row {
                Button(
                    onClick = { viewModel.requestPickFile() },
                    modifier = Modifier.weight(1f)
                ) { // This will now use Material3 Button
                    Icon(Icons.Default.FileUpload, contentDescription = "Pick")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select File")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.sendFile() },
                    enabled = viewModel.fileUri != null && viewModel.selectedDeviceAddr != null,
                    modifier = Modifier.weight(1f)
                ) { // This will now use Material3 Button
                    Text("Send")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.passphrase,
                onValueChange = { newPassphrase ->
                    viewModel.passphrase = newPassphrase
                }, // Changed here
                label = { Text("Passphrase (both devices must use same)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )


            if (viewModel.isTransferring) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = viewModel.transferProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${viewModel.transferProgress}%")
            }

            viewModel.transferStatus?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it)
            }
        }
    }
}
