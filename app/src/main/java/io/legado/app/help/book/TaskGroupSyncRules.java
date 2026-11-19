package io.legado.app.help.book;
import java.net.URI;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.*;

/** Explicit rotating labels and cached native chapter identities; no network or credential use. */
public final class TaskGroupSyncRules {
 private TaskGroupSyncRules() {}
 public static boolean isTaskGroup(String name){
  if(name==null)return false;
  String value=Normalizer.normalize(name,Normalizer.Form.NFKC).replaceAll("\\s+","");
  return value.matches("\\([123]\\)转|上新[123]");
 }
 public static long workMask(long original,long allTasks,long selected){
  if((selected&~allTasks)!=0)throw new IllegalArgumentException("unknown_task_group");
  return (original&~allTasks)|selected;
 }
 public static String chapterRef(String bookIdentity,String raw){
  if(bookIdentity==null||raw==null||raw.length()>32768)return null;
  try{
   String[] book=bookIdentity.split(":",2);if(book.length!=2||!book[1].matches("[1-9][0-9]*"))return null;
   URI uri=new URI(raw.split(",\\s*\\{",2)[0]);String host=uri.getHost();
   if(host==null||uri.getUserInfo()!=null||!("http".equalsIgnoreCase(uri.getScheme())||"https".equalsIgnoreCase(uri.getScheme())))return null;
   host=host.toLowerCase(Locale.ROOT);String prefix;
   if("jj".equals(book[0])){
    if(!host.matches("(?:[a-z0-9-]+\\.)*jjwxc\\.(?:net|org|com)"))return null;prefix="jjwxc";
   }else if("cp".equals(book[0])){
    if(!Arrays.asList("gongzicp.com","www.gongzicp.com","m.gongzicp.com","webapi.gongzicp.com").contains(host))return null;
    Matcher m=Pattern.compile("^/(?:read|chapter)-([0-9]+)\\.html$").matcher(uri.getPath());
    if(m.matches())return "gongzicp:"+book[1]+":"+canonical(m.group(1));prefix="gongzicp";
   }else if("ch".equals(book[0])){if(!host.equals("app.chnovels.com"))return null;prefix="chnovels";}
   else return null;
   Set<String> chapters=new HashSet<>(),books=new HashSet<>();
   for(String pair:Objects.toString(uri.getRawQuery(),"").split("&")){
    String[] kv=pair.split("=",2);if(kv.length!=2)continue;
    String k=URLDecoder.decode(kv[0],"UTF-8").toLowerCase(Locale.ROOT),v=URLDecoder.decode(kv[1],"UTF-8");
    if(Arrays.asList("novelid","novel_id","bookid","book_id","nid").contains(k))books.add(canonical(v));
    if(Arrays.asList("chapterid","chapter_id","cid").contains(k))chapters.add(canonical(v));
   }
   if(books.size()>1||(!books.isEmpty()&&!books.contains(book[1]))||chapters.size()!=1)return null;
   // A JJ chapter must carry its novel ID; the cached FK supplies context for CP/CH.
   if("jj".equals(book[0])&&books.isEmpty())return null;
   return prefix+":"+book[1]+":"+chapters.iterator().next();
  }catch(Exception ignored){return null;}
 }
 private static String canonical(String value){if(!value.matches("[0-9]{1,30}"))throw new IllegalArgumentException();String v=value.replaceFirst("^0+(?!$)","");if("0".equals(v))throw new IllegalArgumentException();return v;}
}
