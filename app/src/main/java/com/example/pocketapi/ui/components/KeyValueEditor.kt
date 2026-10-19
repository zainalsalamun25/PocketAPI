package com.example.pocketapi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.QueryParamItem
import com.example.pocketapi.ui.theme.DarkBorder
import com.example.pocketapi.ui.theme.DarkSurfaceElevated

@Composable
fun KeyValueEditor(
    items: List<HeaderItem>,
    onItemChange: (Int, HeaderItem) -> Unit,
    onItemRemove: (Int) -> Unit,
    onItemAdd: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "Tidak ada header khusus yang ditambahkan.",
    keyPlaceholder: String = "Key (e.g. Authorization)",
    valPlaceholder: String = "Value (e.g. Bearer token)"
) {
    KeyValueListEditor(
        items = items,
        getKey = { it.key },
        getValue = { it.value },
        getIsEnabled = { it.isEnabled },
        updateItem = { item, isEnabled, key, value -> item.copy(isEnabled = isEnabled, key = key, value = value) },
        onItemChange = onItemChange,
        onItemRemove = onItemRemove,
        onItemAdd = onItemAdd,
        modifier = modifier,
        emptyMessage = emptyMessage,
        keyPlaceholder = keyPlaceholder,
        valPlaceholder = valPlaceholder
    )
}

@Composable
@JvmName("QueryParamsEditor")
fun KeyValueEditor(
    items: List<QueryParamItem>,
    onItemChange: (Int, QueryParamItem) -> Unit,
    onItemRemove: (Int) -> Unit,
    onItemAdd: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String = "Tidak ada query parameter yang ditambahkan.",
    keyPlaceholder: String = "Key (e.g. page)",
    valPlaceholder: String = "Value (e.g. 1)"
) {
    KeyValueListEditor(
        items = items,
        getKey = { it.key },
        getValue = { it.value },
        getIsEnabled = { it.isEnabled },
        updateItem = { item, isEnabled, key, value -> item.copy(isEnabled = isEnabled, key = key, value = value) },
        onItemChange = onItemChange,
        onItemRemove = onItemRemove,
        onItemAdd = onItemAdd,
        modifier = modifier,
        emptyMessage = emptyMessage,
        keyPlaceholder = keyPlaceholder,
        valPlaceholder = valPlaceholder
    )
}

@Composable
private fun <T> KeyValueListEditor(
    items: List<T>,
    getKey: (T) -> String,
    getValue: (T) -> String,
    getIsEnabled: (T) -> Boolean,
    updateItem: (T, Boolean, String, String) -> T,
    onItemChange: (Int, T) -> Unit,
    onItemRemove: (Int) -> Unit,
    onItemAdd: () -> Unit,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    keyPlaceholder: String,
    valPlaceholder: String
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (items.isEmpty()) {
            Text(
                text = emptyMessage,
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Checkbox(
                    checked = getIsEnabled(item),
                    onCheckedChange = { isChecked ->
                        onItemChange(index, updateItem(item, isChecked, getKey(item), getValue(item)))
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = getKey(item),
                    onValueChange = { onKey ->
                        onItemChange(index, updateItem(item, getIsEnabled(item), onKey, getValue(item)))
                    },
                    placeholder = { Text(keyPlaceholder, fontSize = 12.sp) },
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
                    value = getValue(item),
                    onValueChange = { onVal ->
                        onItemChange(index, updateItem(item, getIsEnabled(item), getKey(item), onVal))
                    },
                    placeholder = { Text(valPlaceholder, fontSize = 12.sp) },
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
                    onClick = { onItemRemove(index) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onItemAdd,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Tambah Baris", fontSize = 13.sp)
        }
    }
}
