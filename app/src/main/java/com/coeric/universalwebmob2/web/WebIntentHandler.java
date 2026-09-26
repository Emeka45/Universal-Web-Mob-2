package com.coeric.universalwebmob2.web;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public final class WebIntentHandler {
    private WebIntentHandler() {}

    public static boolean openExternal(Context context, Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
