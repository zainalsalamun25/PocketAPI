package com.example.pocketapi.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.ui.theme.*

@Composable
fun JsonCodeViewer(
    code: String,
    modifier: Modifier = Modifier,
    enableSearch: Boolean = true
) {
    val context = LocalContext.current
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }

    val matchCount = remember(code, searchQuery) {
        if (searchQuery.isBlank() || code.isBlank()) 0
        else {
            var count = 0
            var idx = code.indexOf(searchQuery, ignoreCase = true)
            while (idx != -1) {
                count++
                idx = code.indexOf(searchQuery, idx + searchQuery.length, ignoreCase = true)
            }
            count
        }
    }

    val annotatedText = remember(code, searchQuery) {
        buildAnnotatedString {
            if (searchQuery.isBlank() || code.isBlank()) {
                append(code.ifBlank { "// Tidak ada konten balasan" })
                return@buildAnnotatedString
            }

            val queryLower = searchQuery.lowercase()
            val codeLower = code.lowercase()
            var startIndex = 0
            var matchIndex = codeLower.indexOf(queryLower, startIndex)

            while (matchIndex != -1) {
                append(code.substring(startIndex, matchIndex))
                pushStyle(
                    SpanStyle(
                        background = Color(0xFFFFD54F), // Amber/Yellow Highlight
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                )
                append(code.substring(matchIndex, matchIndex + searchQuery.length))
                pop()
                startIndex = matchIndex + searchQuery.length
                matchIndex = codeLower.indexOf(queryLower, startIndex)
            }
            if (startIndex < code.length) {
                append(code.substring(startIndex))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkBackground)
            .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
    ) {
        // Search Toolbar if enabled
        if (enableSearch && code.isNotBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari di JSON...", fontSize = 11.sp, color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkBackground,
                        unfocusedContainerColor = DarkBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                )

                if (searchQuery.isNotBlank()) {
                    Text(
                        text = if (matchCount > 0) "$matchCount cocok" else "Tidak ada",
                        fontSize = 11.sp,
                        color = if (matchCount > 0) Color(0xFFFFD54F) else Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("PocketAPI Response", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin Konten",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            HorizontalDivider(color = DarkBorder, thickness = 0.5.dp)
        }

        Box(modifier = Modifier.weight(1f, fill = false).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(vScroll)
                    .horizontalScroll(hScroll)
            ) {
                Text(
                    text = annotatedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = if (code.isBlank()) TextMuted else TextPrimary,
                    lineHeight = 18.sp
                )
            }

            // Fallback copy button if search is disabled
            if (!enableSearch && code.isNotBlank()) {
                FilledTonalIconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("PocketAPI Response", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = DarkSurfaceElevated.copy(alpha = 0.85f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Salin Konten",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}