package io.legado.app.help.book;

import java.util.regex.Pattern;

/** Presentation only: never changes a chapter's stored title or purchase flags. */
public final class ChapterPurchaseMarker {
    private static final Pattern PREFIX = Pattern.compile("^(?:(?:🥚|🐣|🔒|🔓)\\uFE0F?\\s*)+");
    private ChapterPurchaseMarker() {}

    public static String marker(boolean isVip, boolean isPay, boolean isVolume) {
        if (!isVip || isVolume) return "";
        return isPay ? "🐣" : "🥚";
    }

    public static String title(String title, boolean isVip, boolean isPay, boolean isVolume) {
        String marker = marker(isVip, isPay, isVolume);
        if (marker.isEmpty() || title == null || title.trim().isEmpty()) return title;
        String body = PREFIX.matcher(title.trim()).replaceFirst("");
        return body.isEmpty() ? title : marker + " " + body;
    }
}
