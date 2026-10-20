package com.huyi.recap.core.util

import com.huyi.recap.feature.ai.parseAiWhen
import com.huyi.recap.feature.ai.parseAnalysis
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.*
import org.junit.Test

class RemindTest {
    private val now = LocalDateTime.of(2026, 10, 20, 12, 27) // thứ ba
    private fun at(s: String) = parseWhen(s, now)?.let { java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime() }

    @Test fun parse() {
        assertEquals(LocalDateTime.of(2026, 10, 21, 15, 0), at("họp 3h chiều mai"))
        assertEquals(LocalDateTime.of(2026, 10, 20, 12, 57), at("gọi mẹ sau 30 phút"))
        assertEquals(LocalDateTime.of(2026, 10, 20, 14, 27), at("2 giờ nữa tắt bếp"))
        assertEquals(LocalDateTime.of(2026, 10, 23, 8, 0), at("nộp bài thứ sáu"))
        assertEquals(LocalDateTime.of(2026, 11, 20, 9, 0), at("20/11 đóng tiền nhà 9 giờ sáng"))
        assertEquals(LocalDateTime.of(2026, 10, 20, 20, 30), at("8 giờ rưỡi tối nay"))
        assertEquals(LocalDateTime.of(2026, 10, 21, 7, 0), at("dậy 7h sáng"))   // giờ đã qua → mai
        assertEquals(LocalDateTime.of(2026, 10, 26, 8, 0), at("thứ hai nộp"))
        assertEquals(LocalDateTime.of(2026, 10, 25, 8, 0), at("chủ nhật đi chợ"))
        assertEquals(LocalDateTime.of(2026, 10, 20, 19, 0), at("mua sữa cho mèo tối nay"))   // buổi không giờ
        assertEquals(LocalDateTime.of(2026, 10, 21, 15, 0), at("chiều mai gặp An"))
        assertEquals(LocalDateTime.of(2026, 10, 20, 13, 27), at("việc sáng nay"))   // hôm nay + buổi đã qua → 1 giờ nữa
        assertEquals(LocalDateTime.of(2026, 10, 21, 20, 0), at("gọi mẹ tám giờ tối mai"))   // số bằng chữ
        assertEquals(LocalDateTime.of(2026, 10, 26, 8, 30), at("họp thứ hai tám giờ rưỡi"))   // "thứ hai" ≠ số 2
        assertNull(at("mua sữa"))
        assertNull(at("giá 3.5 triệu"))
    }
}

class AiWhenTest {
    private val now = LocalDateTime.of(2026, 10, 20, 12, 27)
    @Test fun parse() {
        assertNotNull(com.huyi.recap.feature.ai.parseAiWhen("{\"at\":\"2026-10-24T09:00\"}", now))
        assertNull(com.huyi.recap.feature.ai.parseAiWhen("{\"at\":null}", now))
        assertNull(com.huyi.recap.feature.ai.parseAiWhen("{\"at\":\"2026-10-19T09:00\"}", now))  // quá khứ
        assertNull(com.huyi.recap.feature.ai.parseAiWhen("xin chào", now))
    }
}

class AnalysisTest {
    @Test fun parse() {
        val a = com.huyi.recap.feature.ai.parseAnalysis("{\"kind\":\"mua\",\"tags\":[\"Sua Tuoi\",\"cho\",\"cho\",\"a\",\"b\"]}")!!
        assertEquals("mua", a.kind); assertEquals(listOf("sua-tuoi", "cho", "a"), a.tags)
        assertEquals("khac", com.huyi.recap.feature.ai.parseAnalysis("{\"kind\":\"xyz\"}")!!.kind)
        assertNull(com.huyi.recap.feature.ai.parseAnalysis("xin chào"))
        assertEquals(listOf("quan-cafe"), com.huyi.recap.feature.ai.parseAnalysis("{\"tags\":[\"hop\",\"9h\",\"mai\",\"19:00\",\"Thu Hai\",\"quan-cafe\"]}")!!.tags)
    }
}
