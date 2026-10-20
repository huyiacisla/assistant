package com.huyi.recap.core.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun dayOf(ms: Long): LocalDate = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
private val HOUR = DateTimeFormatter.ofPattern("HH:mm")
fun hourOf(ms: Long) = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).format(HOUR)
fun dow(d: LocalDate) = if (d.dayOfWeek == DayOfWeek.SUNDAY) "CN" else "T${d.dayOfWeek.value + 1}"
fun dayLabel(d: LocalDate) = if (d == LocalDate.now()) "Hôm nay" else "${dow(d)}, ${d.dayOfMonth} thg ${d.monthValue}"
