package com.kunfei.bookshelf.view.web;

import android.content.Context;
import android.util.AttributeSet;

public class BrowserWebViewFactory implements WebViewFactory {
    private final Context mContext;

    public BrowserWebViewFactory(Context context) {
        mContext = context;
    }

    private ExplorerWebView instantiateWebView(AttributeSet attrs, int defStyle) {
        return new ExplorerWebView(mContext, attrs, defStyle);
    }

    @Override
    public ExplorerWebView createWebView() {
        return instantiateWebView(null, android.R.attr.webViewStyle);
    }
}
