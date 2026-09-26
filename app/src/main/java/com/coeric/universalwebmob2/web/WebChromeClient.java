package com.coeric.universalwebmob2.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public final class WebChromeClient extends android.webkit.WebChromeClient {
    private final Activity activity;
    private final WebTabManager tabManager;

    public WebChromeClient(Activity activity, WebTabManager tabManager) {
        this.activity = activity;
        this.tabManager = tabManager;
    }

    @Override
    public boolean onCreateWindow(WebView source, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebTab tab = tabManager.createTab(StartPages.HOME);
        WebView child = tab.webView;

        child.setWebViewClient(new WebClient(tabManagerListener()));
        child.setWebChromeClient(new WebChromeClient(activity, tabManager));

        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(child);
        resultMsg.sendToTarget();
        return true;
    }

    private WebClient.Listener tabManagerListener() {
        if (activity instanceof WebClient.Listener) {
            return (WebClient.Listener) activity;
        }
        return new WebClient.Listener() {
            @Override public void onPageState(WebView view, String url, String title, boolean loading, boolean error) {}
            @Override public void onDesktopCompatibilityHint(WebView view) {}
            @Override public void onExternalNavigationUnavailable(WebView view, String url) {}
        };
    }

    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                     FileChooserParams params) {
        Intent intent = params.createIntent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            MainActivityBridge.setPendingFileCallback(callback);
            activity.startActivityForResult(intent, 701);
            return true;
        } catch (Exception e) {
            MainActivityBridge.cancel();
            return false;
        }
    }
}
