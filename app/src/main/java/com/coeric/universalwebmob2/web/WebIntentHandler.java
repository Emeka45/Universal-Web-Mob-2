package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

public final class WebIntentHandler {
    private WebIntentHandler() {}

    /** Handles normal links, intent:// links and application/deep links without trapping the tab. */
    public static boolean openExternal(Context context, Uri uri) {
        if (uri == null) return false;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            context.startActivity(intent);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /** Parse Android intent:// URLs emitted by web app launch buttons. */
    public static boolean openIntentUrl(Context context, String rawUrl) {
        if (rawUrl == null || !rawUrl.startsWith("intent://")) return false;
        try {
            Intent intent = Intent.parseUri(rawUrl, Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            try {
                context.startActivity(intent);
                return true;
            } catch (Exception ignored) {
                String fallback = intent.getStringExtra("browser_fallback_url");
                if (fallback != null && (fallback.startsWith("https://") || fallback.startsWith("http://"))) {
                    return openWebFallback(context, Uri.parse(fallback));
                }
                return false;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean openWebFallback(Context context, Uri uri) {
        try {
            CustomTabsIntent customTabs = new CustomTabsIntent.Builder().build();
            customTabs.launchUrl(context, uri);
            return true;
        } catch (Exception ignored) {
            return openExternal(context, uri);
        }
    }

    /**
     * Google OAuth and Sign in with Google must not run inside an Android WebView.
     * Use the user's secure browser/Custom Tab for the authentication step.
     */
    public static boolean openGoogleAuthentication(Context context, Uri uri) {
        return openWebFallback(context, uri);
    }

    public static boolean isGoogleAuthenticationUrl(Uri uri) {
        if (uri == null) return false;
        String host = uri.getHost();
        if (host == null) return false;

        host = host.toLowerCase();
        if (!"accounts.google.com".equals(host) && !"accounts.googleusercontent.com".equals(host)) {
            return false;
        }

        String path = uri.getPath();
        if (path == null) path = "";

        return path.startsWith("/o/oauth2")
                || path.startsWith("/signin")
                || path.startsWith("/ServiceLogin")
                || path.startsWith("/v3/signin")
                || path.contains("/challenge/");
    }
}
