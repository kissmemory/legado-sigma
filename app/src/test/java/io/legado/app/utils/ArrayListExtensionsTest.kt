package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class ArrayListExtensionsTest {

    @Test
    fun indexOfFromStartIndexFindsElement() {
        val list = arrayListOf("a", "b", "c", "b")
        assertEquals(1, list.indexOf("b", 0))
        assertEquals(3, list.indexOf("b", 2))
    }

    @Test
    fun indexOfReturnsMinusOneWhenNotFoundFromStartIndex() {
        val list = arrayListOf("a", "b", "c")
        assertEquals(-1, list.indexOf("a", 1))
    }

    @Test
    fun indexOfReturnsMinusTwoWhenStartIndexOutOfBounds() {
        val list = arrayListOf("a", "b")
        assertEquals(-2, list.indexOf("a", 2))
        assertEquals(-2, list.indexOf("a", 5))
    }

    @Test
    fun indexOfFindsElementAtStartIndex() {
        val list = arrayListOf(10, 20, 30)
        assertEquals(2, list.indexOf(30, 2))
    }
}
