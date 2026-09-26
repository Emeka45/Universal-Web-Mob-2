package com.coeric.universalwebmob2.web;

import java.util.Locale;
import java.util.regex.Pattern;

public final class CompatibilityDetector {
    private static final Pattern DESKTOP_ONLY = Pattern.compile(
            "(desktop\\s+only|desktop\\s+browser|desktop\\s+version|open\\s+in\\s+(the\\s+)?desktop\\s+app|open\\s+in\\s+(the\\s+)?desktop\\s+browser|desktop\\s+app\\s+required|desktop\\s+experience|desktop\\s+site|not\\s+supported\\s+on\\s+(mobile|phone)|unsupported\\s+on\\s+(mobile|phone)|use\\s+a\\s+desktop|please\\s+use\\s+a\\s+computer|computer\\s+required|mobile\\s+device\\s+not\\s+supported)",
            Pattern.CASE_INSENSITIVE);

    private CompatibilityDetector() {}

    public static boolean requiresDesktopCompatibility(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String normalized = text.toLowerCase(Locale.US)
                .replace('\\u00A0', ' ')
                .replaceAll("\\s+", " ");
        return DESKTOP_ONLY.matcher(normalized).find();
    }
}
