package com.kunfei.bookshelf.view.web;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.URLUtil;

import com.kunfei.bookshelf.help.PreferencesHelper;
import com.kunfei.bookshelf.utils.LogHelper;
import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;

public class ExplorerWebView extends WebView {
    public OnScrollListener listener;
    private int touchX = 0, touchY = 0;
    private onSelectItemListener mOnSelectItemListener;
    private boolean isScrollChanged;
    private IScrollListener mScrollListener;

    public ExplorerWebView(Context context) {
        this(context, null);
    }

    public ExplorerWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExplorerWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setOnScrollListener(IScrollListener listener) {
        mScrollListener = listener;
    }

    private void init(Context context) {
//        Bundle data = new Bundle();
//        //true表示标准全屏，false表示X5全屏；不设置默认false，
//        data.putBoolean("standardFullScreen", true);
//        //false：关闭小窗；true：开启小窗；不设置默认true，
//        data.putBoolean("supportLiteWnd", false);
//        //1：以页面内开始播放，2：以全屏开始播放；不设置默认：1
//        data.putInt("DefaultVideoScreen", 1);
//        getX5WebViewExtension().invokeMiscMethod("setVideoParams", data);
        /**
         * 1) standardFullScreen 全屏设置
         *
         * 设置为true时，我们会回调WebChromeClient的onShowCustomView方法，由开发者自己实现全屏展示；
         *
         * 设置为false时，由我们实现全屏展示，我们实现全屏展示需要满足下面两个条件：
         *
         * a. 我们 Webview初始化的Context必须是Activity类型的Context
         *
         * b. 我们 Webview 所在的Activity要声明这个属性
         *
         * android:configChanges="orientation|screenSize|keyboardHidden"
         * 如果不满足这两个条件，standardFullScreen 自动置为 true
         *
         * 2) supportLiteWnd 小窗播放设置
         *
         * 前提standardFullScreen=false，这个条件才生效
         *
         * 设置为 true， 开启小窗功能
         *
         * 设置为 false，不使用小窗功能
         *
         * 3) DefaultVideoScreen 初始播放形态设置
         *
         * a、以页面内形态开始播放
         *
         * b、以全屏形态开始播放
         */
        WebSettings setting = getSettings();
        String ua_str_android = buildUserAgentString(context, setting, 0, "explorer");
        String ua_str_pc = buildUserAgentString(context, setting, 1, "explorer");
        String ua_str_ios = buildUserAgentString(context, setting, 2, "explorer");
        PreferencesHelper.getInstance().putString("ua_str_android", ua_str_android);
        PreferencesHelper.getInstance().putString("ua_str_pc", ua_str_pc);

        PreferencesHelper.getInstance().putString("ua_str_ios", ua_str_ios);
        switch (AppSetting.getInstance().ismIsUaCheck()) {
            case 0:
                //android
                setting.setUserAgentString(ua_str_android);
                setting.setUseWideViewPort(false);
                break;
            case 1:
                //电脑
                setting.setUserAgentString(ua_str_pc);
                setting.setUseWideViewPort(true);
                break;
            case 2:
                setting.setUserAgentString(ua_str_ios);
                setting.setUseWideViewPort(false);
                break;
        }

        setting.setGeolocationEnabled(true);
        setting.setJavaScriptEnabled(true);
        //设置自适应屏幕，两者合用（下面这两个方法合用）
        setting.setUseWideViewPort(true); //将图片调整到适合webview的大小
        setting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小
        //缩放操作
        setting.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        setting.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        setting.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //其他细节操作
        setting.setCacheMode(WebSettings.LOAD_DEFAULT); //（默认）根据cache-control决定是否从网络上取数据。
        setting.setAllowFileAccess(true); //设置可以访问文件
        setting.setJavaScriptCanOpenWindowsAutomatically(true); //不支持通过JS打开新窗口
        setting.setLoadsImagesAutomatically(true); //支持自动加载图片
        setting.setDefaultTextEncodingName("utf-8");//设置编码格式
        setting.setSupportMultipleWindows(true);
        setting.setTextZoom(AppSetting.getInstance().getFontSize());
        setting.setAppCacheMaxSize(1024 * 1024 * 10);
        setting.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        setting.setDatabaseEnabled(true);   //开启 database storage API 功能
        setting.setAppCacheEnabled(true);//开启 Application Caches 功能
        setting.setTextSize(WebSettings.TextSize.NORMAL);
        String appCachePath = getContext().getCacheDir().getAbsolutePath();
        setting.setAppCachePath(appCachePath);//设置  Application Caches 缓存目录
        setting.setSavePassword(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //android5.0以后webview默认不在保存cookie所以会导致第三方登录状态无法保存
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setting.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        setDayOrNight(AppSetting.getInstance().isIsDayMode());

        setOnLongClickListener(v -> {
            HitTestResult result = getHitTestResult();
            if (null == result)
                return false;
            int type = result.getType();
            String extra = result.getExtra();
            switch (type) {
                case HitTestResult.PHONE_TYPE: // 处理拨号
                    break;
                case HitTestResult.EMAIL_TYPE: // 处理Email
                    break;
                case HitTestResult.GEO_TYPE: // 　地图类型
                    break;
                case HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                    if (mOnSelectItemListener != null && extra != null && URLUtil.isValidUrl(extra)) {
                        mOnSelectItemListener.onLinkSelected(touchX, touchY, result.getType(), extra);
                    }
                    return true;
                case HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 带有链接的图片类型
                case HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                    if (mOnSelectItemListener != null && extra != null && URLUtil.isValidUrl(extra)) {
                        mOnSelectItemListener.onImgSelected(touchX, touchY, result.getType(), extra);
                    }
                    return true;
                case HitTestResult.UNKNOWN_TYPE: //未知
                    break;
                case HitTestResult.EDIT_TEXT_TYPE://文字
                    break;
            }
            return false;

        });
    }

    public void setOnSelectItemListener(onSelectItemListener onSelectItemListener) {
        mOnSelectItemListener = onSelectItemListener;
    }

    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
    }

