package com.kunfei.bookshelf.view.web;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.ad.AdBlocker;
import com.kunfei.bookshelf.bean.AdBlockingUserBean;
import com.kunfei.bookshelf.view.activity.browser.ImagePreviewActivity;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.SettingHelper;
import com.kunfei.bookshelf.utils.ADIntentUtils;
import com.kunfei.bookshelf.utils.DensityUtil;
import com.kunfei.bookshelf.utils.GlideUtils;
import com.kunfei.bookshelf.utils.LogHelper;
import com.kunfei.bookshelf.utils.NetworkUtils;
import com.kunfei.bookshelf.utils.StringUtils;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebBackForwardList;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Tab implements ExplorerWebView.onSelectItemListener {

    static final String ID = "_id";
    static final String CURRENT_URL = "currentUrl";
    static final String CURRENT_TITLE = "currentTitle";
    private static final int REQUEST_CODE_CHOOSE = 23;
    private final static String TAG = "TAB";
    private static final int INITIAL_PROGRESS = 5;
    private static final int MSG_CAPTURE = 42;
    private static Bitmap sDefaultFavicon; //默认网站图标
    private static Paint sAlphaPaint = new Paint();

    static {
        sAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        sAlphaPaint.setColor(Color.TRANSPARENT);
    }

    public String mSavePageTitle;
    // Subwindow container
    public String mSavePageUrl;
    // Subwindow WebView
    private boolean mWillBeClosed = false;
    private long mId = -1;
    // WebView controller
    private WebViewController mWebViewController;
    private Context mContext;
    // Main WebView wrapper
    private View mContainer;
    // Main WebView
    private ExplorerWebView mMainView;
    private View mSubViewContainer;
    private ExplorerWebView mSubView;
    // Saved bundle for when we are running low on memory. It contains the
    // information needed to restore the WebView if the user goes back to the
    // tab.
    private Bundle mSavedState;
    // If true, the tab is in page loading state (after onPageStarted,
    // before onPageFinsihed)
    private boolean mInPageLoad;
    // The last reported progress of the current page
    private int mPageLoadProgress;
    // The time the load started, used to find load page time
    private long mLoadStartTime;
    private int mCaptureWidth;
    private int mCaptureHeight;
    private Bitmap mCapture;
    private Handler mHandler;
    // 用来存储页面信息
    private boolean mUpdateThumbnail;
    // save page
    private HashMap<Integer, Long> mSavePageJob;
    private PageState mCurrentState;
    private boolean mInForeground;
    private boolean isSearch;
    private boolean isMain;
    private boolean isCreateWindow;
    private boolean isHistoryWindow;
    private HashMap<String, String> urlMap = new HashMap<>();
    // 构造WebViewClient
    private final WebViewClient mWebViewClient = new WebViewClient() {

        private Map<String, Boolean> loadedUrls = new HashMap<>();

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mCurrentState = new PageState(mContext, url, favicon);
            mInPageLoad = true;
            mUpdateThumbnail = true;
            mPageLoadProgress = INITIAL_PROGRESS;
            mLoadStartTime = SystemClock.uptimeMillis();
            mWebViewController.onPageStarted(Tab.this, view, favicon);
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        //重写此方法才能够处理在浏览器中的按键事件
        @Override
        public boolean shouldOverrideKeyEvent(WebView webView, KeyEvent keyEvent) {
            return super.shouldOverrideKeyEvent(webView, keyEvent);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest webResourceRequest) {
            return super.shouldInterceptRequest(webView, webResourceRequest);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView webView, String url) {
            if (SettingHelper.getInterceptStatus()) {
                urlMap.put(url, url);
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                if (ad) {
                    LogHelper.i("lanjiehh", NetworkUtils.getDomain(url));
                    LogHelper.i("lanjiehh", url);
                }
                return ad ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(webView, url);
            }
            return super.shouldInterceptRequest(webView, url);
        }

        @Override
        public void onReceivedError(WebView webView, int i, String s, String s1) {
            super.onReceivedError(webView, i, s, s1);
            mWebViewController.onReceivedError(webView, i, s, s1);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            mWebViewController.shouldOverrideUrlLoading(webView, url);
            ADIntentUtils adIntentUtils = new ADIntentUtils((Activity) mContext);
            return adIntentUtils.shouldOverrideUrlLoadingByApp(webView, url);
        }

        @Override
        public void onLoadResource(WebView webView, String s) {
            super.onLoadResource(webView, s);
            mWebViewController.onLoadResource(webView, s);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            syncCurrentState(view, url);
            mWebViewController.onPageFinished(Tab.this, view, url);
            if (SettingHelper.getInterceptStatus()) {
                List<AdBlockingUserBean> adList = DbHelper.getDaoSession().getAdBlockingUserBeanDao().queryBuilder().list();
                for (AdBlockingUserBean key : adList) {
                    if (urlMap.get(key.getHostName()).contains(key.getHostName())) {
                        if (key.getHostName().endsWith("gif") || key.getHostName().endsWith("jpg") || key.getHostName().endsWith("png")) {
                            jsNoneImage(urlMap.get(key.getHostName()));
                        } else {
                            jsNoneHerf(urlMap.get(key.getHostName()));
                        }
                    }
                }
                noneTop();
            }
            if (url != null && url.equals(mSavePageUrl)) {
                mCurrentState.mTitle = mSavePageTitle;
                mCurrentState.mUrl = mSavePageUrl;
            }
            // 传递 html dom
            // 如果是百度/搜狗这些搜索引擎则忽略
            if (url.contains("baidu.com") || url.contains("sogou.com") || url.contains("so.com") ||
                    url.contains("sm.cn") || url.contains("bing.com") || url.contains("taobao.com") ||
                    url.contains("google.com") || url.contains("yz.m.sm.cn")) {
                return; // pass 下面的代码
            }
            String func = mContext.getResources().getString(R.string.get_dom);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                view.loadUrl(func);
            } else {
                view.evaluateJavascript(func, new com.tencent.smtt.sdk.ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                        // ignore
                    }
                });
            }
        }

        @Override
        public void onScaleChanged(WebView webView, float oldScale, float newScale) {
            super.onScaleChanged(webView, oldScale, newScale);
            if (newScale - oldScale > 7) {
                webView.setInitialScale((int) (oldScale / newScale * 100)); //异常放大，缩回去。
            }
        }
    };
    // 构造 WebChromeClient
    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        private View mXProgressVideo;

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
            mWebViewController.uploadMessageFile(valueCallback);
            return true;
        }


        @Override
        public void openFileChooser(ValueCallback<Uri> valueCallback, String s, String s1) {
            super.openFileChooser(valueCallback, s, s1);
        }

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            mWebViewController.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            return true;
        }

        @Override
        public View getVideoLoadingProgressView() {
            if (mXProgressVideo == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                mXProgressVideo = inflater.inflate(R.layout.video_loading_progress, null);
            }
            return mXProgressVideo;
        }

        @Override
        public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback customViewCallback) {
            super.onShowCustomView(view, customViewCallback);
            mWebViewController.onShowCustomView(view, customViewCallback);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            mWebViewController.onHideCustomView();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mPageLoadProgress = newProgress;
            if (newProgress == 100) {
                mInPageLoad = false;
                syncCurrentState(view, view.getUrl());
            }
            mWebViewController.onProgressChanged(Tab.this);
            if (mUpdateThumbnail && newProgress == 100) {
                mUpdateThumbnail = false;
            }
        }

        @Override
        public boolean onJsAlert(WebView webView, String s, String message, JsResult jsResult) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(webView.getContext());
            builder.setTitle("提示")
                    .setMessage(message)
                    .setPositiveButton("确定", null);
            builder.setOnKeyListener((dialog, keyCode, event) -> {
                LogHelper.v("onJsAlert", "keyCode==" + keyCode + "event=" + event);
                return true;
            });
            // 禁止响应按back键的事件
            builder.setCancelable(false);
            AlertDialog dialog = builder.create();
            dialog.show();
            jsResult.confirm();// 因为没有绑定事件，需要强行confirm,否则页面会变黑显示不了内容。
            return true;
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mCurrentState.mTitle = title;
            mWebViewController.onReceivedTitle(view, title);
        }

        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
            mCurrentState.mFavicon = icon;
            mWebViewController.onFavicon(Tab.this, view, icon == null ? getDefaultFavicon(mContext) : icon);
        }
    };

    public Tab(WebViewController webViewController, ExplorerWebView view, Bundle state) {
        mSavePageJob = new HashMap<Integer, Long>();
        mWebViewController = webViewController;
        mContext = mWebViewController.getContext();
        AdBlocker.init();
        mCurrentState = new PageState(mContext);
        mInPageLoad = false;
        mCaptureWidth = DensityUtil.getDisplayPoint(mContext).x;
        mCaptureHeight = DensityUtil.getDisplayPoint(mContext).y;
        updateShouldCaptureThumbnails();
        restoreState(state);
        if (getId() == -1) {
            mId = TabController.getNextId();
        }
        setWebView(view);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_CAPTURE:
                        capture();
                        break;
                }
            }
        };
    }

    public Tab(WebViewController webViewController, ExplorerWebView view) {
        this(webViewController, view, null);
    }

    public Tab(WebViewController webViewController, Bundle state) {
        this(webViewController, null, state);
    }

    // 获取默认网页图标
    private static synchronized Bitmap getDefaultFavicon(Context context) {
        if (sDefaultFavicon == null) {
            sDefaultFavicon = BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_internet);
        }
        return sDefaultFavicon;
    }

    public PageState getmCurrentState() {
        return mCurrentState;
    }

    public void setmCurrentState(PageState mCurrentState) {
        this.mCurrentState = mCurrentState;
    }

    public boolean isCreateWindow() {
        return isCreateWindow;
    }

    public void setCreateWindow(boolean createWindow) {
        isCreateWindow = createWindow;
    }

    public boolean isHistoryWindow() {
        return isHistoryWindow;
    }

    public void setHistoryWindow(boolean historyWindow) {
        isHistoryWindow = historyWindow;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    private void restoreState(Bundle state) {
        mSavedState = state;
        if (mSavedState == null) {
            return;
        }
        mId = state.getLong(ID);
        String url = state.getString(CURRENT_URL);
        String title = state.getString(CURRENT_TITLE);
        mCurrentState = new PageState(mContext, url, title);
    }

    public void shouldUpdateThumbnail(boolean should) {
        mUpdateThumbnail = should;
        if (should) capture();
    }

    /**
     * This is used to get a new ID when the tab has been preloaded, before it is displayed and
     * added to TabControl. Preloaded tabs can be created before restoreInstanceState, leading
     * to overlapping IDs between the preloaded and restored tabs.
     */
    public void refreshIdAfterPreload() {
        mId = TabController.getNextId();
    }

    public void updateShouldCaptureThumbnails() {
        synchronized (Tab.this) {
            if (mCapture == null) {
                mCapture = Bitmap.createBitmap(mCaptureWidth, mCaptureHeight,
                        Bitmap.Config.RGB_565);
                mCapture.eraseColor(Color.WHITE);
            }
        }
    }

    public void setController(WebViewController ctl) {
        mWebViewController = ctl;
        updateShouldCaptureThumbnails();
    }

    public long getId() {
        return mId;
    }

    /**
     * Sets the WebView for this tab, correctly removing the old WebView from
     * the container view.
     */
    void setWebView(ExplorerWebView w, boolean restore) {
        if (mMainView == w) {
            return;
        }
        if (w != null) {
            mWebViewController.onSetWebView(this, w);
        }

        if (mMainView != null) {
            mMainView.setPictureListener(null);
            if (w != null) {
                syncCurrentState(w, null);
            } else {
                mCurrentState = new PageState(mContext);
            }
        }
        // set the new one
        mMainView = w;
        // attach the WebViewClient, WebChromeClient and DownloadListener
        if (mMainView != null) {
            mMainView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                mWebViewController.onDownloadListener(url);
            });

            mMainView.setWebViewClient(mWebViewClient);
            mMainView.setWebChromeClient(mWebChromeClient);
            mMainView.setOnSelectItemListener(this);
            if (restore && (mSavedState != null)) {
                // restoreUserAgent();
                WebBackForwardList restoredState
                        = mMainView.restoreState(mSavedState);
                if (restoredState == null || restoredState.getSize() == 0) {
                    Log.w(TAG, "Failed to restore WebView state!");
                    loadUrl(mCurrentState.mOriginalUrl, null);
                }
                mSavedState = null;
            }
        }
    }

    /**
     * Destroy the tab's main WebView and subWindow if any
     */
    void destroy() {
        if (urlMap != null) {
            urlMap.clear();
        }
        if (mMainView != null) {
            dismissSubWindow();
            // save the WebView to call destroy() after detach it from the tab
            WebView webView = mMainView;
            setWebView(null);
            webView.destroy();
        }
        /// M: add for save page @ {
        if (mSavePageJob == null) {
            return;
        }
        if (mSavePageJob.size() != 0) {
            // new CancelSavePageTask().execute();
        }
    }

    /**
     * Dismiss the subWindow for the tab.
     */
    void dismissSubWindow() {
        if (mSubView != null) {
            mSubView.destroy();
            mSubView = null;
            mSubViewContainer = null;
        }
    }

    void resume() {
        if (mMainView != null) {
            setupHwAcceleration(mMainView);
            mMainView.onResume();
            if (mSubView != null) {
                mSubView.onResume();
            }
        }
    }

    void pause() {
        if (mMainView != null) {
            mMainView.onPause();
            if (mSubView != null) {
                mSubView.onPause();
            }
        }
    }

    void putInForeground() {
        if (mInForeground) {
            return;
        }
        mInForeground = true;
        resume();
    }

    void putInBackground() {
        if (!mInForeground) {
            return;
        }
        capture();
        mInForeground = false;
        pause();
        mMainView.setOnCreateContextMenuListener(null);
        if (mSubView != null) {
            mSubView.setOnCreateContextMenuListener(null);
        }
    }

    boolean inForeground() {
        return mInForeground;
    }

    /**
     * Return the main window of this tab. Note: if a tab is freed in the
     * background, this can return null. It is only guaranteed to be
     * non-null for the current tab.
     *
     * @return The main WebView of this tab.
     */
    public ExplorerWebView getWebView() {
        return mMainView;
    }

    void setWebView(ExplorerWebView w) {
        setWebView(w, true);
    }

    void setViewContainer(View container) {
        mContainer = container;
    }

    public String getUrl() {
        return mCurrentState.mUrl;
    }

    public boolean checkUrlNotNull() {
        return mCurrentState.checkUrlNotNull();
    }

    public String getOriginalUrl() {
        if (mCurrentState.mOriginalUrl == null) {
            return getUrl();
        }
        return mCurrentState.mOriginalUrl;
    }

    /**
     * Get the title of this tab.
     */
    public String getTitle() {
        if (mCurrentState.mTitle == null && mInPageLoad) {
            return mContext.getString(R.string.title_bar_loading);
        }
        return mCurrentState.mTitle;
    }

    /**
     * Get the favicon of this tab.
     */
    public Bitmap getFavicon() {
        if (mCurrentState.mFavicon != null) {
            return mCurrentState.mFavicon;
        }
        return getDefaultFavicon(mContext);
    }

    public int getPageLoadProgress() {
        return mPageLoadProgress;
    }

    public void clearTabData() {
        mUpdateThumbnail = false;
        mCurrentState.mUrl = "";
        mCurrentState.mOriginalUrl = "";
        mCurrentState.mTitle = mContext.getString(R.string.app_name);
        mCurrentState.mFavicon = getDefaultFavicon(mContext);
    }

    /**
     * @return TRUE if onPageStarted is called while onPageFinished is not
     * called yet.
     */
    boolean inPageLoad() {
        return mInPageLoad;
    }

    /**
     * @return The Bundle with the tab's state if it can be saved, otherwise null
     */
    public Bundle saveState() {
        // If the WebView is null it means we ran low on memory and we already
        // stored the saved state in mSavedState.
        if (mMainView == null) {
            return mSavedState;
        }

        if (TextUtils.isEmpty(mCurrentState.mUrl)) {
            return null;
        }

        mSavedState = new Bundle();
        WebBackForwardList savedList = mMainView.saveState(mSavedState);
        if (savedList == null || savedList.getSize() == 0) {
            Log.w(TAG, "Failed to save back/forward list for "
                    + mCurrentState.mUrl);
        }

        mSavedState.putLong(ID, mId);
        mSavedState.putString(CURRENT_URL, mCurrentState.mUrl);
        mSavedState.putString(CURRENT_TITLE, mCurrentState.mTitle);
        return mSavedState;
    }

    public Bitmap getScreenshot() {
        synchronized (Tab.this) {
            return mCapture;
        }
    }

    private void setupHwAcceleration(View web) {
        if (web == null) return;
        // 这里需要用户自己设置
        if (true) {
            web.setLayerType(View.LAYER_TYPE_NONE, null);
        } else {
            web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    public void stopLoading() {
        if (mMainView != null && inPageLoad()) {
            mMainView.stopLoading();
        }
    }

    public void reloadPage() {
        mMainView.reload();
    }

    private void syncCurrentState(WebView view, String url) {
        if (mWillBeClosed) {
            return;
        }
        mCurrentState.mUrl = view.getUrl();
        if (mCurrentState.mUrl == null) {
            mCurrentState.mUrl = mContext.getString(R.string.app_name);
        }
        mCurrentState.mOriginalUrl = view.getOriginalUrl();
        mCurrentState.mTitle = view.getTitle();
        mCurrentState.mFavicon = view.getFavicon();
    }

    public void loadUrl(String url, Map<String, String> headers) {
        if (mMainView != null) {
            mPageLoadProgress = INITIAL_PROGRESS;
            mInPageLoad = true;
            mWebViewController.onPageStarted(this, mMainView, null);
            try {
                mMainView.loadUrl(url, headers);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    public void capture() {
        if (mMainView == null || mCapture == null) return;
        View view = mWebViewController.getActivity().getWindow().getDecorView();
        Canvas c = new Canvas(mCapture);
        int state = c.save();
        view.draw(c);
        c.restoreToCount(state);
        c.setBitmap(null);
        mHandler.removeMessages(MSG_CAPTURE);
        TabController tc = mWebViewController.getTabController();
        if (tc != null) {
            TabController.OnThumbnailUpdatedListener updateListener
                    = tc.getOnThumbnailUpdatedListener();
            if (updateListener != null) {
                updateListener.onThumbnailUpdated(this);
            }
        }
    }

    public boolean canGoBack() {
        return mMainView != null ? mMainView.canGoBack() : false;
    }

    public boolean canGoForward() {
        return mMainView != null ? mMainView.canGoForward() : false;
    }

    public void goBack() {
        if (mMainView != null) {
            mMainView.goBack();
        }
    }

    public void goForward() {
        if (mMainView != null) {
            mMainView.goForward();
        }
    }

    @Override
    public void onImgSelected(int x, int y, int type, String extra) {
        ArrayList<String> titleList = new ArrayList<>();
        titleList.add(extra);
        titleList.add("保存图片");
        titleList.add("预览图片");
        titleList.add("复制图片链接");
        titleList.add("分享图片");
        if (SettingHelper.getInterceptStatus())
            titleList.add("标记为广告");
        HandleListDialog handleListDialog = HandleListDialog.getInstance(x, y, titleList);
        handleListDialog.setOnItemClickListener(position -> {
            switch (position) {
                case 0:
                    StringUtils.copyText(mContext, extra);
                    Toast.makeText(mContext, "已复制到粘贴板", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    GlideUtils.saveImage(mContext, extra);
                    break;
                case 2:
                    ImagePreviewActivity.start(mContext, extra);
                    break;
                case 3:
                    StringUtils.copyText(mContext, extra);
                    Toast.makeText(mContext, "已复制到粘贴板", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    //TODO
                    //DialogUtils.shareUDialog((Activity) mContext, extra, extra, true, extra);
                    break;
                case 5:
                    saveAdBlockingUserBean(extra);
                    jsNoneImage(extra);
                    Toast.makeText(mContext, "已标记为广告", Toast.LENGTH_SHORT).show();
                    break;
            }
            handleListDialog.dismiss();
        });
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
        handleListDialog.show(fragmentManager);
    }

    @Override
    public void onLinkSelected(int x, int y, int type, String extra) {
        ArrayList<String> titleList = new ArrayList<>();
        titleList.add(extra);
        titleList.add("复制网页链接");
        titleList.add("新窗口打开");
        if (SettingHelper.getInterceptStatus())
            titleList.add("标记为广告");

        HandleListDialog handleListDialog = HandleListDialog.getInstance(x, y, titleList);
        handleListDialog.setOnItemClickListener(position -> {
            switch (position) {
                case 0:
                    StringUtils.copyText(mContext, extra);
                    break;
                case 1:
                    StringUtils.copyText(mContext, extra);
                    break;
                case 2:
                    mWebViewController.newWindow(extra);
                    break;
                case 3:
                    saveAdBlockingUserBean(extra);
                    jsNoneHerf(extra);
                    Toast.makeText(MApplication.getInstance(), "已标记为广告", Toast.LENGTH_SHORT).show();
                    break;
            }
            handleListDialog.dismiss();
        });
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
        handleListDialog.show(fragmentManager);
    }

    private void saveAdBlockingUserBean(String extra) {
        Disposable disposable = Observable.just(extra)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String url) throws Exception {
                        DbHelper.getDaoSession().getAdBlockingUserBeanDao().insert(new AdBlockingUserBean(url, NetworkUtils.getDomain(getWebView().getUrl())));
                    }
                });
    }

    private void jsNoneHerf(String url) {
        String js = "javascript: (function () {\n" +
                "    var aList = document.getElementsByTagName(\"a\");\n" +
                "    var parentList = [];\n" +
                "    for (var i = 0; i < aList.length; i++) {\n" +
                "        parentList = parentList.concat([aList[i].parentElement]);\n" +
                "    }\n" +
                "    for (var i = 0; i < aList.length; i++) {\n" +
                "        if (aList[i].getAttribute(\"href\").indexOf(\"" + url + "\") != -1) {\n" +
                "            parentList[i].style.display = \"none\";\n" +
                "        }\n" +
                "    }\n" +
                "})();";
        LogHelper.i("wessd", js);
        getWebView().loadUrl(js);
    }

    private void noneTop() {
        getWebView().loadUrl("javascript: function hideTop(){" +
                "var a = document.getElementsByTagName('a');\n" +
                "\t\tfor(var i = 0; i < a.length; i++) {\n" +
                "\t\t\tif(a[i].getAttribute(\"class\") == 'top') {\n" +
                "\t\t\t\ta[i].style.display = \"none\"\n" +
                "\t\t\t}\n" +
                "\n" +
                "\t\t}" +
                "}");
        getWebView().loadUrl("javascript:hideTop()");
    }

    private void jsNoneImage(String url) {
        String js = "javascript: (function () {\n" +
                "    var aList = document.getElementsByTagName(\"img\");\n" +
                "    var parentList = [];\n" +
                "    for (var i = 0; i < aList.length; i++) {\n" +
                "        parentList = parentList.concat([aList[i].parentElement]);\n" +
                "    }\n" +
                "    for (var i = 0; i < aList.length; i++) {\n" +
                "        if (aList[i].getAttribute(\"src\").indexOf(\"" + url + "\") != -1) {\n" +
                "            parentList[i].style.display = \"none\";\n" +
                "        }\n" +
                "    }\n" +
                "})();";
        LogHelper.i("wessd", js);
        getWebView().loadUrl(js);
    }

    public String getHost(String str) {
        java.net.URL url = null;
        try {
            url = new java.net.URL(str);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url.getHost();
    }

    public static class PageState {
        String mUrl;
        String mOriginalUrl;
        String mTitle;
        Bitmap mFavicon;

        PageState(Context context) {
            this(context, "", getDefaultFavicon(context));
        }

        PageState(Context context, String url, Bitmap favicon) {
            this(url, context.getString(R.string.app_name), favicon);
        }


        public PageState(Context context, String url, String title) {
            this(url, title, getDefaultFavicon(context));
        }

        PageState(String url, String title, Bitmap favicon) {
            mUrl = mOriginalUrl = url;
            mTitle = title;
            mFavicon = favicon;
        }

        boolean checkUrlNotNull() {
            return !TextUtils.isEmpty(mUrl);
        }

    }
}

