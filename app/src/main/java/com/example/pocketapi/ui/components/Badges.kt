package com.example.pocketapi.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocketapi.data.model.HttpMethod
import com.example.pocketapi.ui.theme.StatusError
import com.example.pocketapi.ui.theme.StatusInfo
import com.example.pocketapi.ui.theme.StatusSuccess
import com.example.pocketapi.ui.theme.StatusWarning

@Composable
fun MethodBadge(
    method: HttpMethod,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(method.color.copy(alpha = 0.2f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = method.name,
            color = method.color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun StatusCodeBadge(
    statusCode: Int,
    statusMessage: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor) = when (statusCode) {
        in 200..299 -> StatusSuccess.copy(alpha = 0.2f) to StatusSuccess
        in 300..399 -> StatusInfo.copy(alpha = 0.2f) to StatusInfo
        in 400..499 -> StatusWarning.copy(alpha = 0.2f) to StatusWarning
        in 500..599 -> StatusError.copy(alpha = 0.2f) to StatusError
        else -> Color.Gray.copy(alpha = 0.2f) to Color.LightGray
    }

    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    } else {
        modifier.clip(RoundedCornerShape(6.dp))
    }

    Box(
        modifier = clickableModifier
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val label = if (statusCode > 0) "$statusCode $statusMessage".trim() else statusMessage.ifEmpty { "Error" }
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            if (onClick != null) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Lihat Penjelasan Status",
                    tint = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}