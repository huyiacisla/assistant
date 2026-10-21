package com.huyi.recap

import com.huyi.recap.sync.DriveSync
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class DriveMergeTest {
    private fun a(vararg o: JSONObject) = JSONArray().also { j -> o.forEach(j::put) }
    private fun item(uid: String, text: String) = JSONObject().put("uid", uid).put("text", text).put("createdAt", 1L)

    @Test fun keepsDriveOnlyItemsAndLocalWinsOnSameUid() {
        val m = DriveSync.merge(a(item("a", "cũ"), item("b", "chỉ trên Drive")), a(item("a", "mới")))
        assertEquals(listOf("b", "a"), (0 until m.length()).map { m.getJSONObject(it).getString("uid") })
        assertEquals("mới", m.getJSONObject(1).getString("text"))
    }

    @Test fun emptyLocalNeverWipesDrive() {
        assertEquals(2, DriveSync.merge(a(item("a", "x"), item("b", "y")), JSONArray()).length())
        assertEquals(1, DriveSync.merge(null, a(item("a", "x"))).length())
    }
}
