package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WebSettingsFactory {
    private static final Pattern CHROME_VERSION =
            Pattern.compile("Chrome/([0-9.]+)", Pattern.CASE_INSENSITIVE);

    private WebSettingsFactory() {}

    public static void configure(WebView webView, Context context) {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSafeBrowsingEnabled(true);

        // Start from the real Android WebView identity. A single hard-coded desktop
        // UA is deliberately avoided because UA, viewport and WebView capabilities
        // are separate compatibility signals.
        String nativeUa = s.getUserAgentString();
        s.setUserAgentString(nativeUa);

        applyProfile(webView, CompatibilityProfile.UNIVERSAL);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);
    }

    public static void applyProfile(WebView webView, CompatibilityProfile profile) {
        WebSettings s = webView.getSettings();
        String nativeUa = webView.getTag(com.coeric.universalwebmob2.R.id.native_ua_tag) instanceof String
                ? (String) webView.getTag(com.coeric.universalwebmob2.R.id.native_ua_tag)
                : s.getUserAgentString();

        if (webView.getTag(com.coeric.universalwebmob2.R.id.native_ua_tag) == null) {
            webView.setTag(com.coeric.universalwebmob2.R.id.native_ua_tag, nativeUa);
        }

        if (profile == CompatibilityProfile.DESKTOP) {
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(false);
            s.setInitialScale(100);
            s.setTextZoom(100);
            s.setUserAgentString(toDesktopUa(nativeUa));
        } else {
            s.setUseWideViewPort(true);
            s.setLoadWithOverviewMode(true);
            s.setInitialScale(0);
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
