package com.huyi.recap.shared.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun Chip(selected: Boolean, onClick: () -> Unit, label: String) = FilterChip(
    selected, onClick, { Text(label, fontWeight = FontWeight.Medium) },
    shape = CircleShape,
    colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface, selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer),
    border = FilterChipDefaults.filterChipBorder(true, selected, borderColor = MaterialTheme.colorScheme.outline, selectedBorderColor = Color.Transparent),
)
