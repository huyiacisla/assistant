package com.huyi.recap.feature.recap

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.core.theme.Tok
import com.huyi.recap.shared.model.ItemType
import com.huyi.recap.store.Item
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun dayStart(): Long = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis

private fun kind(i: Item) = when {
    i.source == "voice" -> "Ghi chú thoại"
    i.type == ItemType.Image.db -> "Ảnh"
    i.type == ItemType.Link.db -> "Link"
    else -> "Văn bản"
}

/** Recap hôm nay — thống kê local (chưa có AI tóm tắt: Phase 5). */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecapScreen(all: List<Item>, onBack: () -> Unit) {
    val today = all.filter { it.createdAt >= dayStart() }
    val fmt = SimpleDateFormat("HH:mm", Locale("vi"))
    val range = if (today.isEmpty()) "Chưa có gì hôm nay" else "Từ ${fmt.format(Date(today.minOf { it.createdAt }))} – ${fmt.format(Date(today.maxOf { it.createdAt }))}"
    val kinds = today.groupingBy(::kind).eachCount().entries.sortedByDescending { it.value }
    val tags = today.flatMap { it.tags.split(' ').filter(String::isNotBlank).distinct() }.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }.take(3)
    val tagTotal = tags.sumOf { it.value }.coerceAtLeast(1)
    val ctx = LocalContext.current
    val summary = buildString {
        append("Recap hôm nay — ${today.size} mục ($range)")
        kinds.forEach { (k, n) -> append("\n• $k: $n") }
        if (tags.isNotEmpty()) append("\nTag nổi bật: " + tags.joinToString(" ") { "#${it.key}" })
    }
    androidx.compose.material3.Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        androidx.compose.material3.TopAppBar(
            title = { Text("Recap hôm nay", fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onBack) { androidx.compose.material3.Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại") } },
            colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
        )
    }) { pad ->
    Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Brush.linearGradient(Tok.Pastel.take(3))).padding(24.dp),
        ) {
            Text("Recap hôm nay", style = MaterialTheme.typography.titleLarge)
            Text(range, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${today.size}", fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text("mục đã lưu", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.FilledTonalButton({
                ctx.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Recap", summary))
                Toast.makeText(ctx, "Đã sao chép", Toast.LENGTH_SHORT).show()
            }, Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Outlined.ContentCopy, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Sao chép") }
            Button({
                ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, summary), null))
            }, Modifier.weight(1f)) { androidx.compose.material3.Icon(Icons.Outlined.Share, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Chia sẻ") }
        }
        Panel("Đã lưu theo loại") {
            if (kinds.isEmpty()) Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant)
            kinds.forEach { (k, n) -> Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(k, fontSize = 14.sp); Text("$n", fontWeight = FontWeight.SemiBold) } }
        }
        Panel("Bạn đang đào sâu về…") {
            if (tags.isEmpty()) Text("Chưa có tag", color = MaterialTheme.colorScheme.onSurfaceVariant)
            tags.forEachIndexed { idx, (t, n) ->
                val pct = n * 100 / tagTotal
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text("#$t", fontSize = 14.sp); Text("$pct%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                androidx.compose.material3.LinearProgressIndicator({ pct / 100f }, Modifier.fillMaxWidth().height(8.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant, gapSize = 0.dp, drawStopIndicator = {})
            }
        }
        Text("AI tóm tắt (mood, ý tưởng) sẽ có ở Phase 5.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    }
}

@Composable
private fun Panel(title: String, body: @Composable () -> Unit) = Card(
    Modifier.fillMaxWidth(), RoundedCornerShape(22.dp), CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(1.dp),
) { Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp); body() } }
