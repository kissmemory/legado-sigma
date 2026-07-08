package io.legado.app.utils

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Utf8BomUtilsTest {

    private val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    @Test
    fun removeBomFromStringStrippingLeadingBom() {
        val text = "\uFEFFhello"
        assertEquals("hello", Utf8BomUtils.removeUTF8BOM(text))
    }

    @Test
    fun stringWithoutBomUnchanged() {
        val text = "hello world"
        assertEquals(text, Utf8BomUtils.removeUTF8BOM(text))
    }

    @Test
    fun removeBomFromByteArray() {
        val content = "abc".toByteArray()
        val withBom = bom + content
        assertArrayEquals(content, Utf8BomUtils.removeUTF8BOM(withBom))
    }

    @Test
    fun byteArrayWithoutBomUnchanged() {
        val content = "abc".toByteArray()
        assertArrayEquals(content, Utf8BomUtils.removeUTF8BOM(content))
    }

    @Test
    fun hasBomDetectsBomPresence() {
        assertTrue(Utf8BomUtils.hasBom(bom + "x".toByteArray()))
        assertFalse(Utf8BomUtils.hasBom("x".toByteArray()))
    }

    @Test
    fun hasBomFalseForShortArray() {
        assertFalse(Utf8BomUtils.hasBom(bom))
    }
}
