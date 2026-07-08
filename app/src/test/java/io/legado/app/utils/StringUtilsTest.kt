package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure (framework-independent) helpers in [StringUtils].
 */
class StringUtilsTest {

    @Test
    fun toFirstCapitalUppercasesFirstChar() {
        assertEquals("Hello", StringUtils.toFirstCapital("hello"))
        assertEquals("A", StringUtils.toFirstCapital("a"))
    }

    @Test
    fun halfToFullConvertsAsciiToFullWidth() {
        assertEquals("\uFF21", StringUtils.halfToFull("A"))
        assertEquals("\u3000", StringUtils.halfToFull(" "))
    }

    @Test
    fun fullToHalfConvertsFullWidthToAscii() {
        assertEquals("A", StringUtils.fullToHalf("\uFF21"))
        assertEquals(" ", StringUtils.fullToHalf("\u3000"))
    }

    @Test
    fun halfToFullAndFullToHalfAreInverse() {
        val ascii = "Hello123!"
        assertEquals(ascii, StringUtils.fullToHalf(StringUtils.halfToFull(ascii)))
    }

    @Test
    fun isNumericMatchesIntegersIncludingNegatives() {
        assertTrue(StringUtils.isNumeric("123"))
        assertTrue(StringUtils.isNumeric("-45"))
        assertFalse(StringUtils.isNumeric("12.3"))
        assertFalse(StringUtils.isNumeric("abc"))
        assertFalse(StringUtils.isNumeric("12a"))
    }

    @Test
    fun isContainNumberDetectsAnyDigit() {
        assertTrue(StringUtils.isContainNumber("abc9"))
        assertFalse(StringUtils.isContainNumber("abc"))
    }

    @Test
    fun chineseNumToIntConvertsSingleDigit() {
        assertEquals(5, StringUtils.chineseNumToInt("五"))
        assertEquals(10, StringUtils.chineseNumToInt("十"))
    }

    @Test
    fun chineseNumToIntConvertsCompoundNumbers() {
        assertEquals(1025, StringUtils.chineseNumToInt("一千零二十五"))
        assertEquals(1200, StringUtils.chineseNumToInt("一千二"))
    }

    @Test
    fun stringToIntParsesArabicAndChinese() {
        assertEquals(123, StringUtils.stringToInt("123"))
        assertEquals(1200, StringUtils.stringToInt("一千二"))
        assertEquals(-1, StringUtils.stringToInt(null))
    }

    @Test
    fun wordCountFormatFromInt() {
        assertEquals("", StringUtils.wordCountFormat(0))
        assertEquals("5000字", StringUtils.wordCountFormat(5000))
        assertEquals("2万字", StringUtils.wordCountFormat(20000))
        assertEquals("1.5万字", StringUtils.wordCountFormat(15000))
    }

    @Test
    fun wordCountFormatFromString() {
        assertEquals("", StringUtils.wordCountFormat(null))
        assertEquals("5000字", StringUtils.wordCountFormat("5000"))
        // non-numeric strings are returned unchanged
        assertEquals("很多", StringUtils.wordCountFormat("很多"))
    }

    @Test
    fun repeatBuildsRepeatedString() {
        assertEquals("ababab", StringUtils.repeat("ab", 3))
        assertEquals("", StringUtils.repeat("x", 0))
    }

    @Test
    fun removeUTFCharactersDecodesUnicodeEscapes() {
        assertEquals("ABC", StringUtils.removeUTFCharacters("\\u0041\\u0042C"))
        assertNull(StringUtils.removeUTFCharacters(null))
    }
}
