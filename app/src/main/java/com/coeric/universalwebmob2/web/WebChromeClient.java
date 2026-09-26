package com.coeric.universalwebmob2.web;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebView;

public final class WebChromeClient extends android.webkit.WebChromeClient {
    private final Activity activity;

    public WebChromeClient(Activity activity) {
        this.activity = activity;
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
