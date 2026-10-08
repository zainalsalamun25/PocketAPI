package com.example.pocketapi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.ui.components.SaveRequestDialog
import com.example.pocketapi.ui.theme.DarkBackground
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.viewmodel.PocketApiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: PocketApiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val savedRequestsList by viewModel.savedRequestsList.collectAsState()
    val foldersList by viewModel.foldersList.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var showCollectionsSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PocketAPI",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Mobile REST API Client",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(
                            Icons.Default.BookmarkAdd,
                            contentDescription = "Simpan ke Koleksi",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    BadgedBox(
                        badge = {
                            if (savedRequestsList.isNotEmpty()) {
                                Badge { Text(savedRequestsList.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showCollectionsSheet = true }) {
                            Icon(Icons.Default.Folder, contentDescription = "Koleksi Request", tint = Color.LightGray)
                        }
                    }
                    BadgedBox(
                        badge = {
                            if (historyList.isNotEmpty()) {
                                Badge { Text(historyList.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { showHistorySheet = true }) {
                            Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color.LightGray)
                        }
                    }
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Tentang Aplikasi", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RequestSection(
                uiState = uiState,
                viewModel = viewModel
            )

            ResponseSection(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        if (showHistorySheet) {
            HistoryBottomSheet(
                historyList = historyList,
                viewModel = viewModel,
                onSelectHistory = { entity ->
                    viewModel.loadFromHistory(entity)
                },
                onDismiss = { showHistorySheet = false }
            )
        }

        if (showCollectionsSheet) {
            CollectionsBottomSheet(
                savedRequests = savedRequestsList,
                folders = foldersList,
                viewModel = viewModel,
                onSelectSaved = { entity ->
                    viewModel.loadSavedRequest(entity)
                },
                onDismiss = { showCollectionsSheet = false }
            )
        }

        if (showSaveDialog) {
            SaveRequestDialog(
                initialName = "",
                existingFolders = foldersList,
                onDismiss = { showSaveDialog = false },
                onSave = { name, folder ->
                    viewModel.saveCurrentRequest(name, folder)
                    showSaveDialog = false
                }
            )
        }

        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = { Text("Tentang PocketAPI") },
                text = {
                    Text(
                        "PocketAPI adalah HTTP Client modern untuk Android.\n\n" +
                        "• Dibangun dengan Jetpack Compose & Material 3\n" +
                        "• Dynamic HTTP Engine didukung OkHttp 4\n" +
                        "• Penyimpanan riwayat lokal otomatis via Room DB\n" +
                        "• Format JSON Pretty-Print otomatis\n\n" +
                        "Dibuat untuk portofolio pengembangan Android Modern dengan Kotlin."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showInfoDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}