package io.legado.app.help.book;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Pure presentation/history logic. Stored source intros never contain our prefix. */
public final class JinjiangTitleHistory {
    private JinjiangTitleHistory() {}

    public static List<String> observe(List<String> seen, String shelfTitle, String currentTitle) {
        if (currentTitle == null || currentTitle.trim().isEmpty()) return new ArrayList<>(seen);
        LinkedHashSet<String> titles = new LinkedHashSet<>();
        for (String title : seen) add(titles, title);
        // Seed the shelf's existing title only once; later manual edits aren't new observations.
        if (titles.isEmpty()) add(titles, shelfTitle);
        add(titles, currentTitle);
        return new ArrayList<>(titles);
    }

    public static String decorate(List<String> seen, String currentTitle, String intro) {
        LinkedHashSet<String> former = new LinkedHashSet<>();
        for (String title : seen) {
            if (title != null && !title.trim().equals(currentTitle)) add(former, title);
        }
        if (former.isEmpty()) return intro;
        String prefix = "曾用名：" + String.join("／", former);
        String body = intro == null ? "" : intro;
        if (body.startsWith("<usehtml>") || body.startsWith("<useweb>")) {
            int end = body.indexOf('>') + 1;
            return body.substring(0, end) + "<p>" + escapeHtml(prefix) + "</p>" + body.substring(end);
        }
        if (body.startsWith("<md>")) {
            StringBuilder escaped = new StringBuilder();
            for (char c : prefix.toCharArray()) {
                if ("\\`*_{}[]<>()#+-.!|>".indexOf(c) >= 0) escaped.append('\\');
                escaped.append(c);
            }
            return "<md>" + escaped + "\n\n" + body.substring(4);
        }
        return prefix + "\n\n" + body;
    }

    private static void add(LinkedHashSet<String> titles, String title) {
        if (title != null && !title.trim().isEmpty()) titles.add(title.trim().replaceAll("[\\r\\n]+", " "));
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
