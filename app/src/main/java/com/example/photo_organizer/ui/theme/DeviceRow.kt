package com.example.photo_organizer.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme // For Material 2
@Composable
fun DeviceRow(name: String, address: String, onConnect: () -> Unit) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(name)
            Text(address, style = MaterialTheme.typography.bodySmall)        }
        Button(onClick = onConnect) { Text("Pair/Connect") }
    }
}
