package com.example.photo_organizer.bluetooth

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class BluetoothManager(private val context: Context, private val scope: CoroutineScope) {

    companion object {
        val APP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP
    }

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val discovered = mutableSetOf<String>()
    val devicesListState = MutableLiveData<List<BluetoothDevice>>(emptyList())

    private var bluetoothGatt: BluetoothGatt? = null
    private var socket: BluetoothSocket? = null
    var onConnectionStateChanged: ((String) -> Unit)? = null
    var onTransferProgress: ((Int) -> Unit)? = null
    var onTransferCompleted: ((Boolean, String) -> Unit)? = null

    private var scanner: BluetoothLeScanner? = null
    private var isScanning = false

    fun startScan() {
        val adapter = adapter ?: return
        discovered.clear()

        // Classic discovery
        if (adapter.isDiscovering) adapter.cancelDiscovery()
        adapter.startDiscovery()

        // update live list from bonded devices + discoveries
        val bonded = adapter.bondedDevices?.toList() ?: emptyList()
        devicesListState.postValue(bonded)

        // Optional BLE scanning (for dual mode devices)
        try {
            scanner = adapter.bluetoothLeScanner
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()
            scanner?.startScan(null, settings, scanCallback)
            isScanning = true
        } catch (e: Exception) { /* ignore */ }
    }

    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {}
        isScanning = false
        adapter?.cancelDiscovery()
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            handleFoundDevice(device)
        }
    }

    private fun handleFoundDevice(device: BluetoothDevice) {
        if (discovered.add(device.address)) {
            val current = ArrayList(devicesListState.value ?: emptyList())
            current.add(device)
            devicesListState.postValue(current)
        }
    }

    fun connectToDevice(address: String) {
        val device = adapter?.getRemoteDevice(address) ?: run {
            onConnectionStateChanged?.invoke("Device not found")
            return
        }

        // Ensure bonded (pair) - requestBond will prompt pairing UI if needed
        if (device.bondState == BluetoothDevice.BOND_NONE) {
            device.createBond()
        }

        // try to create RFCOMM socket and connect
        try {
            socket?.close()
        } catch (e: Exception) {}

        scope.launch {
            try {
                val sock = device.createRfcommSocketToServiceRecord(APP_UUID)
                adapter?.cancelDiscovery()
                sock.connect()
                socket = sock
                onConnectionStateChanged?.invoke("CONNECTED")
            } catch (e: Exception) {
                onConnectionStateChanged?.invoke("CONNECT_FAILED: ${e.message}")
            }
        }
    }

    fun getConnectedSocket(): BluetoothSocket? = socket

    fun cleanup() {
        try { socket?.close() } catch (e: Exception) {}
        try { bluetoothGatt?.close() } catch (e: Exception) {}
    }
}
