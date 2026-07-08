package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AlphanumComparatorTest {

    private fun sign(v: Int) = Integer.signum(v)

    @Test
    fun equalStringsCompareToZero() {
        assertEquals(0, AlphanumComparator.compare("abc", "abc"))
        assertEquals(0, AlphanumComparator.compare("file12", "file12"))
    }

    @Test
    fun numericChunksSortNumericallyNotLexically() {
        // "file2" should come before "file10" (2 < 10), unlike plain String sort
        assertTrue(AlphanumComparator.compare("file2", "file10") < 0)
        assertTrue(AlphanumComparator.compare("file10", "file2") > 0)
        // sanity: plain lexical order would be the opposite
        assertTrue("file10" < "file2")
    }

    @Test
    fun sameLengthNumericChunksComparedDigitByDigit() {
        assertTrue(AlphanumComparator.compare("a12", "a13") < 0)
        assertTrue(AlphanumComparator.compare("a13", "a12") > 0)
    }

    @Test
    fun leadingNumericChunkComparedNumerically() {
        assertTrue(AlphanumComparator.compare("2a", "10a") < 0)
        assertTrue(AlphanumComparator.compare("10a", "2a") > 0)
    }

    @Test
    fun textChunksComparedLexically() {
        assertTrue(AlphanumComparator.compare("abc", "abd") < 0)
        assertTrue(AlphanumComparator.compare("abd", "abc") > 0)
    }

    @Test
    fun shorterPrefixSortsFirstWhenOtherwiseEqual() {
        assertTrue(AlphanumComparator.compare("chapter", "chapter1") < 0)
        assertTrue(AlphanumComparator.compare("chapter1", "chapter") > 0)
    }

    @Test
    fun comparatorIsAntisymmetric() {
        val a = "第2章"
        val b = "第10章"
        assertEquals(sign(AlphanumComparator.compare(a, b)), -sign(AlphanumComparator.compare(b, a)))
    }

    @Test
    fun sortingProducesNaturalOrder() {
        val input = mutableListOf("第10章", "第2章", "第1章", "第20章")
        input.sortWith(AlphanumComparator)
        assertEquals(listOf("第1章", "第2章", "第10章", "第20章"), input)
    }
}
