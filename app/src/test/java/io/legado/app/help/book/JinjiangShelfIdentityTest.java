package io.legado.app.help.book;

import org.junit.Test;
import static org.junit.Assert.*;

public class JinjiangShelfIdentityTest {
    @Test public void actualIndicatorMatchesRenamedWorkButNotNewWorkAndClearsAfterRemoval() {
        java.util.Set<String> shelf = new java.util.HashSet<>();
        JinjiangShelfIdentity.addKeys(shelf, "old title", "author", "http://app-cdn.jjwxc.net/androidapi/novelbasicinfo?novelId=6057684");
        assertTrue(JinjiangShelfIdentity.contains(shelf, "new title", "author", "https://www.jjwxc.net/onebook.php?novelid=6057684"));
        assertFalse(JinjiangShelfIdentity.contains(shelf, "new work", "author", "https://www.jjwxc.net/onebook.php?novelid=999"));
        shelf.clear();
        assertFalse(JinjiangShelfIdentity.contains(shelf, "new title", "author", "https://www.jjwxc.net/onebook.php?novelid=6057684"));
    }
    @Test public void existingNameAuthorAndExactUrlBehaviorIsPreservedForOtherSites() {
        java.util.Set<String> shelf = new java.util.HashSet<>();
        JinjiangShelfIdentity.addKeys(shelf, "title", "author", "https://www.gongzicp.com/novel-12.html");
        assertTrue(JinjiangShelfIdentity.contains(shelf, "title", "author", "https://example.org/book"));
        assertTrue(JinjiangShelfIdentity.contains(shelf, "changed", "author", "https://www.gongzicp.com/novel-12.html"));
        assertFalse(JinjiangShelfIdentity.contains(shelf, "changed", "author", "https://www.gongzicp.com/novel-13.html"));
        assertFalse(JinjiangShelfIdentity.contains(shelf, "title", "other author", "https://example.org/book"));
        assertTrue(JinjiangShelfIdentity.contains(shelf, "title", "", "https://example.org/book"));
    }
    private String key(String url) { return JinjiangShelfIdentity.key(url); }

    @Test public void renamedBookKeepsIdentityAcrossApiAndWebUrls() {
        String shelf = key("http://app-cdn.jjwxc.net/androidapi/novelbasicinfo?novelId=6057684");
        assertNotNull(shelf);
        assertEquals(shelf, key("https://www.jjwxc.net/onebook.php?novelid=6057684"));
        assertEquals(shelf, key("https://app.jjwxc.org/androidapi/novelbasicinfo?novelId=6057684&versionCode=513"));
    }
    @Test public void queryOrderCaseEncodingAndRequestOptionsDoNotChangeIdentity() {
        String expected = key("https://app-cdn.jjwxc.net/androidapi/novelbasicinfo?novelId=6057684");
        assertEquals(expected, key("https://WWW.JJWXC.NET/onebook.php?other=x&NOVELID=6057684#toc"));
        assertEquals(expected, key("https://www.jjwxc.net/onebook.php?novel%69d=%36%30%35%37%36%38%34"));
        assertEquals(expected, key("https://www.jjwxc.net/onebook.php?novelid=6057684, {\"headers\":{\"Referer\":\"https://example.org\"}}"));
        assertEquals(expected, key("https://www.jjwxc.net/onebook.php?novelid=06057684"));
    }
    @Test public void existingJinjiangBridgeUrlsMatchTheSameWork() {
        String expected = key("https://www.jjwxc.net/onebook.php?novelid=6057684");
        assertEquals(expected, key("https://sangtacviet.app/truyen/jjwxc/1/6057684/"));
        assertEquals(expected, key("http://sangtacviet.com/truyen/jjwxc/1/6057684/"));
    }
    @Test public void differentWorksNeverMatchEvenWithSimilarTitles() {
        assertNotEquals(key("https://www.jjwxc.net/onebook.php?novelid=1"), key("https://www.jjwxc.net/onebook.php?novelid=2"));
    }
    @Test public void untrustedDomainsAndUnrelatedPathsDoNotProduceIdentity() {
        String[] urls = {"https://eviljjwxc.net/onebook.php?novelid=1", "https://jjwxc.net.evil.org/onebook.php?novelid=1", "https://jjwxc.net@evil.org/onebook.php?novelid=1", "https://www.jjwxc.net/author.php?novelid=1", "https://www.jjwxc.net/onebook.php?next=https://evil.org/?novelid=1", "https://www.gongzicp.com/novel-1.html", "https://evil.org/onebook.php?novelid=1", "https://sangtacviet.app/truyen/qidian/1/6057684/"};
        for (String url : urls) assertNull(url, key(url));
    }
    @Test public void missingMalformedAndConflictingIdsAreRejected() {
        String[] urls = {null,"", "not a url", "https://www.jjwxc.net/onebook.php", "https://www.jjwxc.net/onebook.php?novelid=", "https://www.jjwxc.net/onebook.php?novelid=0", "https://www.jjwxc.net/onebook.php?novelid=-1", "https://www.jjwxc.net/onebook.php?novelid=1abc", "https://www.jjwxc.net/onebook.php?novelid=1&novelId=2", "https://www.jjwxc.net/onebook.php?novelid=%zz", "file://www.jjwxc.net/onebook.php?novelid=1"};
        for (String url : urls) assertNull(url, key(url));
    }
}
