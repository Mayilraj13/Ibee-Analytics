package com.example.photo_organizer.bluetooth

import android.content.Context
import android.net.Uri
import com.example.photo_organizer.crypto.FileCrypto
import com.example.photo_organizer.utils.FileUtils
import kotlinx.coroutines.delay
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*

object TransferService {

    // Send a file on an already connected device (address is used only for clarity)
    suspend fun sendFile(context: Context, manager: BluetoothManager, addr: String, uri: Uri, passphrase: String) {
        val sock = manager.getConnectedSocket()
        if (sock == null) {
            manager.onTransferCompleted?.invoke(false, "Not connected")
            return
        }

        try {
            // read bytes
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: run {
                manager.onTransferCompleted?.invoke(false, "Cannot open file")
                return
            }
            val fileBytes = inputStream.readBytes()
            inputStream.close()

            // encrypt
            val encrypted = FileCrypto.encrypt(fileBytes, passphrase)

            val out: OutputStream = BufferedOutputStream(sock.outputStream)
            val header = "BTFILE;${FileUtils.getFileName(context, uri)};${encrypted.size}\n"
            out.write(header.toByteArray(Charsets.UTF_8))
            out.flush()

            // send in chunks and report progress
            val chunk = 8 * 1024
            var sent = 0
            while (sent < encrypted.size) {
                val toSend = minOf(chunk, encrypted.size - sent)
                out.write(encrypted, sent, toSend)
                out.flush()
                sent += toSend
                val progress = (sent * 100L / encrypted.size).toInt()
                manager.onTransferProgress?.invoke(progress)
                // small delay letting UI update
                delay(10)
            }

            manager.onTransferCompleted?.invoke(true, "File sent")
        } catch (e: Exception) {
            manager.onTransferCompleted?.invoke(false, "Send failed: ${e.message}")
        }
    }

    // Call this on the receiving device in a coroutine to accept incoming file data.
    // It blocks reading the socket input. For a production app you'd run this in a Service.
    fun startReceiving(manager: BluetoothManager, outputFolderProvider: (String, ByteArray) -> Unit, passphraseProvider: () -> String) {
        val socket = manager.getConnectedSocket() ?: run {
            manager.onTransferCompleted?.invoke(false, "Not connected")
            return
        }
        try {
            val input = BufferedInputStream(socket.inputStream)
            // read header line (ends with \n)
            val headerBuilder = StringBuilder()
            while (true) {
                val ch = input.read()
                if (ch == -1) throw Exception("Stream closed")
                if (ch.toChar() == '\n') break
                headerBuilder.append(ch.toChar())
            }
            val header = headerBuilder.toString()
            // format: BTFILE;filename;size
            val parts = header.split(";")
            if (parts.size < 3 || parts[0] != "BTFILE") throw Exception("Invalid header")
            val filename = parts[1]
            val size = parts[2].toInt()

            val buffer = ByteArray(size)
            var readTotal = 0
            while (readTotal < size) {
                val r = input.read(buffer, readTotal, size - readTotal)
                if (r == -1) throw Exception("Stream closed while reading")
                readTotal += r
                val progress = (readTotal * 100L / size).toInt()
                manager.onTransferProgress?.invoke(progress)
            }

            // decrypt
            val pass = passphraseProvider()
            val decrypted = FileCrypto.decrypt(buffer, pass)
            // call provider to save file
            outputFolderProvider(filename, decrypted)
            manager.onTransferCompleted?.invoke(true, "Received: $filename")
        } catch (e: Exception) {
            manager.onTransferCompleted?.invoke(false, "Receive failed: ${e.message}")
        }
    }
}
