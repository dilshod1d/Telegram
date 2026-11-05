import org.telegram.messenger.LocalSafety;

public final class LocalSafetyTest {
    private static int checks;

    public static void main(String[] args) {
        for (String url : new String[] {
                "https://example.com/path?email=user@example.org", "http://192.0.2.1/",
                "https://t.me/example", "https://bit.ly/example", "https://пример.com",
                "https://日本語.jp", "https://école.fr", "mailto:user@example.com",
                "tg://resolve?domain=example", "not a URL"
        }) {
            check(LocalSafety.linkRisk(url) == 0, url);
        }
        check(LocalSafety.linkRisk("https://trusted.example@destination.example") == 1, "login");
        check(LocalSafety.linkRisk("https://user:password@example.com") == 1, "password");
        check(LocalSafety.linkRisk("https://example.com/\u202Etxt") == 2, "direction");
        check(LocalSafety.linkRisk("https://example.com/%E2%80%AEtxt") == 2, "encoded direction");
        check(LocalSafety.linkRisk("https://pаypal.example") == 3, "mixed script");
        check(LocalSafety.linkRisk("pаypal.example") == 3, "scheme-less mixed script");
        check(LocalSafety.linkRisk("https://pаypal.example/a b") == 3, "space in path");
        check(LocalSafety.linkRisk("https://xn--pypal-4ve.example") == 3, "punycode");
        check(LocalSafety.destination("https://trusted.example@destination.example/path").equals("destination.example"), "destination");
        check(LocalSafety.isApk("APP.APK", "text/plain"), "APK extension");
        check(LocalSafety.isApk("invoice.pdf", "application/vnd.android.package-archive; x=1"), "APK MIME");
        check(!LocalSafety.isApk("notes.apk.txt", "text/plain"), "text");
        check(!LocalSafety.isApk("archive.zip", "application/zip"), "archive");
        System.out.println("Local safety checks passed: " + checks);
    }

    private static void check(boolean passed, String name) {
        if (!passed) throw new AssertionError(name);
        checks++;
    }
}
