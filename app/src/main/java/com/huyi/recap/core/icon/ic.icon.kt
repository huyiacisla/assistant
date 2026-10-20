package com.huyi.recap.core.icon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Icon Material + chữ cùng dòng; thay cho emoji/ký tự (⏰ ✓ ✕ ▾). */
@Composable
fun IconLabel(icon: ImageVector, text: String, tint: Color, size: Int = 12, weight: FontWeight? = null) =
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, Modifier.size((size + 2).dp), tint = tint)
        Text(text, fontSize = size.sp, fontWeight = weight, color = tint)
    }
