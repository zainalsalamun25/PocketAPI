package com.example.pocketapi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.pocketapi.ui.components.AuthSection
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
                placeholder = { Text("https://api.example.com/posts/{{postId}}", fontSize = 13.sp) },
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

            FilterChip(
                selected = uiState.bypassSsl,
                onClick = { viewModel.setBypassSsl(!uiState.bypassSsl) },
                label = { Text(if (uiState.bypassSsl) "SSL: Unsafe" else "SSL: Strict", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFFB74D).copy(alpha = 0.2f),
                    selectedLabelColor = Color(0xFFFFB74D)
                )
            )
        }

        // One-Click Automatic CRUD Actions (JSONPlaceholder)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Otomatis Jalankan (1-Klik):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val crudScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(crudScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. READ (GET)
                FilledTonalButton(
                    onClick = { viewModel.runCrudAction("READ") },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HttpMethod.GET.color.copy(alpha = 0.2f),
                        contentColor = HttpMethod.GET.color
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GET Baca #1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 2. CREATE (POST)
                FilledTonalButton(
                    onClick = { viewModel.runCrudAction("CREATE") },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HttpMethod.POST.color.copy(alpha = 0.2f),
                        contentColor = HttpMethod.POST.color
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("POST Create", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 3. UPDATE (PUT)
                FilledTonalButton(
                    onClick = { viewModel.runCrudAction("UPDATE") },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HttpMethod.PUT.color.copy(alpha = 0.2f),
                        contentColor = HttpMethod.PUT.color
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PUT Update #1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 4. DELETE (DELETE)
                FilledTonalButton(
                    onClick = { viewModel.runCrudAction("DELETE") },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = HttpMethod.DELETE.color.copy(alpha = 0.2f),
                        contentColor = HttpMethod.DELETE.color
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DELETE Hapus #1", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // 5. LIST USERS (GET)
                FilledTonalButton(
                    onClick = { viewModel.runCrudAction("USERS") },
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF00B0FF).copy(alpha = 0.2f),
                        contentColor = Color(0xFF00B0FF)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GET List Users", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs: Params vs Headers vs Body vs Auth
        val activeParamsCount = uiState.queryParams.count { it.isEnabled }
        val activeHeadersCount = uiState.headers.count { it.isEnabled }
        val authLabel = if (uiState.authConfig.type.name != "NONE") "Auth (${uiState.authConfig.type.label})" else "Auth"
        val tabs = listOf(
            if (activeParamsCount > 0) "Params ($activeParamsCount)" else "Params",
            if (activeHeadersCount > 0) "Headers ($activeHeadersCount)" else "Headers",
            "Body",
            authLabel
        )

        ScrollableTabRow(
            selectedTabIndex = uiState.selectedRequestTab,
            containerColor = DarkSurfaceElevated,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 0.dp,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = uiState.selectedRequestTab == index,
                    onClick = { viewModel.setRequestTab(index) },
                    text = { Text(title, fontSize = 12.sp) }
                )
            }
        }

        when (uiState.selectedRequestTab) {
            0 -> {
                // Query Params Editor
                KeyValueEditor(
                    items = uiState.queryParams,
                    onItemChange = { idx, item -> viewModel.updateQueryParam(idx, item) },
                    onItemRemove = { idx -> viewModel.removeQueryParam(idx) },
                    onItemAdd = { viewModel.addQueryParam() }
                )
            }
            1 -> {
                // Headers Editor
                KeyValueEditor(
                    items = uiState.headers,
                    onItemChange = { idx, item -> viewModel.updateHeader(idx, item) },
                    onItemRemove = { idx -> viewModel.removeHeader(idx) },
                    onItemAdd = { viewModel.addHeader() }
                )
            }
            2 -> {
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
            3 -> {
                // Auth Editor
                AuthSection(
                    authConfig = uiState.authConfig,
                    onAuthConfigChange = { viewModel.setAuthConfig(it) }
                )
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
