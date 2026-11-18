package io.legado.app.help.book

import io.legado.app.data.entities.BookChapter
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrentChapterMarkerTest {
    @Test fun currentChapterReflectsPurchaseChangesWithoutChangingRawTitleOrIndex() {
        val chapter = BookChapter(title = "第170章 当前章", index = 169, isVip = true)
        assertEquals("🥚 第170章 当前章", chapter.getProgressTitle(chineseConvert = false))
        chapter.isPay = true
        assertEquals("🐣 第170章 当前章", chapter.getProgressTitle(chineseConvert = false))
        assertEquals("第170章 当前章", chapter.title)
        assertEquals(169, chapter.index)
    }
    @Test fun freeAndVolumeCurrentChaptersKeepTheirTitles() {
        assertEquals("第1章", BookChapter(title = "第1章", isPay = true).getProgressTitle(chineseConvert = false))
        assertEquals("番外", BookChapter(title = "番外", isVolume = true, isVip = true).getProgressTitle(chineseConvert = false))
    }
}
