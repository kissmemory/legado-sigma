package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlUtilTest {

    @Test
    fun replaceReservedCharEncodesSpecialCharacters() {
        assertEquals("%20", UrlUtil.replaceReservedChar(" "))
        assertEquals("%23", UrlUtil.replaceReservedChar("#"))
        assertEquals("%26", UrlUtil.replaceReservedChar("&"))
        assertEquals("%3D", UrlUtil.replaceReservedChar("="))
        assertEquals("%3F", UrlUtil.replaceReservedChar("?"))
        assertEquals("%2F", UrlUtil.replaceReservedChar("/"))
    }

    @Test
    fun replaceReservedCharEncodesPercentFirstToAvoidDoubleEncoding() {
        // '%' -> '%25'; a literal "%20" becomes "%2520"
        assertEquals("%2520", UrlUtil.replaceReservedChar("%20"))
    }

    @Test
    fun replaceReservedCharLeavesPlainTextUnchanged() {
        assertEquals("abc123", UrlUtil.replaceReservedChar("abc123"))
    }

    @Test
    fun replaceReservedCharHandlesCombinedString() {
        assertEquals("a%20b%26c", UrlUtil.replaceReservedChar("a b&c"))
    }
}
