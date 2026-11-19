package io.legado.app.help.book;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.*;

/** Trusted original-site identity for create-only bridge requests. */
public final class DiscoveryBookIdentity {
 private DiscoveryBookIdentity() {}
 public static String key(String raw) {
  String jj=JinjiangShelfIdentity.key(raw);
  if(jj!=null)return "jj:"+jj.substring(jj.lastIndexOf(':')+1);
  if(raw==null||raw.length()>32768)return null;
  try{
   String url=raw.split(",\\s*\\{",2)[0];URI u=new URI(url);String host=u.getHost();
   if(host==null||u.getUserInfo()!=null||!("http".equalsIgnoreCase(u.getScheme())||"https".equalsIgnoreCase(u.getScheme())))return null;
   host=host.toLowerCase(Locale.ROOT);String site;Set<String> names;
   if(Arrays.asList("www.gongzicp.com","m.gongzicp.com","gongzicp.com","webapi.gongzicp.com").contains(host)){
    Matcher m=Pattern.compile("^/novel-([0-9]+)\\.html$").matcher(u.getPath());
    if(m.matches())return canonical("cp",m.group(1));
    if(!(u.getPath().startsWith("/webapi/novel/")||u.getPath().startsWith("/novel/")))return null;
    site="cp";names=new HashSet<>(Arrays.asList("id","novel_id"));
   }else if("app.chnovels.com".equals(host)&&u.getPath().startsWith("/api/")){
    site="ch";names=new HashSet<>(Arrays.asList("novel_id","book_id","novelid","bookid","id"));
   }else return null;
   Set<String> ids=new HashSet<>();
   for(String pair:Objects.toString(u.getRawQuery(),"").split("&")){
    String[] kv=pair.split("=",2);
    if(kv.length==2&&names.contains(URLDecoder.decode(kv[0],"UTF-8").toLowerCase(Locale.ROOT)))ids.add(URLDecoder.decode(kv[1],"UTF-8"));
   }
   return ids.size()==1?canonical(site,ids.iterator().next()):null;
  }catch(Exception ignored){return null;}
 }
 private static String canonical(String site,String id){
  if(!id.matches("[0-9]{1,30}"))return null;
  id=id.replaceFirst("^0+(?!$)","");return "0".equals(id)?null:site+":"+id;
 }
}
