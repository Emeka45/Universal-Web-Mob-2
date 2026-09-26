package com.coeric.universalwebmob2.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class WebClient extends WebViewClient {
    public interface Listener {
        void onPageState(WebView view, String url, String title, boolean loading, boolean error);
        void onDesktopCompatibilityHint(WebView view);
    }

    private final Listener listener;

    public WebClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();

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

        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            view.evaluateJavascript(
                    "(function(){return document.body ? document.body.innerText : '';})()",
                    value -> {
                        if (value == null) return;
                        String text = value;
                        if (text.length() > 30000) text = text.substring(0, 30000);
                        if (CompatibilityDetector.requiresDesktopCompatibility(text)) {
                            listener.onDesktopCompatibilityHint(view);
                        }
                    });
        }
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request,
                                WebResourceError error) {
        if (request.isForMainFrame()) {
            Uri uri = request.getUrl();
            String description = error == null ? "" : String.valueOf(error.getDescription());

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
