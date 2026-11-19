package io.legado.app.help.book

import io.legado.app.data.entities.Book
import org.junit.Assert.*
import org.junit.Test

class BookInfoRefreshTest {
    @Test fun explicitRefreshDropsResponseCachesButPreservesUserFieldsAndRuleVariables() {
        val book = Book(bookUrl="https://www.jjwxc.net/onebook.php?novelid=123", name="手写名", group=32,
            durChapterIndex=41, durChapterPos=9, intro="旧简介", customIntro="手写简介",
            variable="""{"collection":"10"}""")
        book.infoHtml="old info response";book.tocHtml="old toc response"
        BookInfoRefresh.prepare(book, true)
        assertNull(book.infoHtml);assertNull(book.tocHtml)
        assertEquals("手写名",book.name);assertEquals(32L,book.group)
        assertEquals(41,book.durChapterIndex);assertEquals(9,book.durChapterPos)
        assertEquals("手写简介",book.customIntro);assertEquals("""{"collection":"10"}""",book.variable)
        // Source rules still run on this same record, including explicit upCustomIntro.
        book.intro="新简介 收藏20";book.variable="""{"collection":"20"}""";book.upCustomIntro()
        assertEquals("新简介 收藏20",book.getDisplayIntro());assertEquals("""{"collection":"20"}""",book.variable)
    }

    @Test fun initialDiscoveryResponseCanStillBeUsedWithoutForcingANetworkRefresh() {
        val book=Book();book.infoHtml="search response";book.tocHtml="same response"
        BookInfoRefresh.prepare(book,false)
        assertEquals("search response",book.infoHtml);assertEquals("same response",book.tocHtml)
    }

    @Test fun exactShelfMatchNeverLoadsTheWholeShelfToObserveATitle() {
        val book=Book(bookUrl="https://www.jjwxc.net/onebook.php?novelid=123",name="旧名")
        val result=BookTitleHistory.matchingShelf(book,{url->if(url==book.bookUrl)book else null},{error("whole shelf lookup")})
        assertEquals(listOf(book),result)
    }

    @Test fun alternateSourceUrlLooksUpOnlyMatchingIdsInsteadOfMaterializingAllBooks() {
        val opened=Book(bookUrl="https://www.jjwxc.net/onebook.php?novelid=123")
        val saved=Book(bookUrl="https://android.jjwxc.net/androidapi/novelbasicinfo?novelId=123")
        val calls=mutableListOf<String>()
        val result=BookTitleHistory.matchingShelf(opened,{url->calls.add(url);if(url==saved.bookUrl)saved else null},{listOf("https://www.jjwxc.net/onebook.php?novelid=999",saved.bookUrl)})
        assertEquals(listOf(saved),result);assertFalse(calls.any{it.contains("999")})
    }
}
