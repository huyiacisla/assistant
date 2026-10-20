package com.huyi.recap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ponytail: markdown tối giản tự viết (# ## ###, **đậm**, *nghiêng*, `code`, - list, - [ ] việc, > trích); thêm bảng/link/fence khi cần.
private val INLINE = Regex("\\*\\*(.+?)\\*\\*|\\*(.+?)\\*|`(.+?)`")
private val TASK = Regex("^(\\s*)- \\[( |x|X)] (.*)$")

/** Chữ có `**đậm**`, `*nghiêng*`, `` `code` `` → AnnotatedString. */
fun mdInline(s: String, codeBg: androidx.compose.ui.graphics.Color): AnnotatedString = buildAnnotatedString {
    var at = 0
    INLINE.findAll(s).forEach { m ->
        append(s.substring(at, m.range.first)); at = m.range.last + 1
        val (b, i, c) = m.destructured
        when {
            b.isNotEmpty() -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(b) }
            i.isNotEmpty() -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(i) }
            else -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg)) { append(c) }
        }
    }
    append(s.substring(at))
}

/** Bọc vùng chọn bằng `m` (không chọn gì → chèn cặp m m, con trỏ nằm giữa). */
fun TextFieldValue.wrap(m: String): TextFieldValue {
    val s = selection.min; val e = selection.max
    return copy(text.substring(0, s) + m + text.substring(s, e) + m + text.substring(e), TextRange(s + m.length, e + m.length))
}

/** Thêm tiền tố vào đầu dòng đang đứng; đã có đúng tiền tố đó thì gỡ ra (bấm lần hai = tắt). */
fun TextFieldValue.linePrefix(p: String): TextFieldValue {
    val s = text.lastIndexOf('\n', selection.min - 1) + 1
    if (text.startsWith(p, s)) return copy(text.removeRange(s, s + p.length), TextRange((selection.min - p.length).coerceAtLeast(s), (selection.max - p.length).coerceAtLeast(s)))
    return copy(text.substring(0, s) + p + text.substring(s), TextRange(selection.min + p.length, selection.max + p.length))
}

/** Chạm vào phần `- [ ] ` của dòng việc → đảo ☐↔☑; null nếu chạm chỗ khác. */
fun TextFieldValue.toggleTaskAt(offset: Int): TextFieldValue? {
    val ls = text.lastIndexOf('\n', offset - 1) + 1
    val le = text.indexOf('\n', ls).let { if (it < 0) text.length else it }
    val m = TASK.matchEntire(text.substring(ls, le)) ?: return null
    val at = ls + m.groupValues[1].length
    if (offset > at + 6) return null
    return copy(text = text.replaceRange(at + 3, at + 4, if (m.groupValues[2] == " ") "x" else " "))
}

/**
 * Soạn và xem là một: văn bản giữ nguyên độ dài (con trỏ/chọn vùng không lệch), chỉ tô kiểu.
 * ký hiệu `#` `**` mờ đi; `- ` → •; `- [ ] ` → ☐ (☑ + gạch ngang khi xong).
 */
