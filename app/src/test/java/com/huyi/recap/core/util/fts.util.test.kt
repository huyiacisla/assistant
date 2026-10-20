package com.huyi.recap.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FtsTest {
    @Test fun fts() {
        assertEquals("mua* sữa*", ftsQuery("mua sữa"))
        assertEquals("a* b*", ftsQuery(" a!! \"b\" "))
    }
}
