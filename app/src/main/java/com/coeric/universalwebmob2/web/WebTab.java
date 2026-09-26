package com.coeric.universalwebmob2.web;

import android.webkit.WebView;

public final class WebTab {
    public final int id;
    public final WebView webView;
    public String title = "New Tab";\n    public CompatibilityProfile compatibilityProfile = CompatibilityProfile.UNIVERSAL;\n    public boolean desktopRetryAttempted = false;

    public WebTab(int id, WebView webView) {
        this.id = id;
        this.webView = webView;
    }
}
