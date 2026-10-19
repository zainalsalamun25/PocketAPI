package com.example.pocketapi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.data.local.SavedRequestEntity
import com.example.pocketapi.data.model.HttpMethod
import com.example.pocketapi.ui.components.ImportPostmanDialog
import com.example.pocketapi.ui.components.MethodBadge
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated
import com.example.pocketapi.ui.viewmodel.PocketApiViewModel
import com.example.pocketapi.util.JsonFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsBottomSheet(
    savedRequests: List<SavedRequestEntity>,
    folders: List<String>,
    viewModel: PocketApiViewModel,
    onSelectSaved: (SavedRequestEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var requestToDelete by remember { mutableStateOf<SavedRequestEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    var showImportPostmanDialog by remember { mutableStateOf(false) }

    val filteredList = remember(savedRequests, searchQuery, selectedFolder) {
        savedRequests.filter { request ->
            val matchesFolder = selectedFolder == null || request.folderName.equals(selectedFolder, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                    request.name.contains(searchQuery, ignoreCase = true) ||
                    request.url.contains(searchQuery, ignoreCase = true) ||
                    request.folderName.contains(searchQuery, ignoreCase = true)
            matchesFolder && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Gray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Koleksi Request (${savedRequests.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (selectedFolder != null) {
                    TextButton(
                        onClick = { folderToDelete = selectedFolder },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hapus Folder", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Postman Import/Export Action Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { showImportPostmanDialog = true },
                    label = { Text("Import Postman JSON", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )

                if (savedRequests.isNotEmpty()) {
                    AssistChip(
                        onClick = {
                            val json = viewModel.exportPostmanCollection()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Postman Collection v2.1", json)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Postman Collection v2.1 disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text("Export Postman JSON", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            if (savedRequests.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari request atau URL...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
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

                Spacer(modifier = Modifier.height(10.dp))

                // Folder Filter Chips
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFolder == null,
                        onClick = { selectedFolder = null },
                        label = { Text("Semua (${savedRequests.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    folders.forEach { folder ->
                        val count = savedRequests.count { it.folderName.equals(folder, ignoreCase = true) }
                        FilterChip(
                            selected = selectedFolder == folder,
                            onClick = { selectedFolder = if (selectedFolder == folder) null else folder },
                            label = { Text("$folder ($count)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content List or Empty State
            if (savedRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Belum ada request yang disimpan ke koleksi.",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Gunakan tombol 'Import Postman JSON' di atas atau bookmark request.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            } else if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada request yang sesuai dengan pencarian.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        SavedRequestItemCard(
                            item = item,
                            onClick = {
                                onSelectSaved(item)
                                onDismiss()
                            },
                            onDelete = { requestToDelete = item }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showImportPostmanDialog) {
        ImportPostmanDialog(
            onDismiss = { showImportPostmanDialog = false },
            onImport = { jsonString ->
                val result = viewModel.importPostmanCollection(jsonString)
                result.fold(
                    onSuccess = { count ->
                        Toast.makeText(context, "Berhasil mengimpor $count request!", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {}
                )
                result
            }
        )
    }

    // Delete Request Confirmation Dialog
    requestToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { requestToDelete = null },
            title = { Text("Hapus Request?") },
            text = { Text("Yakin ingin menghapus request '${target.name}' dari koleksi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSavedRequest(target.id)
                        requestToDelete = null
                    }
                ) {
                    Text("Hapus", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { requestToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Delete Entire Folder Confirmation Dialog
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("Hapus Folder '$folder'?") },
            text = { Text("Semua request di dalam folder ini akan dihapus permanen dari koleksi.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSavedFolder(folder)
                        folderToDelete = null
                        selectedFolder = null
                    }
                ) {
                    Text("Hapus Folder", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun SavedRequestItemCard(
    item: SavedRequestEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = DarkSurfaceElevated,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: Method Badge + Name + Folder Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MethodBadge(method = HttpMethod.fromString(item.method))
                    Text(
                        text = item.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Folder tag
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = item.folderName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // URL
            Text(
                text = item.url,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Bottom bar: Timestamp & Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = JsonFormatter.formatTimestamp(item.createdAt),
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Muat Request",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
