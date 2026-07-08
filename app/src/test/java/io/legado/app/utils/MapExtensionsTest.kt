package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapExtensionsTest {

    @Test
    fun hasChecksKeyPresence() {
        val map = hashMapOf("Content-Type" to "text/html")
        assertTrue(map.has("Content-Type"))
        assertFalse(map.has("Accept"))
    }

    @Test
    fun hasSupportsIgnoreCase() {
        val map = hashMapOf("Content-Type" to "text/html")
        assertFalse(map.has("content-type"))
        assertTrue(map.has("content-type", ignoreCase = true))
    }

    @Test
    fun getReturnsValueWithOptionalIgnoreCase() {
        val map = hashMapOf("Content-Type" to "text/html")
        assertEquals("text/html", map.get("Content-Type"))
        assertNull(map.get("content-type"))
        assertEquals("text/html", map.get("content-type", ignoreCase = true))
    }

    @Test
    fun getReturnsNullForMissingKey() {
        val map = hashMapOf("a" to "1")
        assertNull(map.get("b"))
    }

    @Test
    fun getOrPutLimitStoresUntilLimitReached() {
        val map = mutableMapOf<String, Int>()
        assertEquals(1, map.getOrPutLimit("a", 2) { 1 })
        assertEquals(2, map.getOrPutLimit("b", 2) { 2 })
        // limit reached: value computed but not stored
        assertEquals(3, map.getOrPutLimit("c", 2) { 3 })
        assertEquals(2, map.size)
        assertFalse(map.containsKey("c"))
    }

    @Test
    fun getOrPutLimitReturnsExistingValueWithoutRecomputing() {
        val map = mutableMapOf("a" to 10)
        val result = map.getOrPutLimit("a", 5) { throw IllegalStateException("should not compute") }
        assertEquals(10, result)
    }
}
