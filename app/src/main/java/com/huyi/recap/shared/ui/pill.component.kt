package com.huyi.recap.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Pill(text: String, tint: Color, bg: Color = tint.copy(alpha = .12f), icon: androidx.compose.ui.graphics.vector.ImageVector? = null, trailing: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: (() -> Unit)? = null) =
    Row(Modifier.clip(CircleShape).background(bg).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        icon?.let { Icon(it, null, Modifier.size(13.dp), tint = tint) }
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = tint)
        trailing?.let { Icon(it, null, Modifier.size(15.dp), tint = tint) }
    }
