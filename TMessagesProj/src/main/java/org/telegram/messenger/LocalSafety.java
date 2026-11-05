/*
 * This is the source code of Telegram for Android.
 * It is licensed under GNU GPL v. 2 or later (see LICENSE).
 */
package org.telegram.messenger;

import java.net.IDN;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Pattern;

public final class LocalSafety {
    private static final Pattern LATIN = Pattern.compile("\\p{IsLatin}");
    private static final Pattern CYRILLIC = Pattern.compile("\\p{IsCyrillic}");
    private static final Pattern GREEK = Pattern.compile("\\p{IsGreek}");
    private static final Pattern DIRECTION = Pattern.compile("[\u202A-\u202E\u2066-\u2069]");

    private LocalSafety() {
    }

    public static boolean isApk(String fileName, String mimeType) {
        return fileName != null && fileName.trim().toLowerCase(Locale.ROOT).endsWith(".apk")
                || mimeType != null && mimeType.split(";", 2)[0].trim().equalsIgnoreCase("application/vnd.android.package-archive");
    }

    public static int linkRisk(String url) {
        try {
            URI parsed = parse(url);
            if (!"http".equalsIgnoreCase(parsed.getScheme()) && !"https".equalsIgnoreCase(parsed.getScheme())) {
                return 0;
            }
            String authority = parsed.getRawAuthority();
            if (authority == null) return 0;
            if (authority.contains("@")) return 1;
            if (DIRECTION.matcher(decode(url)).find()) return 2;
            String host = IDN.toUnicode(decode(authority.split(":", 2)[0]));
            for (String label : host.split("\\.")) {
                int scripts = (LATIN.matcher(label).find() ? 1 : 0)
                        + (CYRILLIC.matcher(label).find() ? 1 : 0)
                        + (GREEK.matcher(label).find() ? 1 : 0);
                if (scripts > 1) return 3;
            }
        } catch (Exception ignore) {
        }
        return 0;
    }

    public static String destination(String url) {
        try {
            String authority = parse(url).getRawAuthority();
            if (authority == null) return "";
            String host = authority.substring(authority.lastIndexOf('@') + 1);
            if (host.startsWith("[")) return host;
            return IDN.toASCII(decode(host.split(":", 2)[0]));
        } catch (Exception ignore) {
            return "";
        }
    }

    public static String displayUrl(String url) {
        try {
            return parse(url).toASCIIString();
        } catch (Exception ignore) {
            return url.replaceAll("[\u202A-\u202E\u2066-\u2069]", "");
        }
    }

    private static URI parse(String value) throws Exception {
        URI parsed = new URI(value.replace(" ", "%20"));
        return parsed.getScheme() == null ? new URI("https://" + value.replace(" ", "%20")) : parsed;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value.replace("+", "%2B"), "UTF-8");
        } catch (Exception ignore) {
            return value;
        }
    }
}
