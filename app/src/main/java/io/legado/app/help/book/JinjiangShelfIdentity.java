package io.legado.app.help.book;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extra identity for shelf indicators only. Never changes stored book data. */
public final class JinjiangShelfIdentity {
    private static final Pattern OPTIONS = Pattern.compile(",\\s*\\{");
    private static final Pattern BRIDGE = Pattern.compile("^/truyen/jjwxc/[0-9]+/([0-9]+)/?$", Pattern.CASE_INSENSITIVE);

    private JinjiangShelfIdentity() {}

    public static void addKeys(Collection<String> keys, String name, String author, String bookUrl) {
        keys.add(name + "-" + author);
        keys.add(name);
        keys.add(bookUrl);
        String id = key(bookUrl);
        if (id != null) keys.add(id);
    }

    public static boolean contains(Set<String> shelf, String name, String author, String bookUrl) {
        String legacyKey = isBlank(author) ? name : name + "-" + author;
        String id = key(bookUrl);
        return shelf.contains(legacyKey) || shelf.contains(bookUrl) ||
                (id != null && shelf.contains(id));
    }

    public static String key(String bookUrl) {
        if (bookUrl == null || bookUrl.length() > 8192) return null;
        try {
            String url = bookUrl.trim();
            Matcher options = OPTIONS.matcher(url);
            if (options.find()) url = url.substring(0, options.start());
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) return null;
            String host = uri.getHost();
            if (host == null || uri.getRawUserInfo() != null) return null;
            host = host.toLowerCase(Locale.ROOT);
            String path = uri.getPath();
            if (path == null) return null;
            String id = null;
            if (host.equals("jjwxc.net") || host.endsWith(".jjwxc.net") ||
                    host.equals("jjwxc.org") || host.endsWith(".jjwxc.org") ||
                    host.equals("jjwxc.com") || host.endsWith(".jjwxc.com")) {
                if (!path.equalsIgnoreCase("/onebook.php") &&
                        !path.equalsIgnoreCase("/androidapi/novelbasicinfo")) return null;
                String query = uri.getRawQuery();
                if (query == null) return null;
                for (String part : query.split("&")) {
                    int equals = part.indexOf('=');
                    if (equals < 0) continue;
                    String name = URLDecoder.decode(part.substring(0, equals), "UTF-8");
                    if (!name.equalsIgnoreCase("novelid")) continue;
                    String value = normalize(URLDecoder.decode(part.substring(equals + 1), "UTF-8"));
                    if (value == null || (id != null && !id.equals(value))) return null;
                    id = value;
                }
            } else if (host.equals("sangtacviet.app") || host.equals("sangtacviet.com")) {
                Matcher bridge = BRIDGE.matcher(path);
                if (!bridge.matches()) return null;
                id = normalize(bridge.group(1));
            }
            return id == null ? null : "jjwxc:novel:" + id;
        } catch (Exception ignored) {
            // Bad or unrelated source URLs must not break a discovery/search result list.
            return null;
        }
    }

    private static boolean isBlank(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isWhitespace(c) && !Character.isSpaceChar(c)) return false;
        }
        return true;
    }

    private static String normalize(String id) {
        if (id.isEmpty() || id.length() > 20) return null;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (c < '0' || c > '9') return null;
        }
        int first = 0;
        while (first < id.length() - 1 && id.charAt(first) == '0') first++;
        id = id.substring(first);
        return id.equals("0") ? null : id;
    }
}
