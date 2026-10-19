package com.example.pocketapi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated

@Composable
fun ImportPostmanDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Result<Int>
) {
    var rawJson by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Import Postman Collection v2.1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tempelkan (Paste) konten file JSON Postman Collection v2.1 di bawah ini:",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                OutlinedTextField(
                    value = rawJson,
                    onValueChange = {
                        rawJson = it
                        errorMessage = null
                    },
                    placeholder = { Text("{\n  \"info\": { \"name\": \"My Collection\" },\n  \"item\": [...] \n}", fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceElevated,
                        unfocusedContainerColor = DarkSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF5350),
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawJson.isBlank()) {
                        errorMessage = "Konten JSON Postman tidak boleh kosong."
                        return@Button
                    }
                    val result = onImport(rawJson)
                    result.fold(
                        onSuccess = { onDismiss() },
                        onFailure = { err -> errorMessage = err.localizedMessage ?: "Gagal membaca Postman Collection JSON." }
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Import Collection")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        },
        containerColor = DarkSurface
    )
}
