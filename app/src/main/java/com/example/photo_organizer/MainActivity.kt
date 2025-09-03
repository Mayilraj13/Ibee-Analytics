package com.example.photo_organizer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import com.example.photo_organizer.viewmodel.MainViewModel
import com.example.photo_organizer.ui.MainScreen // Or the correct package for MainScreen


class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.onFileSelected(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.pickFileLauncher = { mime ->
            pickFile.launch(mime ?: "*/*")
        }

        setContent {
            MaterialTheme {
                Surface {
                    MainScreen(viewModel = vm) // This should now resolve
                }
            }
        }
    }
}