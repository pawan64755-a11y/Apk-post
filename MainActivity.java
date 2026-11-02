package com.relationshipcues.fbpost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView web;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int bg = Color.parseColor("#0F1117");
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        web = new WebView(this);
        web.setBackgroundColor(bg);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        // The page calls the Gemini / DeepSeek APIs directly from local assets
        s.setAllowUniversalAccessFromFileURLs(true);

        web.addJavascriptInterface(new Bridge(), "Android");

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternal(request.getUrl().toString());
            }
        });

        web.loadUrl("file:///android_asset/index.html");
    }

    private boolean openExternal(String url) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            return false;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
        }
        return true;
    }

    private class Bridge {
        @JavascriptInterface
        public void copy(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null) {
                        cm.setPrimaryClip(ClipData.newPlainText("prompt", text));
                    }
                }
            });
        }

        @JavascriptInterface
        public void openUrl(final String url) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    openExternal(url);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
