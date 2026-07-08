package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CollectionExtensionsTest {

    @Test
    fun fastSumAddsAllElements() {
        assertEquals(6f, listOf(1f, 2f, 3f).fastSum(), 0f)
        assertEquals(0f, emptyList<Float>().fastSum(), 0f)
    }

    @Test
    fun fastBinarySearchFindsExistingElement() {
        val list = listOf(1, 3, 5, 7, 9)
        val index = list.fastBinarySearch { it.compareTo(5) }
        assertEquals(2, index)
    }

    @Test
    fun fastBinarySearchReturnsNegativeInsertionPointWhenMissing() {
        val list = listOf(1, 3, 5, 7, 9)
        // 4 would be inserted at index 2 -> -(2+1) = -3
        val index = list.fastBinarySearch { it.compareTo(4) }
        assertEquals(-3, index)
    }

    @Test
    fun fastBinarySearchByFindsByKey() {
        val list = listOf("a", "bb", "ccc", "dddd")
        val index = list.fastBinarySearchBy(3) { it.length }
        assertEquals(2, index)
    }

    @Test
    fun fastBinarySearchValidatesBounds() {
        val list = listOf(1, 2, 3)
        assertThrows(IllegalArgumentException::class.java) {
            list.fastBinarySearch(2, 1) { it.compareTo(1) }
        }
        assertThrows(IndexOutOfBoundsException::class.java) {
            list.fastBinarySearch(-1, 3) { it.compareTo(1) }
        }
        assertThrows(IndexOutOfBoundsException::class.java) {
            list.fastBinarySearch(0, 4) { it.compareTo(1) }
        }
    }

    @Test
    fun removeLastElementRemovesAndReturnsLast() {
        val list = mutableListOf(1, 2, 3)
        assertEquals(3, list.removeLastElement())
        assertEquals(listOf(1, 2), list)
    }

    @Test
    fun removeLastElementThrowsOnEmpty() {
        assertThrows(NoSuchElementException::class.java) {
            mutableListOf<Int>().removeLastElement()
        }
    }
}
