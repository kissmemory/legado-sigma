package io.legado.app.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlFormatterTest {

    @Test
    fun nullReturnsEmptyString() {
        assertEquals("", HtmlFormatter.format(null))
    }

    @Test
    fun emptyStringStaysEmpty() {
        assertEquals("", HtmlFormatter.format(""))
    }

    @Test
    fun nbspEntitiesBecomeSpace() {
        assertEquals("a b", HtmlFormatter.format("a&nbsp;b"))
        // consecutive &nbsp; collapse to a single space
        assertEquals("a b", HtmlFormatter.format("a&nbsp;&nbsp;&nbsp;b"))
    }

    @Test
    fun enAndEmSpacesBecomeSpace() {
        assertEquals("a b", HtmlFormatter.format("a&ensp;b"))
        assertEquals("a b", HtmlFormatter.format("a&emsp;b"))
    }

    @Test
    fun nonPrintingCharactersRemoved() {
        assertEquals("ab", HtmlFormatter.format("a\u200Cb"))
        assertEquals("ab", HtmlFormatter.format("a\u200Db"))
    }

    @Test
    fun commentsRemoved() {
        assertEquals("ab", HtmlFormatter.format("a<!--comment-->b"))
    }

    @Test
    fun inlineTagsStripped() {
        assertEquals("x", HtmlFormatter.format("<span>x</span>"))
        assertEquals("bold", HtmlFormatter.format("<b>bold</b>"))
        assertEquals("link", HtmlFormatter.format("<a href=\"http://x\">link</a>"))
    }
}
