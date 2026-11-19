package io.legado.app.help.book;
import org.junit.Test;
import static org.junit.Assert.*;
public class DiscoveryBookIdentityTest {
 @Test public void stableOriginalSiteIdsDoNotDependOnTitlesOrSourceUrlForm(){
  assertEquals("jj:123",DiscoveryBookIdentity.key("https://www.jjwxc.net/onebook.php?novelid=123"));
  assertEquals("jj:123",DiscoveryBookIdentity.key("https://app.jjwxc.org/androidapi/novelbasicinfo?novelId=123"));
  assertEquals("cp:123",DiscoveryBookIdentity.key("https://webapi.gongzicp.com/novel/novelInfo?id=123"));
  assertEquals("ch:123",DiscoveryBookIdentity.key("https://app.chnovels.com/api/v3/novel_library/getNovelInfo?novel_id=123&sign=private"));
 }
 @Test public void officialDotComAliasesMatchTheSameWork(){
  assertEquals("jj:123",DiscoveryBookIdentity.key("https://android.jjwxc.com/androidapi/novelbasicinfo?novelId=123"));
  assertNull(DiscoveryBookIdentity.key("https://android.jjwxc.com.evil.org/androidapi/novelbasicinfo?novelId=123"));
 }
 @Test public void untrustedHostsAndConflictingIdsAreNotAccepted(){
  assertNull(DiscoveryBookIdentity.key("https://app.chnovels.com.evil.org/api/a?novel_id=123"));
  assertNull(DiscoveryBookIdentity.key("https://www.gongzicp.com/webapi/novel/novelInfo?id=1&novel_id=2"));
  assertNull(DiscoveryBookIdentity.key("https://app.chnovels.com/api/a?novel_id=0"));
 }
}
