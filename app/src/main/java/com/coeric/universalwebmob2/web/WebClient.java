package com.coeric.universalwebmob2.web;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class WebClient extends WebViewClient {
    public interface Listener {
        void onPageState(WebView view, String url, String title, boolean loading, boolean error);
    }

    private final Listener listener;

    public WebClient(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Uri uri = request.getUrl();
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
            listener.onPageState(view, request.getUrl().toString(),
                    view.getTitle(), false, true);
        }
    }
}
