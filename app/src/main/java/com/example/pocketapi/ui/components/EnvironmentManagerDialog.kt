package com.example.pocketapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.data.model.Environment
import com.example.pocketapi.data.model.EnvironmentVariable
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurface
import com.example.pocketapi.ui.theme.DarkSurfaceElevated

@Composable
fun EnvironmentManagerDialog(
    environments: List<Environment>,
    activeEnvironmentId: String?,
    onSelectActiveEnvironment: (String?) -> Unit,
    onSaveEnvironments: (List<Environment>) -> Unit,
    onDismiss: () -> Unit
) {
    var envList by remember { mutableStateOf(environments) }
    var selectedEnvId by remember { mutableStateOf(activeEnvironmentId ?: envList.firstOrNull()?.id) }
    val currentEnv = envList.find { it.id == selectedEnvId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Kelola Environment Variables", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Environment Selector & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var dropdownExpanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { dropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = currentEnv?.name ?: "Pilih Environment",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            envList.forEach { env ->
                                DropdownMenuItem(
                                    text = { Text(env.name, fontSize = 13.sp) },
                                    onClick = {
                                        selectedEnvId = env.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Add new Environment
                    IconButton(
                        onClick = {
                            val newEnv = Environment(
                                name = "Env #${envList.size + 1}",
                                variables = listOf(
                                    EnvironmentVariable(key = "baseUrl", value = "https://api.example.com")
                                )
                            )
                            envList = envList + newEnv
                            selectedEnvId = newEnv.id
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Env", tint = MaterialTheme.colorScheme.primary)
                    }

                    // Delete current Environment
                    if (currentEnv != null && envList.size > 1) {
                        IconButton(
                            onClick = {
                                envList = envList.filter { it.id != currentEnv.id }
                                selectedEnvId = envList.firstOrNull()?.id
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus Env", tint = Color(0xFFEF5350))
                        }
                    }
                }

                if (currentEnv != null) {
                    // Environment Name Field
                    OutlinedTextField(
                        value = currentEnv.name,
                        onValueChange = { newName ->
                            envList = envList.map {
                                if (it.id == currentEnv.id) it.copy(name = newName) else it
                            }
                        },
                        label = { Text("Nama Environment", fontSize = 12.sp) },
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
                        text = "Variabel (Gunakan {{key}} pada URL, Header, atau Body):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // Variables Editor
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(currentEnv.variables, key = { _, v -> v.id }) { index, variable ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Checkbox(
                                    checked = variable.isEnabled,
                                    onCheckedChange = { checked ->
                                        val updatedVars = currentEnv.variables.toMutableList()
                                        updatedVars[index] = variable.copy(isEnabled = checked)
                                        envList = envList.map {
                                            if (it.id == currentEnv.id) it.copy(variables = updatedVars) else it
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                )

                                OutlinedTextField(
                                    value = variable.key,
                                    onValueChange = { newKey ->
                                        val updatedVars = currentEnv.variables.toMutableList()
                                        updatedVars[index] = variable.copy(key = newKey)
                                        envList = envList.map {
                                            if (it.id == currentEnv.id) it.copy(variables = updatedVars) else it
                                        }
                                    },
                                    placeholder = { Text("Key (e.g. baseUrl)", fontSize = 11.sp) },
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

                                OutlinedTextField(
                                    value = variable.value,
                                    onValueChange = { newVal ->
                                        val updatedVars = currentEnv.variables.toMutableList()
                                        updatedVars[index] = variable.copy(value = newVal)
                                        envList = envList.map {
                                            if (it.id == currentEnv.id) it.copy(variables = updatedVars) else it
                                        }
                                    },
                                    placeholder = { Text("Value", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = DarkSurfaceElevated,
                                        unfocusedContainerColor = DarkSurfaceElevated,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = DarkBorder
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                IconButton(
                                    onClick = {
                                        val updatedVars = currentEnv.variables.filterIndexed { i, _ -> i != index }
                                        envList = envList.map {
                                            if (it.id == currentEnv.id) it.copy(variables = updatedVars) else it
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            val updatedVars = currentEnv.variables + EnvironmentVariable()
                            envList = envList.map {
                                if (it.id == currentEnv.id) it.copy(variables = updatedVars) else it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah Variabel", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveEnvironments(envList)
                    onSelectActiveEnvironment(selectedEnvId)
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan & Terapkan")
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
