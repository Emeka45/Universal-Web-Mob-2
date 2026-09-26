package com.coeric.universalwebmob2.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class WebClient extends WebViewClient {
    public interface Listener {
        void onPageState(WebView view, String url, String title, boolean loading, boolean error);\n        void onDesktopCompatibilityHint(WebView view);
    }

    private final Listener listener;

    public WebClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();

        // Google explicitly blocks OAuth authorization inside embedded WebViews.
        // Hand the authentication request to a secure browser/Custom Tab instead.
        if (request.isForMainFrame() && WebIntentHandler.isGoogleAuthenticationUrl(uri)) {
            WebIntentHandler.openGoogleAuthentication(view.getContext(), uri);
            return true;
        }

        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return false;
        }

        WebIntentHandler.openExternal(view.getContext(), uri);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        listener.onPageState(view, url, view.getTitle(), true, false);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        listener.onPageState(view, url, view.getTitle(), false, false);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request,
                                WebResourceError error) {
        if (request.isForMainFrame()) {
            Uri uri = request.getUrl();
            String description = error == null ? "" : String.valueOf(error.getDescription());

            // Some Google authentication failures arrive as an error page instead of
            // a navigational callback. Give the user the secure-browser route.
            if (WebIntentHandler.isGoogleAuthenticationUrl(uri)
                    || description.toLowerCase().contains("disallowed_useragent")) {
                WebIntentHandler.openGoogleAuthentication(view.getContext(), uri);
                return;
            }

            listener.onPageState(view, request.getUrl().toString(),
                    view.getTitle(), false, true);
        }
    }
}
