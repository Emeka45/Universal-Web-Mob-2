package com.coeric.universalwebmob2.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class WebClient extends WebViewClient {
    public interface Listener {
        void onPageState(WebView view, String url, String title, boolean loading, boolean error);
        void onDesktopCompatibilityHint(WebView view);
        void onExternalNavigationUnavailable(WebView view, String url);
    }

    private final Listener listener;

    public WebClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (request == null) return false;

        Uri uri = request.getUrl();
        String scheme = uri == null || uri.getScheme() == null
                ? "" : uri.getScheme().toLowerCase();

        // HTTP(S) belongs to WebView. Returning false is important: it lets
        // WebView continue the navigation instead of cancelling/restarting it.
        if ("http".equals(scheme) || "https".equals(scheme)) {
            if (request.isForMainFrame() && WebIntentHandler.isGoogleAuthenticationUrl(uri)) {
                return WebIntentHandler.openGoogleAuthentication(view.getContext(), uri);
            }
            return false;
        }

        if ("intent".equals(scheme)) {
            boolean handled = WebIntentHandler.openIntentUrl(view.getContext(), uri.toString());
            if (!handled) listener.onExternalNavigationUnavailable(view, uri.toString());
            return true;
        }

        // Custom app/deep links are handed off only when WebView cannot handle
        // the scheme. If no application exists, keep the current page alive and
        // surface the real fallback UI instead of navigating to a dead page.
        boolean handled = WebIntentHandler.openExternal(view.getContext(), uri);
        if (!handled) listener.onExternalNavigationUnavailable(
                view, uri == null ? "" : uri.toString());
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
        if (!request.isForMainFrame()) return;

        Uri uri = request.getUrl();
        String description = error == null ? "" : String.valueOf(error.getDescription());

        if (WebIntentHandler.isGoogleAuthenticationUrl(uri)
                || description.toLowerCase().contains("disallowed_useragent")) {
            WebIntentHandler.openGoogleAuthentication(view.getContext(), uri);
            return;
        }

        listener.onPageState(view,
                uri == null ? "" : uri.toString(),
                view.getTitle(), false, true);
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request,
                                    android.webkit.WebResourceResponse errorResponse) {
        if (request.isForMainFrame()) {
            // HTTP errors are real server responses, not WebView navigation failures.
            // Keep the page in place so the website's own error page remains visible.
            listener.onPageState(view, request.getUrl().toString(),
                    view.getTitle(), false, true);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler,
                                   SslError error) {
        // Never bypass certificate errors. This avoids turning "connection
        // recovery" into a security vulnerability.
        handler.cancel();
        listener.onPageState(view,
                view.getUrl() == null ? "" : view.getUrl(),
                view.getTitle(), false, true);
    }
}
