package com.huyi.recap.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun word(w: String) = Regex("(?<![\\p{L}])(?:$w)(?![\\p{L}])")
private val REL = Regex("(?:sau\\s*)?(\\d{1,3})\\s*(phút|giờ|tiếng|ngày)\\s*(nữa|sau)?")
private val CLOCK = Regex("(\\d{1,2})\\s*(?:giờ|h|:)\\s*(\\d{1,2}|rưỡi)?")
private val DATE = Regex("(\\d{1,2})/(\\d{1,2})")
private val WEEKDAY = Regex("thứ\\s*(hai|ba|tư|năm|sáu|bảy)|chủ nhật")
private val NUMW = mapOf("một" to 1, "hai" to 2, "ba" to 3, "bốn" to 4, "tư" to 4, "năm" to 5, "sáu" to 6, "bảy" to 7, "tám" to 8, "chín" to 9, "mười" to 10, "mười một" to 11, "mười hai" to 12)
private val NUMGIO = Regex("(?<![\\p{L}])(mười hai|mười một|mười|một|hai|ba|bốn|tư|năm|sáu|bảy|tám|chín)\\s+giờ")   // "tám giờ" → "8 giờ"; "thứ hai" không đổi vì không theo sau bởi "giờ"
private val DAYS = mapOf("hai" to 1, "ba" to 2, "tư" to 3, "năm" to 4, "sáu" to 5, "bảy" to 6)

/** Mốc nhắc trong ghi chú tiếng Việt → epoch ms; null nếu không thấy giờ/ngày. Có ngày mà không giờ → 08:00. */
fun parseWhen(text: String, now: LocalDateTime = LocalDateTime.now()): Long? {
    val s = NUMGIO.replace(text.lowercase()) { "${NUMW[it.groupValues[1]]} giờ" }
    val zone = ZoneId.systemDefault()
    fun ms(t: LocalDateTime) = t.atZone(zone).toInstant().toEpochMilli()

    // "sau 30 phút" / "2 giờ nữa" — "N giờ" trần (vd "3 giờ chiều") là giờ đồng hồ, không phải khoảng
    REL.find(s)?.takeIf { it.groupValues[3].isNotEmpty() || it.value.startsWith("sau") }?.let {
        val n = it.groupValues[1].toLong()
        return ms(when (it.groupValues[2]) { "phút" -> now.plusMinutes(n); "ngày" -> now.plusDays(n); else -> now.plusHours(n) })
    }

    var date: LocalDate? = when {
        word("ngày kia|mốt").containsMatchIn(s) -> now.toLocalDate().plusDays(2)
        word("ngày mai|mai").containsMatchIn(s) -> now.toLocalDate().plusDays(1)
        word("hôm nay|nay").containsMatchIn(s) -> now.toLocalDate()
        else -> null
    }
    WEEKDAY.find(s)?.let { m ->
        val dow = DayOfWeek.of(DAYS[m.groupValues[1]] ?: 7)
        val ahead = (dow.value - now.dayOfWeek.value + 7) % 7
        date = now.toLocalDate().plusDays(if (ahead == 0) 7 else ahead.toLong())
    }
    DATE.find(s)?.let { m ->
        val d = m.groupValues[1].toInt(); val mo = m.groupValues[2].toInt()
        if (d in 1..31 && mo in 1..12) runCatching {
            var x = LocalDate.of(now.year, mo, d)
            if (x.isBefore(now.toLocalDate())) x = x.plusYears(1)
            date = x
        }
    }

    val clock = CLOCK.find(s)?.let { m ->
        var h = m.groupValues[1].toInt()
        val min = m.groupValues[2].let { if (it == "rưỡi") 30 else it.toIntOrNull() ?: 0 }
        if (h > 23 || min > 59) return@let null
        if (h < 12 && word("chiều|tối").containsMatchIn(s)) h += 12
        if (h in 1..5 && word("trưa").containsMatchIn(s)) h += 12
        LocalTime.of(h, min)
    }
    if (date == null && clock == null) return null
    val t = clock ?: when {   // chỉ có ngày: mặc định theo buổi
        word("tối").containsMatchIn(s) -> LocalTime.of(19, 0)
        word("chiều").containsMatchIn(s) -> LocalTime.of(15, 0)
        word("trưa").containsMatchIn(s) -> LocalTime.of(12, 0)
        else -> LocalTime.of(8, 0)
    }
    var at = LocalDateTime.of(date ?: now.toLocalDate(), t)
    if (!at.isAfter(now)) at = when {
        date == null -> at.plusDays(1)   // chỉ có giờ và đã qua → mai
        clock == null && date == now.toLocalDate() -> now.plusHours(1).withSecond(0).withNano(0)   // "tối nay" lúc đã quá 19:00 → 1 giờ nữa
        else -> at
    }
    return ms(at)
}
