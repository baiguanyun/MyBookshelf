package com.kunfei.bookshelf.view.web;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.view.View;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebView;


public interface WebViewController {
    Context getContext();

    Activity getActivity();

    TabController getTabController();

    WebViewFactory getWebViewFactory();

    void onSetWebView(Tab tab, WebView view);

    void onPageStarted(Tab tab, WebView webView, Bitmap favicon);

    void onPageFinished(Tab tab, WebView view, String url);

    void onProgressChanged(Tab tab);

    void onReceivedTitle(WebView view, final String title);

    void onFavicon(Tab tab, WebView view, Bitmap icon);

    boolean shouldOverrideUrlLoading(WebView webView, String s);

    void onLoadResource(WebView webView, String s);

    void onReceivedError(WebView webView, int i, String s, String s1);

    boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg);

    void newWindow(String url);

    void onDownloadListener(String url);

    void uploadMessageFile(ValueCallback<Uri[]> valueCallback);

    void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback);

    void onHideCustomView();
}
