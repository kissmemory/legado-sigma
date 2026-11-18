package io.legado.app.help.book;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class JinjiangTitleHistoryTest {
    @Test public void firstRenameKeepsTheShelfTitle() {
        List<String> seen=JinjiangTitleHistory.observe(Collections.emptyList(), "旧书名", "新书名");
        assertEquals("曾用名：旧书名\n\n最新文案", JinjiangTitleHistory.decorate(seen,"新书名","最新文案"));
    }
    @Test public void successiveRefreshesDeduplicateAndUseFreshIntro() {
        List<String> seen=JinjiangTitleHistory.observe(Collections.emptyList(),"甲","乙");
        seen=JinjiangTitleHistory.observe(seen,"甲","丙");
        seen=JinjiangTitleHistory.observe(seen,"甲","丙");
        assertEquals("曾用名：甲／乙\n\n更新后的文案",JinjiangTitleHistory.decorate(seen,"丙","更新后的文案"));
    }
    @Test public void returningToAnOldTitleDoesNotListTheCurrentTitleAsFormer() {
        List<String> seen=JinjiangTitleHistory.observe(Arrays.asList("甲","乙","丙"),"甲","乙");
        assertEquals("曾用名：甲／丙\n\n文案",JinjiangTitleHistory.decorate(seen,"乙","文案"));
    }
    @Test public void unchangedOrEmptyTitlesDoNotInventHistory() {
        List<String> seen=JinjiangTitleHistory.observe(Collections.emptyList(),"甲","甲");
        assertEquals("文案",JinjiangTitleHistory.decorate(seen,"甲","文案"));
        assertEquals(seen,JinjiangTitleHistory.observe(seen,"甲"," "));
        assertEquals("文案",JinjiangTitleHistory.decorate(Collections.emptyList(),"甲","文案"));
    }
    @Test public void richIntroMarkersAndMarkupEscapingArePreserved() {
        assertEquals("<md>曾用名：甲\\*\\[旧\\]\n\n正文",JinjiangTitleHistory.decorate(Arrays.asList("甲*[旧]","乙"),"乙","<md>正文"));
        String html=JinjiangTitleHistory.decorate(Arrays.asList("<旧&名>","新"),"新","<usehtml><b>正文</b>");
        assertEquals("<usehtml><p>曾用名：&lt;旧&amp;名&gt;</p><b>正文</b>",html);
        assertEquals("<useweb><p>曾用名：旧</p><div>正文</div>",JinjiangTitleHistory.decorate(Arrays.asList("旧","新"),"新","<useweb><div>正文</div>"));
    }
    @Test public void decorationNeverMutatesStoredTitlesOrIntro() {
        List<String> seen=Collections.unmodifiableList(Arrays.asList("旧","新"));
        assertEquals("曾用名：旧\n\n正文",JinjiangTitleHistory.decorate(seen,"新","正文"));
        assertEquals(Arrays.asList("旧","新"),seen);
        assertEquals("曾用名：旧\n\n正文",JinjiangTitleHistory.decorate(seen,"新","正文"));
    }
}
