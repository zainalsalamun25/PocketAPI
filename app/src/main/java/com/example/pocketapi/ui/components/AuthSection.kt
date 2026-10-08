package com.example.pocketapi.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.data.model.ApiKeyLocation
import com.example.pocketapi.data.model.AuthConfig
import com.example.pocketapi.data.model.AuthType
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurfaceElevated
import com.example.pocketapi.util.JwtDecoder

@Composable
fun AuthSection(
    authConfig: AuthConfig,
    onAuthConfigChange: (AuthConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var showJwtInspector by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Auth Type Selector
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AuthType.entries.forEach { type ->
                FilterChip(
                    selected = authConfig.type == type,
                    onClick = { onAuthConfigChange(authConfig.copy(type = type)) },
                    label = { Text(type.label, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DarkSurfaceElevated,
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (authConfig.type == type) MaterialTheme.colorScheme.primary else DarkBorder,
                        enabled = true,
                        selected = authConfig.type == type
                    )
                )
            }
        }

        when (authConfig.type) {
            AuthType.NONE -> {
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Request ini tidak menggunakan autentikasi otomatis. Anda tetap dapat menambahkan header kustom di tab Headers.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            AuthType.BEARER -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Token / JWT", fontSize = 12.sp, color = Color.Gray)
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clipText.isNullOrBlank()) {
                                    onAuthConfigChange(authConfig.copy(bearerToken = clipText))
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste Token", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = authConfig.bearerToken,
                        onValueChange = { onAuthConfigChange(authConfig.copy(bearerToken = it)) },
                        placeholder = { Text("Paste Bearer Token / JWT di sini...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        trailingIcon = {
                            if (authConfig.bearerToken.isNotEmpty()) {
                                IconButton(onClick = { onAuthConfigChange(authConfig.copy(bearerToken = "")) }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Header 'Authorization: Bearer <token>' aktif.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (JwtDecoder.isJwt(authConfig.bearerToken)) {
                            FilledTonalButton(
                                onClick = { showJwtInspector = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Inspect Token", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            AuthType.BASIC -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = authConfig.basicUsername,
                        onValueChange = { onAuthConfigChange(authConfig.copy(basicUsername = it)) },
                        placeholder = { Text("Username", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
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

                    OutlinedTextField(
                        value = authConfig.basicPassword,
                        onValueChange = { onAuthConfigChange(authConfig.copy(basicPassword = it)) },
                        placeholder = { Text("Password", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                    Text(
                        text = "Kredensial otomatis di-encode Base64 ke 'Authorization: Basic <base64>'.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AuthType.API_KEY -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = authConfig.apiKeyName,
                            onValueChange = { onAuthConfigChange(authConfig.copy(apiKeyName = it)) },
                            placeholder = { Text("Key Name (X-Api-Key)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = DarkBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = authConfig.apiKeyValue,
                            onValueChange = { onAuthConfigChange(authConfig.copy(apiKeyValue = it)) },
                            placeholder = { Text("Key Value", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = DarkBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Kirim Lewat:", fontSize = 12.sp, color = Color.Gray)
                        ApiKeyLocation.entries.forEach { loc ->
                            FilterChip(
                                selected = authConfig.apiKeyLocation == loc,
                                onClick = { onAuthConfigChange(authConfig.copy(apiKeyLocation = loc)) },
                                label = { Text(loc.label, fontSize = 11.sp) },
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showJwtInspector && authConfig.bearerToken.isNotBlank()) {
        JwtInspectorDialog(
            jwtToken = authConfig.bearerToken,
            onDismiss = { showJwtInspector = false }
        )
    }
}