package com.example.photo_organizer.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.photo_organizer.bluetooth.BluetoothManager
import com.example.photo_organizer.bluetooth.TransferService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.app.Application

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext

    val bluetoothManager = BluetoothManager(appContext, viewModelScope)

    // UI states
    var devices by mutableStateOf<List<Pair<String, String>>>(emptyList()) // (name, addr)
        private set

    var selectedDeviceAddr by mutableStateOf<String?>(null)
    var transferProgress by mutableStateOf(0)
    var transferStatus by mutableStateOf<String?>(null)
    var isTransferring by mutableStateOf(false)
    var passphrase by mutableStateOf("")

    // file uri to send
    var fileUri by mutableStateOf<Uri?>(null)

    // callback to launch file picker, set from Activity
    var pickFileLauncher: ((String?) -> Unit)? = null

    init {
        bluetoothManager.devicesListState.observeForever { list ->
            // list of BluetoothDevice (converted to pairs)
            devices = list.map { Pair(it.name ?: "Unknown", it.address) }
        }

        bluetoothManager.onConnectionStateChanged = { state ->
            // handle UI changes if needed
        }

        bluetoothManager.onTransferProgress = { progress ->
            transferProgress = progress
        }

        bluetoothManager.onTransferCompleted = { ok, message ->
            isTransferring = false
            transferStatus = message
        }
    }

    fun startScan() {
        bluetoothManager.startScan()
    }

    fun stopScan() {
        bluetoothManager.stopScan()
    }

    fun pairAndConnect(address: String) {
        selectedDeviceAddr = address
        bluetoothManager.connectToDevice(address)
    }

    fun requestPickFile(mime: String? = "*/*") {
        pickFileLauncher?.invoke(mime)
    }

    fun onFileSelected(uri: Uri) {
        fileUri = uri
    }

    fun sendFile() {
        val addr = selectedDeviceAddr ?: return
        val uri = fileUri ?: return
        if (passphrase.isEmpty()) {
            transferStatus = "Enter passphrase for encryption"
            return
        }
        isTransferring = true
        transferStatus = null
        viewModelScope.launch(Dispatchers.IO) {
            TransferService.sendFile(appContext, bluetoothManager, addr, uri, passphrase)
        }
    }

    override fun onCleared() {
        bluetoothManager.cleanup()
        super.onCleared()
    }
}
