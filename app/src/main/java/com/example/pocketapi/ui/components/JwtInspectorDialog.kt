package com.example.pocketapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated
import com.example.pocketapi.ui.theme.StatusError
import com.example.pocketapi.ui.theme.StatusSuccess
import com.example.pocketapi.util.JwtDecoder

@Composable
fun JwtInspectorDialog(
    jwtToken: String,
    onDismiss: () -> Unit
) {
    val decodeResult = remember(jwtToken) { JwtDecoder.decode(jwtToken) }
    var selectedTab by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "JWT Token Inspector",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (decodeResult.isFailure) {
                    val errorMsg = decodeResult.exceptionOrNull()?.localizedMessage ?: "Format JWT tidak valid."
                    Surface(
                        color = StatusError.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMsg,
                            color = StatusError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    val decoded = decodeResult.getOrThrow()

                    // Expiration Banner
                    val statusColor = if (decoded.isExpired) StatusError else StatusSuccess
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = decoded.expiryDescription,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Metadata chips (Algorithm, Subject)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (decoded.algorithm != null) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Alg: ${decoded.algorithm}", fontSize = 11.sp) }
                            )
                        }
                        if (decoded.subject != null) {
                            SuggestionChip(
                                onClick = {},
                                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp)) },
                                label = { Text("User: ${decoded.subject}", fontSize = 11.sp) }
                            )
                        }
                    }

                    // Tabs: Payload vs Header
                    val tabs = listOf("Payload (Claims)", "Header (Algoritma)")
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurfaceElevated,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title, fontSize = 12.sp) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> {
                            JsonCodeViewer(
                                code = decoded.payloadJson,
                                modifier = Modifier.heightIn(min = 160.dp, max = 280.dp)
                            )
                        }
                        1 -> {
                            JsonCodeViewer(
                                code = decoded.headerJson,
                                modifier = Modifier.heightIn(min = 160.dp, max = 280.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}