package com.example.pocketapi.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurfaceElevated

@Composable
fun SaveRequestDialog(
    initialName: String = "",
    existingFolders: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, folder: String) -> Unit
) {
    var requestName by remember { mutableStateOf(initialName.ifBlank { "Untitled Request" }) }
    var folderName by remember { mutableStateOf(if (existingFolders.isNotEmpty()) existingFolders.first() else "General") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Simpan ke Koleksi", style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = requestName,
                    onValueChange = { requestName = it },
                    label = { Text("Nama Request", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Get User Profile", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Folder Field
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Nama Folder / Koleksi", fontSize = 12.sp) },
                    placeholder = { Text("e.g. JSONPlaceholder API", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                // Quick folder suggestions if any
                if (existingFolders.isNotEmpty()) {
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        existingFolders.forEach { folder ->
                            SuggestionChip(
                                onClick = { folderName = folder },
                                label = { Text(folder, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(requestName.trim(), folderName.trim())
                    onDismiss()
                },
                enabled = requestName.isNotBlank()
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}