    /**
     * @param context
     * @param settings
     * @param appName
     * @return
     */
    public String buildUserAgentString(final Context context, final WebSettings settings, final int type, final String appName) {

        final StringBuilder uaBuilder = new StringBuilder();
        uaBuilder.append("Mozilla/5.0");
        switch (type) {
            case 0:
                uaBuilder.append(" (Linux; Android ").append(Build.VERSION.RELEASE).append(") ");
                break;
            case 1:
                uaBuilder.append(" (Linux; diordnA ").append(Build.VERSION.RELEASE).append(") ");
                break;
            case 2:
                uaBuilder.append("(iPhone; U; CPU iPhone OS 4_3_3 like Mac OS X)");
                break;

        }
        final String existingWebViewUA = settings.getUserAgentString();
        final String appVersion;
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("Unable find package details for Focus", e);
        }
        final String focusToken = appName + "/" + appVersion;
        uaBuilder.append(getUABrowserString(existingWebViewUA, focusToken));
        return uaBuilder.toString();
    }

    /**
     * @param existingUAString
     * @param focusToken
     * @return
     */
    public String getUABrowserString(final String existingUAString, final String focusToken) {
        int start = existingUAString.indexOf("AppleWebKit");
        if (start == -1) {
            start = existingUAString.indexOf(")") + 2;
            if (start >= existingUAString.length()) {
                return focusToken;
            }
        }
        final String[] tokens = existingUAString.substring(start).split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("Chrome")) {
                tokens[i] = focusToken + " " + tokens[i];
                return TextUtils.join(" ", tokens);
            }
        }
        return TextUtils.join(" ", tokens) + " " + focusToken;
    }

    @Override
    public void destroy() {
        ViewGroup parent = ((ViewGroup) getParent());
        if (parent != null) {
            parent.removeAllViews();
        }
        super.destroy();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按住
                isScrollChanged = false;
                break;
            case MotionEvent.ACTION_UP://抬起
                isScrollChanged = true;
                break;
            case MotionEvent.ACTION_MOVE://移动
                isScrollChanged = false;
                break;
        }
        touchX = (int) event.getX();
        touchY = (int) event.getY();
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.getScrollY() <= 0)
                    this.scrollTo(0, 1);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (listener != null) {
            if (isScrollChanged) {
                int y = t - oldt;
                if (y > 5) {
                    listener.onScrollDown();
                    LogHelper.i("onScrollChanged", "onScrollDown==" + y);
                } else if (y < -5) {
                    listener.onScrollUp();
                    LogHelper.i("onScrollChanged", "onScrollUp==" + y);
                }
            }
        }
        //只要是通过webview的滚动距离 t
        if (mScrollListener != null) {
            mScrollListener.onScrollChanged(t);
        }
    }

    public void setListener(OnScrollListener listener) {
        this.listener = listener;
    }

    public interface IScrollListener {
        void onScrollChanged(int scrollY);
    }

    public interface OnScrollListener {
        void onScrollUp();//上滑

        void onScrollDown();//下滑
    }

    public interface onSelectItemListener {
        void onImgSelected(int x, int y, int type, String extra);

        void onLinkSelected(int x, int y, int type, String extra);
    }
}

