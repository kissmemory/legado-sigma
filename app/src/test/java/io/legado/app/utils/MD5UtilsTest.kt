package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class MD5UtilsTest {

    @Test
    fun md5EncodeKnownVector() {
        // MD5("abc") == 900150983cd24fb0d6963f7d28e17f72
        assertEquals("900150983cd24fb0d6963f7d28e17f72", MD5Utils.md5Encode("abc"))
    }

    @Test
    fun md5EncodeEmptyString() {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MD5Utils.md5Encode(""))
    }

    @Test
    fun md5EncodeProducesLowercase32HexChars() {
        val hash = MD5Utils.md5Encode("legado")
        assertEquals(32, hash.length)
        assertEquals(hash.lowercase(), hash)
        assertEquals(true, hash.matches(Regex("[0-9a-f]{32}")))
    }

    @Test
    fun md5Encode16ReturnsMiddle16Chars() {
        val full = MD5Utils.md5Encode("abc")
        assertEquals(full.substring(8, 24), MD5Utils.md5Encode16("abc"))
        assertEquals(16, MD5Utils.md5Encode16("abc").length)
    }

    @Test
    fun md5EncodeInputStreamMatchesStringEncode() {
        val stream = ByteArrayInputStream("abc".toByteArray())
        assertEquals(MD5Utils.md5Encode("abc"), MD5Utils.md5Encode(stream))
    }
}
