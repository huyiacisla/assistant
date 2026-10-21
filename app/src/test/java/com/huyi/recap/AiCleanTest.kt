package com.huyi.recap

import com.huyi.recap.feature.ai.CLEAN_MAX
import com.huyi.recap.feature.ai.cleanable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCleanTest {
    @Test fun longTranscriptIsNotSentToAi() {
        assertTrue(cleanable("x".repeat(600)))          // trước đây bị cắt ở 500 rồi thay chữ gốc
        assertTrue(cleanable("x".repeat(CLEAN_MAX)))
        assertFalse(cleanable("x".repeat(CLEAN_MAX + 1)))
    }
}
