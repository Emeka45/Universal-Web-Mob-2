package com.coeric.universalwebmob2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.coeric.universalwebmob2.web.MainActivityBridge;
import com.coeric.universalwebmob2.web.StartPages;
import com.coeric.universalwebmob2.web.WebChromeClient;
import com.coeric.universalwebmob2.web.WebClient;
import com.coeric.universalwebmob2.web.WebDownloadHandler;
import com.coeric.universalwebmob2.web.WebIntentHandler;
import com.coeric.universalwebmob2.web.WebTab;
import com.coeric.universalwebmob2.web.WebTabManager;

import java.util.List;

public class MainActivity extends Activity implements WebTabManager.Listener, WebClient.Listener {
    private LinearLayout root;
    private LinearLayout tabStrip;
    private android.widget.FrameLayout webContainer;
    private EditText address;
    private ProgressBar progress;
    private WebTabManager tabManager;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        tabManager = new WebTabManager(this, webContainer, this);
        tabManager.createTab(StartPages.HOME);
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(4), dp(4), dp(4), dp(2));

        addNavButton(nav, "‹", v -> goBack());
        addNavButton(nav, "›", v -> goForward());
        addNavButton(nav, "↻", v -> reload());
        addNavButton(nav, "⌂", v -> home());

        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("Search or enter web address");
        address.setTextSize(14);
        address.setPadding(dp(10), 0, dp(10), 0);
        address.setOnEditorActionListener((v, actionId, event) -> {
            navigate(address.getText().toString());
            return true;
        });
        nav.addView(address, new LinearLayout.LayoutParams(0, dp(46), 1));

        addNavButton(nav, "⋮", this::showMenu);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setVisibility(View.GONE);

        HorizontalScrollView tabScroller = new HorizontalScrollView(this);
        tabScroller.setHorizontalScrollBarEnabled(false);
        tabStrip = new LinearLayout(this);
        tabStrip.setGravity(Gravity.CENTER_VERTICAL);
        tabScroller.addView(tabStrip, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)));

        webContainer = new android.widget.FrameLayout(this);

        root.addView(nav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        root.addView(progress, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));
        root.addView(tabScroller, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        root.addView(webContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);
    }

    private void addNavButton(LinearLayout parent, String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(18);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(6), 0, dp(6), 0);
        b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(dp(48), dp(46)));
    }

    private void configureWebView(WebView view) {
        view.setWebViewClient(new WebClient(this));
        view.setWebChromeClient(new WebChromeClient(this));
        view.setDownloadListener(new WebDownloadHandler(this));
    }

    @Override
    public void onActiveTabChanged(WebTab tab) {
        configureWebView(tab.webView);
        address.setText(displayUrl(tab.webView.getUrl()));
        updateButtons();
    }

    @Override
    public void onTabsChanged(List<WebTab> tabs) {
        tabStrip.removeAllViews();
        for (WebTab tab : tabs) {
            Button b = new Button(this);
            b.setText(tab.title);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setOnClickListener(v -> tabManager.selectTab(tab.id));
            tabStrip.addView(b, new LinearLayout.LayoutParams(dp(150), dp(42)));
        }
    }

    @Override
    public void onPageState(WebView view, String url, String title, boolean loading, boolean error) {
        WebTab active = tabManager.getActive();
        if (active == null || active.webView != view) return;
        active.title = (title == null || title.trim().isEmpty()) ? "New Tab" : title;
        address.setText(displayUrl(url));
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        progress.setProgress(loading ? 60 : 100);
        if (error) Toast.makeText(this, "Page failed to load. Use ↻ to retry.", Toast.LENGTH_SHORT).show();
        onTabsChanged(tabManager.getTabs());
    }

    private String displayUrl(String url) {
        if (url == null || StartPages.HOME.equals(url)) return "";
        return url;
    }

    private void navigate(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return;
        if (!value.contains("://") && value.contains(".")) value = "https://" + value;
        else if (!value.contains("://")) value = "https://www.google.com/search?q=" + Uri.encode(value);
        WebTab tab = tabManager.getActive();
        if (tab != null) tab.webView.loadUrl(value);
    }

    private void home() {
        WebTab tab = tabManager.getActive();
        if (tab != null) loadHome(tab.webView);
    }

    private void loadHome(WebView view) {
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>" +
                "<style>body{font-family:sans-serif;margin:24px;line-height:1.5}a{display:block;padding:16px;margin:12px 0;border-radius:12px;background:#f1edff;color:#342070;text-decoration:none;font-size:18px}</style></head>" +
                "<body><h1>Universal Web-Mob 2</h1><p>PC-style web workspace for your phone.</p>" +
                "<a href='" + StartPages.CHATGPT + "'>Open ChatGPT</a>" +
                "<a href='" + StartPages.CLOUDFLARE + "'>Open Cloudflare</a>" +
                "<a href='" + StartPages.GITHUB + "'>Open GitHub</a></body></html>";
        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }

    private void goBack() {
        WebTab t = tabManager.getActive();
        if (t != null && t.webView.canGoBack()) t.webView.goBack();
    }

    private void goForward() {
        WebTab t = tabManager.getActive();
        if (t != null && t.webView.canGoForward()) t.webView.goForward();
    }

    private void reload() {
        WebTab t = tabManager.getActive();
        if (t != null) t.webView.reload();
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add("New tab");
        menu.getMenu().add("Close tab");
        menu.getMenu().add("Home");
        menu.getMenu().add("Retry");
        menu.getMenu().add("Open externally");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();
            if ("New tab".equals(s)) tabManager.createTab(StartPages.HOME);
            else if ("Close tab".equals(s)) {
                WebTab t = tabManager.getActive();
                if (t != null) tabManager.closeTab(t.id);
            } else if ("Home".equals(s)) home();
            else if ("Retry".equals(s)) reload();
            else if ("Open externally".equals(s)) {
                WebTab t = tabManager.getActive();
                if (t != null && t.webView.getUrl() != null)
                    WebIntentHandler.openExternal(this, Uri.parse(t.webView.getUrl()));
            }
            return true;
        });
        menu.show();
    }

    private void updateButtons() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 701) return;
        if (resultCode == RESULT_OK && data != null) {
            MainActivityBridge.deliver(new Uri[]{data.getData()});
        } else {
            MainActivityBridge.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        WebTab t = tabManager == null ? null : tabManager.getActive();
        if (t != null && t.webView.canGoBack()) t.webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (tabManager != null) tabManager.destroyAll();
        super.onDestroy();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
