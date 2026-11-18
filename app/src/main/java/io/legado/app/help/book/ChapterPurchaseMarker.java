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

    /** Normalize only explicit source status; never infer a purchase from an unknown title. */
    public static String normalizeLegacyTitle(String value) {
        if (value == null) return null;
        String text = value.trim();
        if (text.startsWith("🔒")) return title(text, true, false, false);
        if (text.startsWith("🔓")) return title(text, true, true, false);
        return value;
    }

    /** A book-info refresh has no purchase flags; retain known status only for the identical title. */
    public static String mergeLatestTitle(String incoming, String previous) {
        String normalized = normalizeLegacyTitle(incoming);
        if (normalized == null || previous == null) return normalized;
        String fresh = normalized.trim();
        String old = normalizeLegacyTitle(previous).trim();
        if (PREFIX.matcher(fresh).find()) return normalized;
        if (PREFIX.matcher(old).find() && fresh.equals(PREFIX.matcher(old).replaceFirst(""))) return old;
        return normalized;
    }

    public static String title(String title, boolean isVip, boolean isPay, boolean isVolume) {
        String marker = marker(isVip, isPay, isVolume);
        if (marker.isEmpty() || title == null || title.trim().isEmpty()) return title;
        String body = PREFIX.matcher(title.trim()).replaceFirst("");
        return body.isEmpty() ? title : marker + " " + body;
    }
}
