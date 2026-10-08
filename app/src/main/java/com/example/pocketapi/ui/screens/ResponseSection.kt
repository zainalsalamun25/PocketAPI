package com.example.pocketapi.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import com.example.pocketapi.ui.components.HttpStatusDetailBottomSheet
import com.example.pocketapi.ui.components.JsonCodeViewer
import com.example.pocketapi.ui.components.JwtInspectorDialog
import com.example.pocketapi.ui.components.StatusCodeBadge
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated
import com.example.pocketapi.ui.theme.StatusError
import com.example.pocketapi.ui.viewmodel.PocketApiUiState
import com.example.pocketapi.ui.viewmodel.PocketApiViewModel
import com.example.pocketapi.util.JsonFormatter
import com.example.pocketapi.util.JwtDecoder

@Composable
fun ResponseSection(
    uiState: PocketApiUiState,
    viewModel: PocketApiViewModel,
    modifier: Modifier = Modifier
) {
    val response = uiState.response
    var showJwtDialog by remember { mutableStateOf(false) }
    var showStatusDetailSheet by remember { mutableStateOf(false) }
    val detectedJwt = remember(response?.body) { response?.body?.let { JwtDecoder.findJwtInText(it) } }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Response",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (response != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusCodeBadge(
                        statusCode = response.statusCode,
                        statusMessage = response.statusMessage,
                        onClick = { showStatusDetailSheet = true }
                    )
                    Text(
                        text = "${response.responseTimeMs} ms",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                    Text(
                        text = "•",
                        color = Color.Gray
                    )
                    Text(
                        text = JsonFormatter.formatBytes(response.contentLengthBytes),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Text("Mengirim request ke server...", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else if (response == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Klik 'Send' untuk melihat hasil respon API.",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        } else {
            // Error banner if any
            if (response.errorMessage != null) {
                Surface(
                    color = StatusError.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = response.errorMessage,
                        color = StatusError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // Detected JWT Banner if response contains a JWT
            if (detectedJwt != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Token JWT terdeteksi dalam respon",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        FilledTonalButton(
                            onClick = { showJwtDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Inspect JWT", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Tabs for Response: Body / Headers / Raw
            val respTabs = listOf("Formatted Body", "Headers (${response.headers.size})", "Raw")
            TabRow(
                selectedTabIndex = uiState.selectedResponseTab,
                containerColor = DarkSurfaceElevated,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                respTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedResponseTab == index,
                        onClick = { viewModel.setResponseTab(index) },
                        text = { Text(title, fontSize = 12.sp) }
                    )
                }
            }

            when (uiState.selectedResponseTab) {
                0 -> {
                    // Formatted pretty print
                    val prettyJson = JsonFormatter.prettyPrint(response.body)
                    JsonCodeViewer(
                        code = prettyJson,
                        modifier = Modifier.heightIn(min = 180.dp, max = 320.dp)
                    )
                }
                1 -> {
                    // Headers table
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 320.dp)
                            .background(DarkSurfaceElevated, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (response.headers.isEmpty()) {
                            Text("Tidak ada response headers.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            response.headers.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = value,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color.LightGray,
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
                2 -> {
                    // Raw string
                    JsonCodeViewer(
                        code = response.body,
                        modifier = Modifier.heightIn(min = 180.dp, max = 320.dp)
                    )
                }
            }
        }
    }

    if (showJwtDialog && detectedJwt != null) {
        JwtInspectorDialog(
            jwtToken = detectedJwt,
            onDismiss = { showJwtDialog = false }
        )
    }

    if (showStatusDetailSheet && response != null) {
        HttpStatusDetailBottomSheet(
            statusCode = response.statusCode,
            statusMessage = response.statusMessage,
            onDismiss = { showStatusDetailSheet = false }
        )
    }
}