package io.legado.app.help.book;

import org.junit.Test;
import static org.junit.Assert.*;

public class ChapterPurchaseMarkerTest {
    @Test public void legacyTitlesNormalizeOnlyExplicitStatus() {
        assertEquals("🥚 第9章", ChapterPurchaseMarker.normalizeLegacyTitle("🔒第9章"));
        assertEquals("🐣 第9章", ChapterPurchaseMarker.normalizeLegacyTitle("🔓第9章"));
        assertEquals("普通标题", ChapterPurchaseMarker.normalizeLegacyTitle("普通标题"));
        assertNull(ChapterPurchaseMarker.normalizeLegacyTitle(null));
    }
    @Test public void infoRefreshKeepsStatusOnlyForTheSameChapter() {
        assertEquals("🐣 第9章", ChapterPurchaseMarker.mergeLatestTitle("第9章", "🐣 第9章"));
        assertEquals("第10章", ChapterPurchaseMarker.mergeLatestTitle("第10章", "🐣 第9章"));
        assertEquals("🥚 第9章", ChapterPurchaseMarker.mergeLatestTitle("🔒第9章", "🐣 第9章"));
    }

    @Test public void bookshelfLatestChapterUsesTheSameUnpaidAndPurchasedMarkers() {
        assertEquals("🥚 第54章 遗孀", ChapterPurchaseMarker.title("第54章 遗孀",true,false,false));
        assertEquals("🐣 第54章 遗孀", ChapterPurchaseMarker.title("第54章 遗孀",true,true,false));
    }
    @Test public void freeChaptersAreNotMislabelledEvenIfSourceReportsPayTrue() {
        assertEquals("第1章",ChapterPurchaseMarker.title("第1章",false,true,false));
        assertEquals("第1章",ChapterPurchaseMarker.title("第1章",false,false,false));
        assertEquals("",ChapterPurchaseMarker.marker(false,true,false));
    }
    @Test public void volumeHeadersHaveNoPurchaseMarker() {
        assertEquals("番外卷",ChapterPurchaseMarker.title("番外卷",true,false,true));
        assertEquals("",ChapterPurchaseMarker.marker(true,true,true));
    }
    @Test public void refreshDoesNotDuplicateOrRetainAnOutdatedSourcePrefix() {
        assertEquals("🥚 第54章",ChapterPurchaseMarker.title("🥚 第54章",true,false,false));
        assertEquals("🐣 第54章",ChapterPurchaseMarker.title("🥚 第54章",true,true,false));
        assertEquals("🥚 第54章",ChapterPurchaseMarker.title("🔒 第54章",true,false,false));
        assertEquals("🐣 第54章",ChapterPurchaseMarker.title("🔓 第54章",true,true,false));
    }
    @Test public void emptyOrUnknownTitlesDoNotBecomeMarkerOnlyRows() {
        assertEquals("",ChapterPurchaseMarker.title("",true,false,false));
        assertEquals("   ",ChapterPurchaseMarker.title("   ",true,true,false));
        assertEquals("书源原样标题",ChapterPurchaseMarker.title("书源原样标题",false,false,false));
    }
}
