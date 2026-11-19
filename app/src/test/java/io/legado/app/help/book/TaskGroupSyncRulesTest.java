package io.legado.app.help.book;
import org.junit.Test;
import static org.junit.Assert.*;
public class TaskGroupSyncRulesTest {
 @Test public void onlyExplicitRotatingWorkGroupsAreRecognized(){
  assertTrue(TaskGroupSyncRules.isTaskGroup("⑴\n转"));assertTrue(TaskGroupSyncRules.isTaskGroup("上\n新¹"));
  assertFalse(TaskGroupSyncRules.isTaskGroup("ᴺ⁸连‼️"));assertFalse(TaskGroupSyncRules.isTaskGroup("衍生"));
 }
 @Test public void changingWorkMaskPreservesFixedCategoryAndHighBit(){
  long fixed=Long.MIN_VALUE|1024L;assertEquals(fixed|32L,TaskGroupSyncRules.workMask(fixed|16L,16L|32L,32L));
 }
 @Test public void stableChapterIdentityUsesOfficialHostAndMatchingBookId(){
  assertEquals("jjwxc:123:9",TaskGroupSyncRules.chapterRef("jj:123","https://android.jjwxc.net/androidapi/chapterContent?novelId=123&chapterId=9&sign=private"));
  assertNull(TaskGroupSyncRules.chapterRef("jj:123","https://android.jjwxc.net.evil.test/a?novelId=123&chapterId=9"));
  assertNull(TaskGroupSyncRules.chapterRef("jj:123","https://android.jjwxc.net/a?novelId=124&chapterId=9"));
  assertNull(TaskGroupSyncRules.chapterRef("jj:123","https://android.jjwxc.net/a?novelId=123&chapterId=9&chapterid=8"));
 }
 @Test public void cpChapterUrlsRemainInCachedBookContext(){
  assertEquals("gongzicp:123:99",TaskGroupSyncRules.chapterRef("cp:123","https://m.gongzicp.com/read-99.html"));
  assertEquals("chnovels:123:9",TaskGroupSyncRules.chapterRef("ch:123","https://app.chnovels.com/api/chapter/info?novel_id=123&chapter_id=9"));
 }
}
