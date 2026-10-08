package com.example.pocketapi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.ui.platform.LocalContext
import com.example.pocketapi.data.model.HttpMethod
import com.example.pocketapi.ui.components.ImportCurlDialog
import com.example.pocketapi.ui.components.KeyValueEditor
import com.example.pocketapi.ui.components.MethodBadge
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated
import com.example.pocketapi.ui.viewmodel.PocketApiUiState
import com.example.pocketapi.ui.viewmodel.PocketApiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestSection(
    uiState: PocketApiUiState,
    viewModel: PocketApiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var methodMenuExpanded by remember { mutableStateOf(false) }
    var showImportCurlDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Method dropdown & URL Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Method Selector
            Box {
                FilterChip(
                    selected = true,
                    onClick = { methodMenuExpanded = true },
                    label = { MethodBadge(method = uiState.method) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        selectedContainerColor = DarkSurfaceElevated
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = DarkBorder,
                        enabled = true,
                        selected = true
                    )
                )

                DropdownMenu(
                    expanded = methodMenuExpanded,
                    onDismissRequest = { methodMenuExpanded = false }
                ) {
                    HttpMethod.entries.forEach { method ->
                        DropdownMenuItem(
                            text = { MethodBadge(method = method) },
                            onClick = {
                                viewModel.setMethod(method)
                                methodMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // URL input field
            OutlinedTextField(
                value = uiState.url,
                onValueChange = { viewModel.setUrl(it) },
                placeholder = { Text("https://api.example.com", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    if (uiState.url.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setUrl("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus URL", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DarkBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Send Button
            Button(
                onClick = { viewModel.sendRequest() },
                enabled = !uiState.isLoading && uiState.url.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send", fontSize = 13.sp)
                }
            }
        }

        // Row 2: cURL Tools & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { showImportCurlDialog = true },
                label = { Text("Import cURL", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )

            AssistChip(
                onClick = {
                    val curl = viewModel.generateCurlCommand()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("PocketAPI cURL", curl)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Perintah cURL disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                },
                label = { Text("Copy as cURL", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )
        }

        // Quick Presets Row
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Preset:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterVertically))
            SuggestionChip(
                onClick = { viewModel.loadPreset("JSONPlaceholder GET") },
                label = { Text("GET Post #1", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = { viewModel.loadPreset("JSONPlaceholder POST") },
                label = { Text("POST New Post", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = { viewModel.loadPreset("HttpBin IP Check") },
                label = { Text("IP Check", fontSize = 11.sp) }
            )
            SuggestionChip(
                onClick = { viewModel.loadPreset("HttpBin Headers") },
                label = { Text("Custom Headers", fontSize = 11.sp) }
            )
        }

        // Tabs: Headers vs Body
        val tabs = listOf("Headers (${uiState.headers.count { it.isEnabled }})", "Body (JSON)")
        TabRow(
            selectedTabIndex = uiState.selectedRequestTab,
            containerColor = DarkSurfaceElevated,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedRequestTab == index,
                    onClick = { viewModel.setRequestTab(index) },
                    text = { Text(title, fontSize = 13.sp) }
                )
            }
        }

        when (uiState.selectedRequestTab) {
            0 -> {
                // Headers Editor
                KeyValueEditor(
                    items = uiState.headers,
                    onItemChange = { idx, item -> viewModel.updateHeader(idx, item) },
                    onItemRemove = { idx -> viewModel.removeHeader(idx) },
                    onItemAdd = { viewModel.addHeader() }
                )
            }
            1 -> {
                // Request Body Editor
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Content-Type: application/json",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        TextButton(
                            onClick = { viewModel.beautifyRequestBody() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Format JSON", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = uiState.requestBody,
                        onValueChange = { viewModel.setRequestBody(it) },
                        placeholder = { Text("{\n  \"key\": \"value\"\n}", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 200.dp),
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = DarkBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }

    if (showImportCurlDialog) {
        ImportCurlDialog(
            onDismiss = { showImportCurlDialog = false },
            onImport = { curlString ->
                val result = viewModel.importCurl(curlString)
                if (result.isSuccess) {
                    Toast.makeText(context, "cURL berhasil diimpor!", Toast.LENGTH_SHORT).show()
                }
                result
            }
        )
    }
}