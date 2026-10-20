package com.huyi.recap

import com.huyi.recap.core.util.ftsQuery
import com.huyi.recap.feature.organize.suggest
import com.huyi.recap.store.Item
import org.junit.Assert.assertEquals
import org.junit.Test

class OrganizeTest {
    @Test fun groupsByCommonTagOnce() {
        val s = suggest(listOf(
            Item(id = 1, tags = "quan-cafe an-uong"), Item(id = 2, tags = "quan-cafe"), Item(id = 3, tags = "an-uong"), Item(id = 4, tags = "x"),
        ))
        assertEquals(listOf("Quan cafe"), s.map { it.name })
        assertEquals(listOf(1L, 2L), s[0].items.map { it.id })
    }
    @Test fun ftsPrefix() = assertEquals("ca* phe*", ftsQuery("cà, phê".let { "ca  phe" }))
}
