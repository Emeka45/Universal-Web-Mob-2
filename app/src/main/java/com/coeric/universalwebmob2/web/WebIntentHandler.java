package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabsIntent;

public final class WebIntentHandler {
    private WebIntentHandler() {}

    public static boolean openExternal(Context context, Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Google OAuth and Sign in with Google must not run inside an Android WebView.
     * Use the user's secure browser/Custom Tab for the authentication step.
     */
    public static boolean openGoogleAuthentication(Context context, Uri uri) {
        try {
            CustomTabsIntent customTabs = new CustomTabsIntent.Builder().build();
            customTabs.launchUrl(context, uri);
            return true;
        } catch (Exception ignored) {
            return openExternal(context, uri);
        }
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
