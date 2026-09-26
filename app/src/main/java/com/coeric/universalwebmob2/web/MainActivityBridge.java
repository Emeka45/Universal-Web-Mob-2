package com.coeric.universalwebmob2.web;

import android.webkit.ValueCallback;
import android.net.Uri;

public final class MainActivityBridge {
    private static ValueCallback<Uri[]> pending;

    private MainActivityBridge() {}

    public static void setPendingFileCallback(ValueCallback<Uri[]> callback) {
        pending = callback;
    }

    public static void deliver(Uri[] result) {
        if (pending != null) {
            pending.onReceiveValue(result);
            pending = null;
        }
    }

    public static void cancel() {
        deliver(null);
    }
}