fun mdTransform(faint: Color, codeBg: Color): VisualTransformation = VisualTransformation { src ->
    val t = StringBuilder(src.text)
    val marks = mutableListOf<Triple<SpanStyle, Int, Int>>()
    fun dim(a: Int, b: Int) { marks += Triple(SpanStyle(color = faint), a, b) }
    fun inline(content: String, o: Int) = INLINE.findAll(content).forEach { m ->
        val a = o + m.range.first; val b = o + m.range.last + 1
        val n = when { m.groupValues[1].isNotEmpty() -> 2; else -> 1 }
        dim(a, a + n); dim(b - n, b)
        marks += Triple(
            when {
                m.groupValues[1].isNotEmpty() -> SpanStyle(fontWeight = FontWeight.Bold)
                m.groupValues[2].isNotEmpty() -> SpanStyle(fontStyle = FontStyle.Italic)
                else -> SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg)
            }, a + n, b - n,
        )
    }
    var o = 0
    src.text.split('\n').forEach { line ->
        val task = TASK.matchEntire(line)
        val head = listOf("### " to 17, "## " to 18, "# " to 20).firstOrNull { line.startsWith(it.first) }
        when {
            task != null -> {
                val at = o + task.groupValues[1].length
                val done = task.groupValues[2] != " "
                t.replace(at, at + 6, (if (done) "☑" else "☐") + "     ")
                val rest = at + 6
                if (done) marks += Triple(SpanStyle(color = faint, textDecoration = TextDecoration.LineThrough), rest, o + line.length)
                else inline(task.groupValues[3], rest)
            }
            head != null -> {
                dim(o, o + head.first.length)
                marks += Triple(SpanStyle(fontWeight = FontWeight.Bold, fontSize = head.second.sp), o + head.first.length, o + line.length)
                inline(line.drop(head.first.length), o + head.first.length)
            }
            line.startsWith("- ") || line.startsWith("* ") -> { t.setCharAt(o, '•'); dim(o, o + 1); inline(line.drop(2), o + 2) }
            line.startsWith("> ") -> { dim(o, o + 2); marks += Triple(SpanStyle(color = faint, fontStyle = FontStyle.Italic), o + 2, o + line.length) }
            else -> inline(line, o)
        }
        o += line.length + 1
    }
    TransformedText(buildAnnotatedString { append(t.toString()); marks.forEach { addStyle(it.first, it.second, it.third) } }, OffsetMapping.Identity)
}

/** Hiển thị markdown; chạm ô ☑ gọi onChange với văn bản đã đảo dấu dòng đó. */
@Composable
fun Markdown(text: String, modifier: Modifier = Modifier, onChange: ((String) -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    val lines = text.split('\n')
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEachIndexed { idx, raw ->
            val t = raw.trimEnd()
            val task = TASK.matchEntire(t)
            when {
                t.isBlank() -> Spacer(Modifier.height(4.dp))
                t.startsWith("### ") -> Text(mdInline(t.drop(4), cs.surfaceVariant), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
                t.startsWith("## ") -> Text(mdInline(t.drop(3), cs.surfaceVariant), fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp)
                t.startsWith("# ") -> Text(mdInline(t.drop(2), cs.surfaceVariant), fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                task != null -> {
                    val done = task.groupValues[2] != " "
                    Row(Modifier.clickable(enabled = onChange != null) {
                        val flip = task.groupValues[1] + (if (done) "- [ ] " else "- [x] ") + task.groupValues[3]
                        onChange?.invoke(lines.toMutableList().also { it[idx] = flip }.joinToString("\n"))
                    }, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(if (done) Icons.Outlined.CheckBox else Icons.Outlined.CheckBoxOutlineBlank, if (done) "Đã xong" else "Chưa xong", Modifier.size(20.dp), tint = if (done) cs.onSurfaceVariant else cs.onSurface)
                        Text(mdInline(task.groupValues[3], cs.surfaceVariant), fontSize = 15.sp, lineHeight = 22.sp, color = if (done) cs.onSurfaceVariant else cs.onSurface,
                            textDecoration = if (done) androidx.compose.ui.text.style.TextDecoration.LineThrough else null)
                    }
                }
                t.startsWith("- ") || t.startsWith("* ") -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("•", fontSize = 15.sp, modifier = Modifier.width(20.dp), color = cs.onSurfaceVariant)
                    Text(mdInline(t.drop(2), cs.surfaceVariant), fontSize = 15.sp, lineHeight = 22.sp)
                }
                t.startsWith("> ") -> Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Spacer(Modifier.width(3.dp).fillMaxHeight().background(cs.outline, RoundedCornerShape(2.dp)))
                    Text(mdInline(t.drop(2), cs.surfaceVariant), fontSize = 15.sp, lineHeight = 22.sp, color = cs.onSurfaceVariant)
                }
                else -> Text(mdInline(t, cs.surfaceVariant), fontSize = 15.sp, lineHeight = 22.sp)
            }
        }
    }
}
