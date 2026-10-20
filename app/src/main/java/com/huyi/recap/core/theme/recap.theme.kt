package com.huyi.recap.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huyi.recap.R

/** Token theo documents/05-designs/ui_redesign_prototype_20-10-2026.md §2. ponytail: chỉ light, thêm dark khi cần. */
val BeVietnam = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
)

/** Token màu dùng chung (DESIGN.md §8) — Compose + overlay View. */
object Tok {
    val Bg = Color(0xFFF2F2F7); val Ink2 = Color(0xFF5C5C69)
    val Line = Color(0x0D000000); val LineStrong = Color(0x1A000000); val ChipOn = Color(0xFFE6E1FF)
    val Blue = Color(0xFF007AFF); val Green = Color(0xFF34C759); val Purple = Color(0xFFAF52DE)
    val Orange = Color(0xFFFF9500); val Red = Color(0xFFFF3B30)
    val Pill = Color(0xE6262629); val Sheet = Color(0xF2FFFFFF); val Scrim = Color(0x66FFFFFF); val Handle = Color(0x33000000)
    val Pastel = listOf(0xFFE6E1FF, 0xFFFFD6E8, 0xFFD6F2FF, 0xFFE8F5E9, 0xFFFEF3C7, 0xFFFFEDD5).map(::Color)
}

private val colors = lightColorScheme(
    primary = Color.Black, onPrimary = Color.White,
    secondary = Tok.Blue,
    tertiary = Tok.Purple,
    background = Tok.Bg, onBackground = Color.Black,
    surface = Color.White, onSurface = Color.Black,
    surfaceVariant = Color(0xFFE5E5EA), onSurfaceVariant = Tok.Ink2,
    secondaryContainer = Tok.ChipOn, onSecondaryContainer = Color.Black,
    outline = Tok.LineStrong, outlineVariant = Tok.Line,
)

// DESIGN.md §9
private val dark = darkColorScheme(
    primary = Color.White, onPrimary = Color.Black,
    secondary = Tok.Blue, tertiary = Tok.Purple,
    background = Color(0xFF0E0E10), onBackground = Color.White,
    surface = Color(0xFF18181B), onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2E), onSurfaceVariant = Color(0xFFA0A0AB),
    secondaryContainer = Color(0xFF3A3560), onSecondaryContainer = Color.White,
    outline = Color(0x33FFFFFF), outlineVariant = Color(0x1AFFFFFF),
)

private fun t(size: Int, w: FontWeight = FontWeight.Normal, track: Double = 0.0) =
    TextStyle(fontFamily = BeVietnam, fontWeight = w, fontSize = size.sp, letterSpacing = track.sp)

private val type = Typography(
    headlineMedium = t(28, FontWeight.Bold, -0.8),
    titleLarge = t(22, FontWeight.SemiBold, -0.5),
    titleMedium = t(16, FontWeight.SemiBold),
    bodyLarge = t(14), bodyMedium = t(13), bodySmall = t(12),
    labelLarge = t(14, FontWeight.Medium), labelMedium = t(12, FontWeight.Medium), labelSmall = t(11, FontWeight.Medium),
)

private val shapes = Shapes(
    small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(18.dp), large = RoundedCornerShape(22.dp),
)

@Composable
fun RecapTheme(content: @Composable () -> Unit) = MaterialTheme(if (isSystemInDarkTheme()) dark else colors, shapes, type, content)

/** Chữ xanh trên nền xanh 12%: ≥4.5:1 ở cả sáng (#0050B3) lẫn tối (#6FB1FF). */
@Composable
fun blueInk() = if (isSystemInDarkTheme()) Color(0xFF6FB1FF) else Color(0xFF0050B3)
