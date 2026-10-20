package com.huyi.recap

import com.huyi.recap.feature.clipboard.isLink
import org.junit.Assert.*
import org.junit.Test

class ClipLinkTest {
    @Test fun link() {
        assertTrue(isLink("https://a.com/x?y=1"))
        assertTrue(isLink("  HTTP://a.com "))
        assertTrue(isLink("www.a.com"))
        assertFalse(isLink("httpfoo"))
        assertFalse(isLink("xem https://a.com nhé"))
        assertFalse(isLink("hello"))
    }
}
