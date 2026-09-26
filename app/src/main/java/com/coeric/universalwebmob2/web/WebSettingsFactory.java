package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebSettingsFactory {
    private static final Pattern CHROME_VERSION =
            Pattern.compile("Chrome/([0-9.]+)", Pattern.CASE_INSENSITIVE);
    private static final WeakHashMap<WebView, String> NATIVE_UAS = new WeakHashMap<>();

    private WebSettingsFactory() {}

    public static void configure(WebView webView, Context context) {
        WebSettings s = webView.getSettings();

        // Core web compatibility.
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);

        // Allow normal user content and downloads without granting file://
        // pages unrestricted access to the local filesystem.
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);

        // Keep media behavior predictable; websites can still request playback
        // through normal WebView mechanisms.
        s.setMediaPlaybackRequiresUserGesture(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Some otherwise-HTTPS sites still contain HTTP assets. Compatibility
            // mode permits those assets without enabling unrestricted mixed content.
            s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            s.setSafeBrowsingEnabled(true);
        }

        // Do not let WebView cache failures indefinitely. Normal HTTP caching is
        // still available, while reloads can recover from stale network state.
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        String nativeUa = s.getUserAgentString();
        NATIVE_UAS.put(webView, nativeUa);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
        cookies.flush();
    }

    public static void applyProfile(WebView webView, CompatibilityProfile profile) {
        WebSettings s = webView.getSettings();
        String nativeUa = NATIVE_UAS.get(webView);
        if (nativeUa == null) {
            nativeUa = s.getUserAgentString();
            NATIVE_UAS.put(webView, nativeUa);
        }

        if (profile == CompatibilityProfile.DESKTOP) {
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(false);
            webView.setInitialScale(100);
            s.setTextZoom(100);
            s.setUserAgentString(toDesktopUa(nativeUa));
        } else {
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
            webView.setInitialScale(0);
            s.setTextZoom(100);
            s.setUserAgentString(nativeUa);
        }
    }

    private static String toDesktopUa(String nativeUa) {
        String chromeVersion = "140.0.0.0";
        Matcher matcher = CHROME_VERSION.matcher(nativeUa == null ? "" : nativeUa);
        if (matcher.find()) chromeVersion = matcher.group(1);

        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/" + chromeVersion + " Safari/537.36";
    }
}
