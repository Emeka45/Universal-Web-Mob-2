package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

public final class WebTabManager {
    public interface Listener {
        void onActiveTabChanged(WebTab tab);
        void onTabsChanged(List<WebTab> tabs);
    }

    private final Context context;
    private final ViewGroup webContainer;
    private final Listener listener;
    private final List<WebTab> tabs = new ArrayList<>();
    private int nextId = 1;
    private WebTab active;

    public WebTabManager(Context context, ViewGroup webContainer, Listener listener) {
        this.context = context;
        this.webContainer = webContainer;
        this.listener = listener;
    }

    public WebTab createTab(String url) {
        WebView webView = new WebView(context);
        WebSettingsFactory.configure(webView, context);
        WebTab tab = new WebTab(nextId++, webView);
        tabs.add(tab);
        selectTab(tab.id);
        // HOME is an app-owned start page rendered by MainActivity; it is not a real URL.
        if (url != null && !StartPages.HOME.equals(url)) webView.loadUrl(url);
        return tab;
    }

    public void applyCompatibility(WebTab tab) {\n        if (tab == null) return;\n        WebSettingsFactory.applyProfile(tab.webView, tab.compatibilityProfile);\n    }\n\n    public void selectTab(int id) {
        WebTab found = null;
        for (WebTab tab : tabs) {
            if (tab.id == id) {
                found = tab;
                break;
            }
        }
        if (found == null) return;
        active = found;
        webContainer.removeAllViews();
        if (found.webView.getParent() != null) {
            ((ViewGroup) found.webView.getParent()).removeView(found.webView);
        }
        webContainer.addView(found.webView,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        listener.onActiveTabChanged(found);
        listener.onTabsChanged(new ArrayList<>(tabs));
    }

    public void closeTab(int id) {
        int index = -1;
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).id == id) {
                index = i;
                break;
            }
        }
        if (index < 0) return;

        WebTab removed = tabs.remove(index);
        removed.webView.stopLoading();
        removed.webView.clearHistory();
        removed.webView.destroy();

        if (tabs.isEmpty()) {
            createTab(StartPages.HOME);
            return;
        }

        if (removed == active) {
            int nextIndex = Math.min(index, tabs.size() - 1);
            selectTab(tabs.get(nextIndex).id);
        } else {
            listener.onTabsChanged(new ArrayList<>(tabs));
        }
    }

    public WebTab getActive() {
        return active;
    }

    public List<WebTab> getTabs() {
        return new ArrayList<>(tabs);
    }

    public void destroyAll() {
        for (WebTab tab : tabs) {
            tab.webView.stopLoading();
            tab.webView.destroy();
        }
        tabs.clear();
        active = null;
    }
}
