package com.coeric.universalwebmob2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.coeric.universalwebmob2.web.CompatibilityProfile;
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
    private static final int FILE_CHOOSER_REQUEST = 701;

    private LinearLayout root;
    private LinearLayout tabStrip;
    private FrameLayout webContainer;
    private EditText address;
    private ProgressBar progress;
    private WebTabManager tabManager;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        tabManager = new WebTabManager(this, webContainer, this);
        WebTab first = tabManager.createTab(StartPages.HOME);
        loadHome(first.webView);
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(6), dp(6), dp(6), dp(4));
        nav.setBackgroundColor(Color.WHITE);

        ImageView logo = new ImageView(this);
        logo.setImageResource(com.coeric.universalwebmob2.R.drawable.ic_logo);
        logo.setPadding(dp(3), dp(3), dp(3), dp(3));
        nav.addView(logo, new LinearLayout.LayoutParams(dp(40), dp(40)));

        addNavButton(nav, "‹", v -> goBack());
        addNavButton(nav, "›", v -> goForward());
        addNavButton(nav, "↻", v -> reload());
        addNavButton(nav, "⌂", v -> home());

        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint(getString(R.string.address_hint));
        address.setTextSize(14);
        address.setTextColor(Color.rgb(35, 32, 45));
        address.setHintTextColor(Color.rgb(125, 120, 135));
        address.setPadding(dp(14), 0, dp(14), 0);
        address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        address.setBackground(roundRect(Color.rgb(247, 245, 251), dp(22), Color.TRANSPARENT));
        address.setOnEditorActionListener((v, actionId, event) -> {
            navigate(address.getText().toString());
            address.clearFocus();
            return true;
        });
        nav.addView(address, new LinearLayout.LayoutParams(0, dp(42), 1));

        addNavButton(nav, "⋮", this::showMenu);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setVisibility(View.GONE);

        HorizontalScrollView tabScroller = new HorizontalScrollView(this);
        tabScroller.setHorizontalScrollBarEnabled(false);
        tabScroller.setFillViewport(false);
        tabStrip = new LinearLayout(this);
        tabStrip.setGravity(Gravity.CENTER_VERTICAL);
        tabStrip.setPadding(dp(6), dp(2), dp(6), dp(4));
        tabScroller.addView(tabStrip, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)));

        webContainer = new FrameLayout(this);
        webContainer.setBackgroundColor(Color.WHITE);

        root.addView(nav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));
        root.addView(progress, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(2)));
        root.addView(tabScroller, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44)));
        root.addView(webContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);
    }

    private void addNavButton(LinearLayout parent, String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(19);
        b.setTextColor(Color.rgb(70, 64, 82));
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(0, 0, 0, 0);
        b.setGravity(Gravity.CENTER);
        b.setBackground(roundRect(Color.WHITE, dp(20), Color.TRANSPARENT));
        b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(dp(38), dp(42)));
    }

    private GradientDrawable roundRect(int fill, int radius, int stroke) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(radius);
        if (stroke != Color.TRANSPARENT) drawable.setStroke(dp(1), stroke);
        return drawable;
    }

    private void configureWebView(WebView view) {
        view.setWebViewClient(new WebClient(this));
        view.setWebChromeClient(new WebChromeClient(this, tabManager));
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
            b.setText(tab.title == null || tab.title.trim().isEmpty() ? "New Tab" : tab.title);
            b.setTextSize(12);
            b.setTextColor(Color.rgb(62, 55, 76));
            b.setAllCaps(false);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setSingleLine(true);
            b.setEllipsize(android.text.TextUtils.TruncateAt.END);
            b.setPadding(dp(10), 0, dp(10), 0);
            b.setBackground(roundRect(
                    tabManager.getActive() == tab
                            ? Color.rgb(239, 234, 255)
                            : Color.rgb(249, 248, 251),
                    dp(16),
                    Color.TRANSPARENT));
            b.setOnClickListener(v -> tabManager.selectTab(tab.id));
            tabStrip.addView(b, new LinearLayout.LayoutParams(dp(154), dp(38)));
        }

        Button add = new Button(this);
        add.setText("+");
        add.setTextSize(20);
        add.setTextColor(Color.rgb(86, 73, 120));
        add.setAllCaps(false);
        add.setMinWidth(0);
        add.setMinimumWidth(0);
        add.setPadding(0, 0, 0, 0);
        add.setBackground(roundRect(Color.rgb(247, 245, 251), dp(19), Color.TRANSPARENT));
        add.setOnClickListener(v -> {
            WebTab tab = tabManager.createTab(StartPages.HOME);
            loadHome(tab.webView);
        });
        tabStrip.addView(add, new LinearLayout.LayoutParams(dp(40), dp(38)));
    }

    @Override
    public void onPageState(WebView view, String url, String title, boolean loading, boolean error) {
        WebTab active = tabManager.getActive();
        if (active == null || active.webView != view) return;

        active.title = (title == null || title.trim().isEmpty())
                ? (isHomeUrl(url) ? "Welcome" : "New Tab")
                : title;

        address.setText(displayUrl(url));
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        progress.setProgress(loading ? 60 : 100);

        if (error) {
            Toast.makeText(this, "Page failed to load. Use ↻ to retry.", Toast.LENGTH_SHORT).show();
        }

        onTabsChanged(tabManager.getTabs());
    }

    private boolean isHomeUrl(String url) {
        return url == null
                || url.isEmpty()
                || "about:blank".equals(url)
                || StartPages.HOME.equals(url);
    }

    private String displayUrl(String url) {
        return isHomeUrl(url) ? "" : url;
    }

    private void navigate(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return;

        if (!value.contains("://") && value.contains(".")) {
            value = "https://" + value;
        } else if (!value.contains("://")) {
            value = "https://www.google.com/search?q=" + Uri.encode(value);
        }

        WebTab tab = tabManager.getActive();
        if (tab != null) tab.webView.loadUrl(value);
    }

    private void home() {
        WebTab tab = tabManager.getActive();
        if (tab != null) loadHome(tab.webView);
    }

    private void loadHome(WebView view) {
        configureWebView(view);

        String html =
                "<!doctype html><html><head>" +
                "<meta name='viewport' content='width=device-width,initial-scale=1,viewport-fit=cover'>" +
                "<style>" +
                "*{box-sizing:border-box}" +
                "body{margin:0;background:linear-gradient(180deg,#fbfaff 0%,#f4f0ff 100%);" +
                "font-family:Arial,sans-serif;color:#25202e}" +
                ".wrap{max-width:720px;margin:0 auto;padding:42px 22px 56px}" +
                ".brand{text-align:center}" +
                ".logo{width:92px;height:92px;margin:4px auto 20px;display:block}" +
                "h1{font-size:31px;line-height:1.12;margin:0 0 10px;letter-spacing:-.7px}" +
                ".lead{font-size:16px;line-height:1.55;color:#686174;margin:0 auto 28px;max-width:560px}" +
                ".card{background:#fff;border:1px solid #e8e2f4;border-radius:20px;padding:18px;" +
                "margin:12px 0;box-shadow:0 5px 22px rgba(62,40,100,.06)}" +
                ".card h2{font-size:17px;margin:0 0 5px}.card p{font-size:13px;color:#777080;margin:0 0 13px}" +
                ".links{display:grid;grid-template-columns:1fr;gap:10px}" +
                "a{display:block;text-decoration:none;color:#33245f;background:#f2edff;border-radius:14px;" +
                "padding:14px 15px;font-weight:700;font-size:15px}" +
                ".small{font-size:12px;color:#777080;text-align:center;margin-top:22px;line-height:1.5}" +
                "@media(min-width:560px){.links{grid-template-columns:repeat(3,1fr)}a{min-height:72px}}" +
                "</style></head><body>" +
                "<main class='wrap'>" +
                "<section class='brand'>" +
                "<svg class='logo' viewBox='0 0 108 108' aria-label='Universal logo'>" +
                "<circle cx='54' cy='54' r='48' fill='#7C5CFF'/>" +
                "<path fill='#fff' d='M27 31 54 18l27 13-27 13-27-13m0 11 27 13 27-13v11L54 66 27 53V42m0 22 27 13 27-13v11L54 88 27 75V64'/>" +
                "</svg>" +
                "<h1>Welcome to Universal Web-Mob 2</h1>" +
                "<p class='lead'>A lightweight web workspace built for serious work on your phone — with tabs, downloads, uploads and desktop-friendly web access.</p>" +
                "</section>" +
                "<section class='card'><h2>Desktop-class web access</h2><p>Universal Web-Mob 2 can adapt a site when its mobile compatibility page asks for a desktop browser.</p>" +
                "<div class='links'>" +
                "<a href='https://www.google.com/'>Open the web</a>" +
                "<a href='https://www.wikipedia.org/'>Research</a>" +
                "<a href='https://www.youtube.com/'>Media</a>" +
                "</div></section>" +
                "<section class='card'><h2>Universal compatibility</h2><p>Use the menu to choose Universal, Mobile or Desktop compatibility for the current tab. Universal Web-Mob 2 can also detect common desktop-only messages and retry automatically.</p></section>" +
                "<p class='small'>Use the address bar above for any HTTPS website. Some services may still enforce their own device, account, security or feature restrictions.</p>" +
                "</main></body></html>";

        view.loadDataWithBaseURL(null, html, "text/html", "UTF-8", "about:blank");
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
        menu.getMenu().add("Universal compatibility");
        menu.getMenu().add("Mobile compatibility");
        menu.getMenu().add("Desktop compatibility");
        menu.getMenu().add("Open externally");
        menu.setOnMenuItemClickListener(item -> {
            String s = item.getTitle().toString();

            if ("New tab".equals(s)) {
                WebTab t = tabManager.createTab(StartPages.HOME);
                loadHome(t.webView);
            } else if ("Close tab".equals(s)) {
                WebTab t = tabManager.getActive();
                if (t != null) tabManager.closeTab(t.id);
            } else if ("Home".equals(s)) {
                home();
            } else if ("Retry".equals(s)) {
                reload();
            } else if ("Universal compatibility".equals(s)) {
                setCompatibility(CompatibilityProfile.UNIVERSAL);
            } else if ("Mobile compatibility".equals(s)) {
                setCompatibility(CompatibilityProfile.MOBILE);
            } else if ("Desktop compatibility".equals(s)) {
                setCompatibility(CompatibilityProfile.DESKTOP);
            } else if ("Open externally".equals(s)) {
                WebTab t = tabManager.getActive();
                if (t != null && t.webView.getUrl() != null && !isHomeUrl(t.webView.getUrl())) {
                    WebIntentHandler.openExternal(this, Uri.parse(t.webView.getUrl()));
                }
            }
            return true;
        });
        menu.show();
    }

    private void updateButtons() {}

    @Override
    public void onDesktopCompatibilityHint(WebView view) {
        WebTab active = tabManager == null ? null : tabManager.getActive();
        if (active == null || active.webView != view) return;
        if (active.compatibilityProfile == CompatibilityProfile.DESKTOP
                || active.desktopRetryAttempted) return;

        active.desktopRetryAttempted = true;
        active.compatibilityProfile = CompatibilityProfile.DESKTOP;
        tabManager.applyCompatibility(active);
        Toast.makeText(this, "Universal compatibility mode enabled for this site.",
                Toast.LENGTH_SHORT).show();
        view.reload();
    }

    private void setCompatibility(CompatibilityProfile profile) {
        WebTab active = tabManager == null ? null : tabManager.getActive();
        if (active == null) return;

        active.compatibilityProfile = profile;
        active.desktopRetryAttempted = profile == CompatibilityProfile.DESKTOP;
        tabManager.applyCompatibility(active);
        active.webView.reload();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST) return;

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
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
