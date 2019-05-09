package com.kunfei.bookshelf.view.activity.browser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nukc.stateview.StateView;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseBrowserActivity;
import com.kunfei.bookshelf.base.RxBus;
import com.kunfei.bookshelf.bean.SearchplBean;
import com.kunfei.bookshelf.bean.TabMagBean;
import com.kunfei.bookshelf.bean.WebHistoryBean;
import com.kunfei.bookshelf.bean.event.LoadWebUrlEvent;
import com.kunfei.bookshelf.bean.event.MainSearchEvent;
import com.kunfei.bookshelf.bean.event.OnFontSizeChangeEvent;
import com.kunfei.bookshelf.constant.Constants;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.PreferencesHelper;
import com.kunfei.bookshelf.help.SettingHelper;
import com.kunfei.bookshelf.utils.DensityUtil;
import com.kunfei.bookshelf.utils.LogHelper;
import com.kunfei.bookshelf.utils.Utils;
import com.kunfei.bookshelf.view.activity.MainActivity;
import com.kunfei.bookshelf.view.adapter.browser.TabAdapter;
import com.kunfei.bookshelf.view.stackview.widget.TabStackView;
import com.kunfei.bookshelf.view.web.BrowserWebViewFactory;
import com.kunfei.bookshelf.view.web.ExplorerWebView;
import com.kunfei.bookshelf.view.web.MainActivityView;
import com.kunfei.bookshelf.view.web.RefreshImageView;
import com.kunfei.bookshelf.view.web.Tab;
import com.kunfei.bookshelf.view.web.TabCard;
import com.kunfei.bookshelf.view.web.TabController;
import com.kunfei.bookshelf.view.web.TabWebController;
import com.kunfei.bookshelf.view.web.WebViewFactory;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainCCActivity extends BaseBrowserActivity implements
        TabStackView.OnChildDismissedListener,
        TabWebController {
    public static final long TWO_SECOND = 2 * 1000;
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_CHOOSE = 23;

    //vector 5.0以下适配
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private TextView mEdSearch, mTvPrivacy, mTvPagerNum, mTvCloseAll;
    private FrameLayout mRootLayout;
    private ImageButton mIvScanning;
    private RefreshImageView mIvSearch;
    private ContentLoadingProgressBar mFloatSearchProgress;
    private FrameLayout mTabsManagerLayout;
    private TabStackView mTabStackView;
    private ImageView mIvAddPager, mIvForward, mIvBack, mIvMenu, mIvHome,
            mIvSearchSwitch, mIvSearchWebHeaderSwitch, mTvBack, mIvWindow;
    private RelativeLayout mFloatSearchBar;
    private FrameLayout mFlWindowsNum;
    private LinearLayout llexBottomBar, rlSearchMain, mContentWrapper;
    private TabAdapter mTabAdapter;
    private TabController mTabController;
    private View viewDiver;
    private Tab mActiveTab;
    private WebViewFactory mFactory;
    private boolean mIsAnimating = false;
    //是否直接加载链接
    private boolean isLoading;
    private StateView loadingStateView;
    private boolean mIsLoadApk;
    private boolean isPrivacyMode = false;
    private boolean isStopLoading;
    private ValueCallback<Uri[]> uploadMessage;
    private FrameLayout mFrameLayout;
    //视图View
    private View mCustomView;
    // 一个回调接口使用的主机应用程序通知当前页面的自定义视图已被撤职
    private IX5WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int parseCount = 0;
    private ConstraintLayout mRootView;
    private RecyclerView mShelfRecycler;
    private Boolean isFirstStart;
    private long mPreTime;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        initWindow();
        findView();
        initView();
        setListener();
        toWebView();
        onLoadWebUrlEvent();
        getHttpIntent();
        onMainSearchEvent();
    }

    private void onMainSearchEvent() {
        Disposable disposable = RxBus.register(MainSearchEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<MainSearchEvent>() {
                    @Override
                    public void accept(MainSearchEvent mainSearchEvent) throws Exception {
                        setTab(true);
                        switchToMain();
                        if (!TextUtils.isEmpty(mainSearchEvent.getUrl())) {
                            search(mainSearchEvent.getUrl());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogHelper.e("throwable", throwable);
                    }
                });
        addDisposable(disposable);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //设置窗口
    private void toWebView() {
        //初始化tab数据
        mTabController = new TabController(this, this);
        mFactory = new BrowserWebViewFactory(this);
        mTabAdapter = new TabAdapter(this, this);
        mTabStackView.setAdapter(mTabAdapter);
        mTabStackView.setOnChildDismissedListener(this);
        // 先建立一个tab标记主页
        if (mTabController.getTabCount() <= 0) {
            //从数据库中获取窗口链接数据
            List<TabMagBean> arrayList = DbHelper.getDaoSession().getTabMagBeanDao().queryBuilder().list();
            if (arrayList.size() == 0) {
                Tab tab = buildTab();
                mActiveTab = tab;
                mTabController.setActiveTab(mActiveTab);
                //判断本地是否有保存一张网页，一进入首页直接访问
                String history = SettingHelper.getHistoryUrl();
                if (!TextUtils.isEmpty(history)) {
                    search(history);
                    mActiveTab.setMain(false);
                    mActiveTab.setSearch(true);
                    setPageProgress();
                } else {
                    if (SettingHelper.isLoadFiction()) {
                        Disposable disposable = AppUtils.getNetIp()
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<String>() {
                                    @Override
                                    public void accept(String url) throws Exception {
                                        if (!TextUtils.isEmpty(url)) {
                                            search(url);
                                            StorageHelper.saveLoadFiction();
                                            mActiveTab.setMain(false);
                                            mActiveTab.setSearch(true);
                                            setPageProgress();
                                        }
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogHelper.e("error", throwable.getMessage());
                                    }
                                });
                        addDisposable(disposable);
                    }
                }
                //夜间模式切换后回到首页状态设置
                if (SettingHelper.ismIsLoadTabs()) {
                    tab.loadUrl(AppSetting.getInstance().getUrl(), null);
                    switchToTab();
                    setPageProgress();
                    AppSetting.getInstance().setmIsLoadTabs(false);
                }
            } else {
                //启动后有窗口数据新建窗口
                for (int i = 0; i < arrayList.size(); i++) {
                    Tab tab = buildTab();
                    mActiveTab = tab;
                    tab.setMain(false);
                    tab.setSearch(true);
                    tab.setHistoryWindow(true);
                    tab.setmCurrentState(new Tab.PageState(this, arrayList.get(i).getUrl(),
                            arrayList.get(i).getTitle()));
                    if (i == arrayList.size() - 1) {
                        //创建完毕后判断最后一个是否有链接数据，没有就进入首页不加载网页
                        if (TextUtils.isEmpty(tab.getUrl()) || TextUtils.isEmpty(tab.getTitle())) {
                            mActiveTab = tab;
                            mTabController.setActiveTab(tab);
                            switchToMain();
                        } else {
                            mTabController.setActiveTab(tab);
                            //夜间模式切换后回到首页判断，
                            if (!AppSetting.getInstance().ismIsLoadTabs()) {
                                //有链接数据加载网页
                                tab.loadUrl(tab.getUrl(), null);
                                switchToTab();
                                setPageProgress();
                                AppSetting.getInstance().setmIsLoadTabs(false);
                            } else {
                                //加载网页不显示网页，只更新按钮状态
                                tab.loadUrl(tab.getUrl(), null);
                                switchToMain();
                                setPageProgress();
                            }
                            //在浏览网页期间切换夜间模式回到首页有链接就加载该链接
                            if (!TextUtils.isEmpty(AppSetting.getInstance().getUrl())) {
                                search(AppSetting.getInstance().getUrl());
                                setPageProgress();
                                AppSetting.getInstance().setUrl(null);
                            }
                        }
                    }
                }
            }
        }
    }

    private void setListener() {
        //回到首页监听
        mIvHome.setOnClickListener(v -> {
            //更新底部标签
            setTab(true);
            //回到首页
            switchToMain();
            //重新设置标签选中状态
            //夜间模式切换后回到首页重新加载activity,更新setmIsUpdateDelegate状态
            if (SettingHelper.ismIsUpdateDelegate()) {
                recreate();
                AppSetting.getInstance().setmIsUpdateDelegate(false);
            }
        });
        //显示窗口管理
        mFlWindowsNum.setOnClickListener(v1 -> showTabs());
        //从窗口界面返回到首页
        mTvBack.setOnClickListener(v -> {
            hideTabs(true);
            mainSelectTab(mActiveTab);
        });
        //添加窗口
        mIvAddPager.setOnClickListener(v -> addTab(true));
        //显示APP操作菜单
        mIvMenu.setOnClickListener(v -> {
            if (mFloatSearchBar.getVisibility() == View.VISIBLE) {
                MainActivityView.showWebMenu(this, mTabController);
            } else {
                MainActivityView.showWebMenu(this, null);
            }
        });
        //关闭全部窗口
        mTvCloseAll.setOnClickListener(v -> {
            if (mTabController.getTabCount() == 1) {
                hideTabs(true);
            } else {
                int size = mTabController.getTabCount();
                for (int i = 0; i < size; i++) {
                    removeTab(0);
                    //关闭完毕后创建一个窗口
                    if (mTabController.getTabCount() == 0) {
                        addTab(false);
                        switchToMain();
                        if (AppSetting.getInstance().ismIsUpdateDelegate()) {
                            recreate();
                            AppSetting.getInstance().setmIsUpdateDelegate(false);
                        }
                        StorageHelper.saveTabList(null);
                    }
                    hideTabs(true);
                }
                //重新设置状态
                mIvBack.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_idv_off));
                mIvBack.setEnabled(false);
                mIvForward.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_back_off));
                mIvForward.setEnabled(false);
            }
        });
        //点击数据库后全选文本
        mEdSearch.setOnClickListener(view -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_URL, mActiveTab.getUrl());
            startActivityForResult(intent, 1);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
        //点击搜索取消加载中
        mIvSearch.setOnClickListener(v -> {
            mTabController.getCurrentTab().getWebView().reload();
        });


        //浏览网页返回状态设置
        mIvForward.setOnClickListener(view -> {
            mIsLoadApk = false;
            if (loadingStateView != null)
                loadingStateView.showContent();
            if (mActiveTab.isMain()) {
                mActiveTab.setMain(false);
                switchToTab();
                setPageProgress();
            } else {
                if (mActiveTab.getWebView().canGoBack()) {
                    mActiveTab.goBack();
                } else {
                    if (mActiveTab.isCreateWindow() && !mActiveTab.canGoBack()) {
                        mTabController.removeTab(mActiveTab);
                        selectTabs(mTabController.getTab(mTabController.getIndex()), false);
                        updateTabIndex();
                    } else {
                        switchToMain();
                    }
                }
            }
        });

        //选择设置搜索引擎 首页
        mIvSearchSwitch.setOnClickListener(v -> switchSearchPlatform());
        //无痕模式切换
        mTvPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPrivacyMode) {
                    //标准模式
                    AppSetting.getInstance().setPrivacyMode(false);
                    isPrivacyMode = false;
                    mTabController.setPrivacyMode(false);
                    showTabs();
                    mActiveTab = mTabController.getTab(mTabAdapter.getItemCount() - 1);
                    mTabController.setActiveTab(mActiveTab);
                    mTvPrivacy.setText(R.string.isPrivacyMode_true);
                    mIvWindow.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this, R.drawable.ic_wd));
                } else {
                    //无痕模式
                    AppSetting.getInstance().setPrivacyMode(true);
                    isPrivacyMode = true;
                    mTabController.setPrivacyMode(true);
                    List<Tab> tabList = mTabController.getTabs();
                    if (tabList.size() == 0) {
                        addTab(true);
                        hideTabs(true);
                        switchToMain();
                        if (StorageHelper.getPrivacyHint()) {
                            search(Constants.PRIVACY_URL);
                            StorageHelper.savePrivacyHint();
                        }
                    } else {
                        showTabs();
                    }
                    mTvPrivacy.setText(R.string.isPrivacyMode_false);
                    mIvWindow.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this, R.drawable.ic_py_wd));
                }
            }
        });
    }

    //接受现实搜索引擎对话框消息
    private void switchSearchPlatform() {
        Disposable disposable = LocalDataSource.getInstance().query(SearchplBean.class)
                .flatMap(new Function<List<SearchplBean>, ObservableSource<List<SearchplBean>>>() {
                    @Override
                    public ObservableSource<List<SearchplBean>> apply(List<SearchplBean> searchplBeans) throws Exception {
                        Collections.reverse(searchplBeans);
                        return Observable.just(searchplBeans);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<SearchplBean>>() {
                    @Override
                    public void accept(List<SearchplBean> searchPlatforms) throws Exception {
                        showSearchPlatformDialog(searchPlatforms);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogHelper.e("error", throwable.getMessage());
                    }
                });
        addDisposable(disposable);
    }

    //显示搜索引起对话框
    private void showSearchPlatformDialog(List<SearchplBean> searchPlatforms) {
        int checkedItem = 0;
        List<String> sps = new ArrayList<>();
        for (SearchplBean sp : searchPlatforms) {
            sps.add(sp.getName());
            if (AppSetting.getInstance().getDefaultSearch().equals(sp.getUrl())) {
                checkedItem = sps.size() - 1;
            }
        }

        new AlertDialog.Builder(MainCCActivity.this)
                .setSingleChoiceItems(sps.toArray(new String[sps.size()]), checkedItem,
                        (dialog, which) -> {
                            AppSetting.getInstance().setDefaultSearch(searchPlatforms.get(which).getUrl());
//                            AppSetting.getInstance().setSearchSwitch(this, mIvSearchSwitch);
                            dialog.dismiss();
                        })
                .show();
    }


    //加载网页搜索初始化判断
    public void search(String url) {
        if (url.length() == 0) {
            Toast.makeText(MainCCActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
        } else {
            loadWebView(url);
        }
    }

    //加载网页，判断链接数据，isLoading是否显示StateView
    private void loadWebView(String webViewUrl) {
        showLoadingStateView();
        if (webViewUrl.startsWith("http://") ||
                webViewUrl.startsWith("https://") || webViewUrl.startsWith("file://")) {
            mActiveTab.loadUrl(webViewUrl, null);
        } else if (webViewUrl.contains(".com") || webViewUrl.endsWith(".com") ||
                webViewUrl.endsWith(".cn") || webViewUrl.contains(".cn") ||
                webViewUrl.endsWith(".html") || StringUtils.isIP(webViewUrl)) {
            mActiveTab.loadUrl("http://" + webViewUrl, null);
        } else if (mActiveTab.getTitle().endsWith("- 百度")) {
            mEdSearch.setText(mActiveTab.getUrl());
            mActiveTab.loadUrl(AppSetting.getInstance().getDefaultSearch() + webViewUrl, null);
        } else {
            if (this.isLoading) {
                mActiveTab.loadUrl(mActiveTab.getUrl(), null);
            } else {
                mActiveTab.loadUrl(AppSetting.getInstance().getDefaultSearch() + webViewUrl, null);
            }
        }
        mActiveTab.setSearch(true);
        mActiveTab.setMain(false);
        initWindow();
        switchToTab();
    }

    private void initView() {
        //进入搜索界面
        mIvScanning.setOnClickListener(view -> {
            Intent intent = new Intent(this, QrcodeActivity.class);
            startActivityForResult(intent, 1);
        });

    }

    //小说模式更新界面
    //有bug：解析一次html会调用两次这个方法
    /*private void onDomProcessEvent() {
        Observable<DomProcessEvent> register = RxBus.register(DomProcessEvent.class);
        Disposable disposable = register.subscribe(new Consumer<DomProcessEvent>() {
            @Override
            public void accept(DomProcessEvent domProcessEvent) throws Exception {
                Disposable disa = XiaoShuo.getInstance().isXiaoShuoPlus(domProcessEvent.html)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean isXiaoShuo) throws Exception {
                                parseCount++;
                                if (isXiaoShuo && parseCount % 2 == 1) {
                                    XiaoShuo.getInstance().parseHtml(domProcessEvent, mActiveTab.getUrl());
                                    mIvSearchWebHeaderSwitch.setClickable(true);
                                    //点击进入小说模式
                                    mIvSearchWebHeaderSwitch.setOnClickListener(v -> {
                                        XiaoShuo.getInstance().startXsMode(MainCCActivity.this);
                                        XiaoShuo.getInstance().setCurrentUrl(mTabController.getCurrentWebView().getUrl(),
                                                mTabController.getCurrentWebView().getTitle());
                                    });
                                    //更新数据和图标
                                    mIvSearchWebHeaderSwitch.setImageResource(R.drawable.ic_book);

                                    //自动进入阅读模式
                                    XiaoShuo.getInstance().startXsMode(MainCCActivity.this);
                                    XiaoShuo.getInstance().setCurrentUrl(mTabController.getCurrentWebView().getUrl(),
                                            mTabController.getCurrentWebView().getTitle());
                                }
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                LogHelper.e(TAG, throwable);
                            }
                        });
                addDisposable(disa);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                LogHelper.e(TAG, throwable);
            }
        });
        addDisposable(disposable);
    }*/

    //退出程序
    private void exit() {
        switch (SettingHelper.getExitType()) {
            case 0:
            default:
                DialogUtils.exitAppDialog(this);
                break;
            case 1:
                long currentTime = new Date().getTime();
                if ((currentTime - mPreTime) > TWO_SECOND) {
                    RxBus.post(new RefreshPostEvent(Constants.RECOMMENDED_ARTICLE));
                    mPreTime = currentTime;
                    ToastUtils.show("再次点击退出");
                } else {
                    if (StorageHelper.getExitWithHistory()) {//删除历史记录
                        LocalDataSource.getInstance().delete(WebHistoryBean.class);
                    }
                    if (mTabController.getTabCount() == 1) {
                        StorageHelper.saveTabList(null);
                    }
                    finish();
                    ResidentNotificationHelper.showNotice(MainCCActivity.this);
                }
                break;
        }
    }

    /**
     * 进入页面管理界面，用动画改变选择页（可以理解为一张截图）的Y和scale
     */
    public void showTabs() {
        if (isAnimating()) {
            return;
        }
        List<Tab> tabList = mTabController.getTabs();
        mTabAdapter.updateData(tabList);
        mTabsManagerLayout.bringToFront();
        mTabsManagerLayout.setVisibility(View.VISIBLE);
        llexBottomBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.windowBlackL, null));
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }
        mTabStackView.animateShow(mTabController.getTabPosition(mActiveTab) == 0 ? mTabAdapter.getItemCount() - 1
                        : mTabController.getTabPosition(mActiveTab),
                mContentWrapper, mTabsManagerLayout, true, () -> {
                    mContentWrapper.setVisibility(View.GONE);
                });
        View selectedChild = mTabStackView.getSelectedChild();
        if (selectedChild != null) {
            TabCard card = selectedChild.findViewById(R.id.ucTabCard);
            card.active(true, 100, 40, null);
        }
        mActiveTab.capture();
    }

    //接收加载网页消息
    private void onLoadWebUrlEvent() {
        Disposable disposable = RxBus.register(LoadWebUrlEvent.class)
                .subscribe(loadWebUrlEvent -> {
                    String webUal = loadWebUrlEvent.getUrl();
                    isLoading = false;
                    mEdSearch.setText(webUal);
                    search(webUal);
                    switchToTab();
                    setTab(true);
                });
        addDisposable(disposable);
    }


    /**
     * @param animate 是否有动画，有动画时即UCRootView从下往上移
     */
    private void addTab(boolean animate) {
        if (mTabController.getTabCount() == getResources().getInteger(R.integer.max_tab_count)) {
            Toast.makeText(MainCCActivity.this, "已达到最大值", Toast.LENGTH_SHORT).show();
            return;
        }
        Tab tab = buildTab();
        mActiveTab = tab;
        mTabController.setActiveTab(mActiveTab);
        mTabController.setIndex(mTabController.getTabPosition(mActiveTab));
        if (animate) {
            mContentWrapper.bringToFront();
            mTabsManagerLayout.setVisibility(View.GONE);
            llexBottomBar.setVisibility(View.VISIBLE);
            animateShowFromBottomToTop(mContentWrapper, () -> {
                hideTabs(false); // 把页面管理页隐藏
                initWindow(); // 更改状态栏颜色
            });
            switchToMain();
        }
        //保存网页链接
        saveHistory();
        mIvBack.setImageDrawable(ContextCompat
                .getDrawable(MainCCActivity.this, R.drawable.ic_idv_off));
        mIvBack.setEnabled(false);
        mIvForward.setImageDrawable(ContextCompat
                .getDrawable(MainCCActivity.this, R.drawable.ic_back_off));
        mIvForward.setEnabled(false);
    }

    //设置添加窗口动画
    public void animateShowFromBottomToTop(View view, final Runnable onCompleteRunnable) {
        mContentWrapper.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(
                view,
                "translationY",
                DensityUtil.getDisplayPoint(this).y,
                0);
        animator.setDuration(100);
        animator.start();
        mIsAnimating = true;
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
                if (onCompleteRunnable != null) {
                    onCompleteRunnable.run();
                }
            }
        });
    }

    //删除窗口数据
    private void removeTab(int index) {
        mTabController.removeTab(index);
    }

    /**
     * 新建一页并初始化页面，这里的ICON为网站图标，我没资源先用UC代替，title为网站title，key为定义的页面ID
     * previewBitmap 为页面的截图
     * 当新建一页时，让我们的选择页置为新建的这一页，然后下次进入页面管理页时初始化进度
     *
     * @return
     */
    private Tab buildTab() {
        return mTabController.createNewTab();
    }

    //窗口管理点击返回后界面处理
    public void hideTabs(boolean animated) {
        if (isAnimating()) {
            return;
        }
        if (animated) {
            mTabStackView.animateShow(mTabController.getTabPosition(mActiveTab),
                    mContentWrapper, mTabsManagerLayout, false, new Runnable() {
                        @Override
                        public void run() {
                            mTabsManagerLayout.setVisibility(View.GONE);
                            llexBottomBar.setVisibility(View.VISIBLE);
                            initWindow();
                        }
                    });
            View selectedChild = mTabStackView.getSelectedChild();
            if (selectedChild != null) {
                TabCard card = selectedChild.findViewById(R.id.ucTabCard);
                card.active(false, 100, 40, null);
            }
        } else {
            initWindow();
        }
        mContentWrapper.setVisibility(View.VISIBLE);
    }

    //加载网页时，显示状态栏
    private void switchToTab() {
//        if (mContentWrapper.getParent() != null) {
//            ((FrameLayout) mContentWrapper.getParent()).removeView(mContentWrapper);
//        }

        mFloatSearchBar.setVisibility(View.VISIBLE);


        ExplorerWebView view = mActiveTab.getWebView();
        if (view != null && view.getParent() == null) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (lp == null) {
                lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
            }
            lp.topMargin = getResources().getDimensionPixelSize(R.dimen.dimen_48dp);
            lp.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dimen_48dp);
            mContentWrapper.removeView(mRootView);
            mContentWrapper.addView(view, lp);
            if (mActiveTab.getWebView().getUrl() != null) {
                if (StorageHelper.getWebRefresh() &&
                        !mActiveTab.getWebView().getUrl().contains(Constants.TOUTIAO_URL)) {
                    mSwipeRefresh.setEnabled(true);
                }
            }
            view.setOnScrollListener(new ExplorerWebView.IScrollListener() {
                @Override
                public void onScrollChanged(int scrollY) {
                    if (scrollY == 0) {
                        if (StorageHelper.getWebRefresh() &&
                                !mActiveTab.getWebView().getUrl().contains(Constants.TOUTIAO_URL)) {
                            mSwipeRefresh.setEnabled(true);
                        }
                    } else {
                        mSwipeRefresh.setEnabled(false);
                    }
                }
            });
        }
        mSwipeRefresh.setEnabled(false);
    }

    //回到首页后的界面更新，隐藏状态栏隐藏webview

    private void switchToMain() {
        mIsLoadApk = false;
        if (mTabController != null && mActiveTab != null && mTabController.getTabCount() >= 1)
            mActiveTab.setMain(true);
        if (mRootView.getParent() == null) {
            mContentWrapper.addView(mRootView);
        }
        ExplorerWebView view = mActiveTab.getWebView();
        if (view != null) {
            mContentWrapper.removeView(view);
        }
        PreferencesHelper.getInstance().remove("historyUrl");
        mFloatSearchBar.setVisibility(View.GONE);
        status();
        if (loadingStateView != null)
            loadingStateView.showContent();
        mActiveTab.setMain(true);
        initWindow();
    }

    //更新网页前进后退按钮状态
    private void status() {
        if (mActiveTab != null) {
            if (mActiveTab.isSearch() && mActiveTab.isMain()) {
                if (TextUtils.isEmpty(mActiveTab.getUrl())) {
                    mIvBack.setImageDrawable(ContextCompat
                            .getDrawable(MainCCActivity.this, R.drawable.ic_idv_off));
                    mIvBack.setEnabled(false);
                } else {
                    mIvBack.setImageDrawable(ContextCompat
                            .getDrawable(MainCCActivity.this, R.drawable.ic_idv_on));
                    mIvBack.setEnabled(true);
                }
                mIvForward.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_back_off));
                mIvForward.setEnabled(false);
            }
        }
    }

    //窗口管理页关闭窗口
    private void onTabClosed(int index) {
        mIsLoadApk = false;
        removeTab(index);
        if (mTabStackView.getChildCount() <= 0) {
            addTab(true);
        } else {
            mActiveTab = mTabController.getCurrentTab();
            mTabController.setActiveTab(mActiveTab);
        }
    }

    public boolean isAnimating() {
        return mTabStackView.isAnimating() || mIsAnimating;
    }

    //系统状态栏设置
    private void initWindow() {
        isFirstStart = PreferencesHelper.getInstance().getBoolean("isFirstStart", true);
        PreferencesHelper.getInstance().putBoolean("isFirstStart", false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            if (requestCode == 1) {
                //扫一扫加载网页
                isLoading = false;
                String webUal = data.getStringExtra("webUrl");
                mEdSearch.setText(webUal);
                switchToTab();
                setTab(true);
                loadWebView(webUal);
            }
        }
        //网页上传图片回调
        if (requestCode == REQUEST_CODE_CHOOSE) {
            if (uploadMessage != null) {
                onActivityResultAboveL(resultCode, data);
            }
        }
    }

    //处理网页回调
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int resultCode, Intent intent) {
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessage.onReceiveValue(results);
        uploadMessage = null;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public TabController getTabController() {
        return mTabController;
    }

    @Override
    public WebViewFactory getWebViewFactory() {
        return mFactory;
    }

    @Override
    public void onSetWebView(Tab tab, WebView webView) {
        webView.addJavascriptInterface(new DomJavascriptInterface(), XiaoShuo.NAME_JS_DOM_PROCESS);
    }

    private void findView() {
        llexBottomBar = (LinearLayout) findViewById(R.id.llBottomBar);
        mRootLayout = (FrameLayout) findViewById(R.id.rootLayout);
        mFrameLayout = (FrameLayout) findViewById(R.id.fl_video_root);
        viewDiver = (View) findViewById(R.id.view_diver);
        mEdSearch = (TextView) findViewById(R.id.ed_web_search);
        mTvPrivacy = (TextView) findViewById(R.id.tv_privacy);
        mContentWrapper = (LinearLayout) findViewById(R.id.homeContentWrapper);
        mRootView = findViewById(R.id.root_view);
        mIvSearch = (RefreshImageView) findViewById(R.id.btn_search);
        mFloatSearchProgress = (ContentLoadingProgressBar) findViewById(R.id.progress_bar);
        mTabsManagerLayout = (FrameLayout) findViewById(R.id.flPagersManager);
        mTabStackView = (TabStackView) findViewById(R.id.ucStackView);
        mIvAddPager = (ImageView) findViewById(R.id.ivAddPager);
        mTvBack = findViewById(R.id.ivBack);
        mFloatSearchBar = (RelativeLayout) findViewById(R.id.floatSearchBar);
        mIvForward = (ImageView) findViewById(R.id.ivForward);
        mIvBack = (ImageView) findViewById(R.id.ivBack);
        mIvWindow = (ImageView) findViewById(R.id.iv_window);
        mIvMenu = (ImageView) findViewById(R.id.ivMore);
        mFlWindowsNum = (FrameLayout) findViewById(R.id.flWindowsNum);
        mTvPagerNum = (TextView) findViewById(R.id.tvPagerNum);
        mIvHome = (ImageView) findViewById(R.id.ivHome);
        mTvCloseAll = (TextView) findViewById(R.id.tv_close_all);
        mIvSearchSwitch = findViewById(R.id.iv_main_search);
        mIvSearchWebHeaderSwitch = findViewById(R.id.iv_search_web_header);
        rlSearchMain = (LinearLayout) findViewById(R.id.rl_search_main);
        mIvScanning = (ImageButton) findViewById(R.id.iv_scanning);
        mIvForward.setEnabled(false);
        setTab(true);
        rlSearchMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainCCActivity.this, SearchActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    //保存网页浏览记录
    private void saveHistory() {
        if (isPrivacyMode) {
            return;
        }
        List<Tab> tabs = mTabController.getTabs();
        if (tabs.size() != 0) {
            List<TabMagBean> tabMagBeans = new ArrayList<>();
            for (int i = 0; i < tabs.size(); i++) {
                TabMagBean tab = new TabMagBean(tabs.get(i).getTitle(), tabs.get(i).getUrl());
                tabMagBeans.add(tab);
                if (i == tabs.size() - 1) {
                    StorageHelper.saveTabList(tabMagBeans);
                }
            }
        }
        if (tabs.size() == 1) {
            StorageHelper.saveTabList(null);
        }
    }

    private void setPageProgress() {
        //更新前后按钮
        if (mActiveTab != null) {
            if (mActiveTab.canGoForward()) {
                mIvBack.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_idv_on));
                mIvBack.setEnabled(true);
            } else {
                mIvBack.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_idv_off));
                mIvBack.setEnabled(false);
            }
            if (mActiveTab.getWebView() == null) return;
            if (mActiveTab.getWebView().canGoBack() || mActiveTab.isSearch()) {
                mIvForward.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_back_on));
                mIvForward.setEnabled(true);
            } else {
                mIvForward.setImageDrawable(ContextCompat
                        .getDrawable(MainCCActivity.this, R.drawable.ic_back_off));
                mIvForward.setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        WebView newWebView = new WebView(view.getContext());
        newWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mTabController.setIndex(mTabController.getTabPosition(mActiveTab));
                addCreateWindow();
                mActiveTab.loadUrl(url, null);
                switchToTab();
                return true;
            }
        });
        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();
        return true;
    }

    private void addCreateWindow() {
        if (mTabController.getTabCount() == getResources().getInteger(R.integer.max_tab_count)) {
            Toast.makeText(MainCCActivity.this, "已达到最大值", Toast.LENGTH_SHORT).show();
            return;
        }
        Tab tab = buildTab();
        mActiveTab = tab;
        mTabController.setActiveTab(mActiveTab);
        mActiveTab.setMain(false);
        mActiveTab.setSearch(true);
        mActiveTab.setCreateWindow(true);
        mContentWrapper.bringToFront();
        mTabsManagerLayout.setVisibility(View.GONE);
        llexBottomBar.setVisibility(View.VISIBLE);
        animateShowFromBottomToTop(mContentWrapper, () -> {
            hideTabs(false); // 把页面管理页隐藏
            initWindow(); // 更改状态栏颜色
        });
        saveHistory();
        setPageProgress();
    }

    @Override
    public void newWindow(String url) {
        mTabController.setIndex(mTabController.getTabPosition(mActiveTab));
        addCreateWindow();
        mActiveTab.loadUrl(url, null);
        switchToTab();
    }

    @Override
    public void onProgressChanged(Tab tab) {
        if (tab.getPageLoadProgress() >= 100) {
            if (loadingStateView != null && !mIsLoadApk) {
                loadingStateView.showContent();
            }
        }
        mFloatSearchProgress.setProgress(tab.getPageLoadProgress());
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        mEdSearch.setText(title);
        saveWebUrlHistory(view, null);
    }

    @Override
    public void onFavicon(Tab tab, WebView view, Bitmap icon) {
        if (!isPrivacyMode) {
            List<WebHistoryBean> histories = new LocalDataSource().getLiteOrm().query(WebHistoryBean.class);
            if (histories.size() != 0) {
                for (int i = 0; i < histories.size(); i++) {
                    if (histories.get(i).getWebUrl().equals(view.getUrl())) {
                        //已经保持过的关键字数量加1
                        ColumnsValue columnsValue = new ColumnsValue(new String[]{"icon"},
                                new Object[]{Utils.bitmapToString(icon)});
                        LocalDataSource.getInstance()
                                .getLiteOrm()
                                .update(WhereBuilder.create(WebHistoryBean.class)
                                                .where("id= ?", histories.get(i).getId()),
                                        columnsValue, ConflictAlgorithm.None);
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        return true;
    }

    @Override
    public void onPageStarted(Tab tab, WebView webView, Bitmap favicon) {
        if (webView.getTitle().equals("")) {
            mEdSearch.setText(tab.getUrl());
        } else {
            mEdSearch.setText(tab.getTitle());
        }
        mFloatSearchProgress.setVisibility(View.VISIBLE);
        isLoading = false;
        mIvSearch.setBackgroundColor(ContextCompat.getColor(MainCCActivity.this, R.color.web_text));
        mIvSearch.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this,
                R.drawable.ic__sc_delete));
        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStopLoading) {
                    webView.reload();
                    mIvSearch.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this,
                            R.drawable.ic__sc_delete));
                    isStopLoading = false;
                } else {
                    webView.stopLoading();
                    mIvSearch.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this,
                            R.drawable.ic_refresh));
                    isStopLoading = true;
                }
            }
        });
        if (tab.getUrl().startsWith("https://")) {
            mIvSearchWebHeaderSwitch.setImageResource(R.drawable.ic_https);
        } else {
            mIvSearchWebHeaderSwitch.setImageResource(R.drawable.ic_internet);
        }
        //XiaoShuo.getInstance().onStart();
    }

    //网页加载完成
    @Override
    public void onPageFinished(Tab tab, WebView view, String url) {
        //下拉刷新状态改变
        //隐藏进度条
        mFloatSearchProgress.setVisibility(View.INVISIBLE);
        //刷新窗口适配器
        mTabAdapter.notifyDataSetChanged();
        //获取网页截图
        if (mTabsManagerLayout.getVisibility() == View.GONE) {
            tab.shouldUpdateThumbnail(true);
        }
        isLoading = true;
        if (view.getTitle().equals("")) {
            mEdSearch.setText(tab.getUrl());
        } else {
            mEdSearch.setText(tab.getTitle());
        }
        mIvSearch.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this,
                R.drawable.ic_refresh));

        mIvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTabController.getCurrentTab().getWebView().reload();
                mIvSearch.setImageDrawable(ContextCompat.getDrawable(MainCCActivity.this,
                        R.drawable.ic__sc_delete));
                isStopLoading = false;
            }
        });
        setPageProgress();
        if (!isPrivacyMode) {
            if (view.getUrl() != null) {
                if (!view.getUrl().equals(Constants.APP_UPDATE_URL_WEB)) {
                    StorageHelper.saveHistory(view.getUrl());
                }
            }
        }
        saveHistory();

        if (loadingStateView != null && !mIsLoadApk) {
            loadingStateView.showContent();
        }

        // 传递 html dom
        // 如果是百度/搜狗这些搜索引擎则忽略
        if (url.contains("baidu.com") || url.contains("sogou.com") || url.contains("so.com") ||
                url.contains("sm.cn") || url.contains("bing.com") || url.contains("taobao.com") ||
                url.contains("google.com") || url.contains("yz.m.sm.cn")) {
            return; // pass 下面的代码
        }
        String func = getResources().getString(R.string.get_dom);
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
    public void onLoadResource(WebView webView, String s) {
        if (webView.getTitle() == null) {
            mEdSearch.setText(webView.getUrl());
        }
    }

    @Override
    public void onReceivedError(WebView webView, int i, String s, String s1) {
        mEdSearch.setText(webView.getUrl());
        if (loadingStateView != null)
            loadingStateView.showContent();
        mIsLoadApk = false;
    }

    @Override
    public void onWebsiteIconClicked(String url) {
        mEdSearch.setText(url);
        search(url);
    }

    @Override
    public void selectTab(Tab tab) {
        selectTabs(tab, true);
    }

    private void selectTabs(Tab tab, boolean stackView) {
        mIsLoadApk = false;
        if (isAnimating()) {
            return;
        }
        int index = mTabController.getTabPosition(tab);
        mActiveTab = tab;
        if (mActiveTab == null) {
            return;
        }
        if (mActiveTab.getTitle() == null) {
            mEdSearch.setText(mActiveTab.getUrl());
        } else {
            mEdSearch.setText(mActiveTab.getTitle());
        }
        mTabController.setActiveTab(tab);
        if (!TextUtils.isEmpty(tab.getUrl()) && tab.isHistoryWindow()) {
            mActiveTab.setHistoryWindow(false);
            tab.loadUrl(tab.getUrl(), null);
            tab.updateShouldCaptureThumbnails();
        }
        if (tab.checkUrlNotNull()) {
            switchToTab();
        } else {
            switchToMain();
        }
        View selectedChild = mTabStackView.getSelectedChild();
        if (selectedChild != null) {
            TabCard card = selectedChild.findViewById(R.id.ucTabCard);
            card.active(false, 100, 40, null);
        }
        if (stackView) {
            mTabStackView.selectTab(index, () -> {
                llexBottomBar.setVisibility(View.VISIBLE);
                mContentWrapper.setVisibility(View.VISIBLE);
                mTabsManagerLayout.setVisibility(View.GONE);
                initWindow();
            });
        } else {
            llexBottomBar.setVisibility(View.VISIBLE);
            mContentWrapper.setVisibility(View.VISIBLE);
            mTabsManagerLayout.setVisibility(View.GONE);
            initWindow();
        }
        status();
        setPageProgress();
        if (mActiveTab.isMain()) {
            switchToMain();
        }
    }

    public void mainSelectTab(Tab tab) {
        int index = mTabController.getTabPosition(tab);
        mActiveTab = tab;
        mTabController.setActiveTab(mActiveTab);
        if (mActiveTab.getTitle() == null) {
            mEdSearch.setText(mActiveTab.getUrl());
        } else {
            mEdSearch.setText(mActiveTab.getTitle());
        }
        if (!TextUtils.isEmpty(tab.getUrl()) && tab.isHistoryWindow()) {
            mActiveTab.setHistoryWindow(false);
            tab.loadUrl(tab.getUrl(), null);
            tab.updateShouldCaptureThumbnails();
        }
        if (mActiveTab.checkUrlNotNull()) {
            switchToTab();
        } else {
            switchToMain();
        }
        View selectedChild = mTabStackView.getSelectedChild();
        if (selectedChild != null) {
            TabCard card = selectedChild.findViewById(R.id.ucTabCard);
            card.active(false, 100, 40, null);
        }
        status();
        setPageProgress();
        if (mActiveTab.isMain()) {
            switchToMain();
        }
        mTabStackView.selectTab(index, () -> {
            mContentWrapper.setVisibility(View.VISIBLE);
            mTabsManagerLayout.setVisibility(View.GONE);
            llexBottomBar.setVisibility(View.VISIBLE);
            initWindow();
        });
    }

    private void saveWebUrlHistory(WebView view, Bitmap icon) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String historyTime = format.format(new Date(System.currentTimeMillis()));
        boolean isExist = false;
        List<WebHistoryBean> histories = DbHelper.getDaoSession().getWebHistoryBeanDao().queryBuilder().list();
        if (histories.size() != 0) {
            for (int i = 0; i < histories.size(); i++) {
                if (histories.get(i).getWebUrl().equals(view.getUrl())) {
                    isExist = true;
                }
            }
        }
        if (!isExist) {
            DbHelper.getDaoSession().getWebHistoryBeanDao()
                    .save(new WebHistoryBean(historyTime, view.getTitle(), view.getUrl(),
                            icon == null ? "" : Utils.bitmapToString(icon)));
        }
    }

    @Override
    public void closeTab(Tab tab) {
        mIsLoadApk = false;
        mTabStackView.closeTab(mTabController.getTabPosition(tab));
        saveHistory();
        updateTabIndex();
    }

    @Override
    public void onChildDismissed(int index) {
        onTabClosed(index);
        updateTabIndex();
    }

    @Override
    public void onTabCountChanged() {
        mTvPagerNum.setText("" + mTabController.getTabCount()); // 更新页面数量
    }

    @Override
    public void onTabDataChanged(Tab tab) {
        mTabAdapter.notifyDataSetChanged();
    }

    //进入图片选择，赋值valueCallback
    @Override
    public void uploadMessageFile(ValueCallback<Uri[]> valueCallback) {
        uploadMessage = valueCallback;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), REQUEST_CODE_CHOOSE);
    }

    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        //赋值，关闭时需要调用
        mCustomView = view;
        // 将video放到FrameLayout中
        mFrameLayout.addView(mCustomView);
        //  退出全屏模式时释放需要调用，这里先赋值
        mCustomViewCallback = callback;
        // 设置webView隐藏
        mActiveTab.getWebView().setVisibility(View.GONE);
        //切换至横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public void onHideCustomView() {
        //显示竖屏时候的webview
        mActiveTab.getWebView().setVisibility(View.VISIBLE);
        if (mCustomView == null) {
            return;
        }
        //隐藏
        mCustomView.setVisibility(View.GONE);
        //从当前视图中移除
        mFrameLayout.removeView(mCustomView);
        //释放自定义视图
        mCustomViewCallback.onCustomViewHidden();
        mCustomView = null;
        //切换至竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //更新Tab状态
    private void setTab(boolean isPost) {
        if (isPost) {
            mIvBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_idv_off));
            mIvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsLoadApk = false;
                    if (loadingStateView != null)
                        loadingStateView.showContent();
                    if (mActiveTab.isMain()) {
                        mActiveTab.setMain(false);
                        switchToTab();
                        setPageProgress();
                    } else {
                        mActiveTab.goForward();
                        mIvBack.setEnabled(true);
                    }
                }
            });
        } else {
            mIvBack.setEnabled(true);
            mIvBack.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh));
            mIvBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation rotate = new RotateAnimation(0f, 359f,
                            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    LinearInterpolator lin = new LinearInterpolator();
                    rotate.setInterpolator(lin); //设置插值器
                    rotate.setDuration(500);//设置动画持续周期
                    rotate.setRepeatCount(0);//设置重复次数
                    rotate.setFillAfter(true);//动画执行完后是否停留在执行完的状态
                    //        rotate.setStartOffset(1000);//执行前的等待时间  单位ms
                    mIvBack.setAnimation(rotate);
                    mIvBack.startAnimation(rotate);
                    RxBus.post(new RefreshPostEvent(Constants.RECOMMENDED_ARTICLE));
                }
            });
        }
    }


    //更新网页UA保存状态
    private void onFontSizeChangeEvent() {
        Disposable disposable = RxBus.register(OnFontSizeChangeEvent.class)
                .subscribe(new Consumer<OnFontSizeChangeEvent>() {
                    @Override
                    public void accept(OnFontSizeChangeEvent onFontSizeChangeEvent) throws Exception {
                        Log.e("jh", "uc change " + onFontSizeChangeEvent);
                        WebSettings settings = mActiveTab.getWebView().getSettings();
                        switch (AppSetting.getInstance().ismIsUaCheck()) {
                            case 0:
                                //android
                                settings.setUserAgentString(PreferencesHelper.getInstance().getString("ua_str_android", ""));
                                settings.setUseWideViewPort(false);
                                break;
                            case 1:
                                //电脑
                                settings.setUserAgentString(PreferencesHelper.getInstance().getString("ua_str_pc", ""));
                                settings.setUseWideViewPort(true);
                                break;
                            case 2:
                                //ios
                                settings.setUserAgentString(PreferencesHelper.getInstance().getString("ua_str_ios", ""));
                                settings.setUseWideViewPort(false);
                                break;
                        }
                        if (!TextUtils.isEmpty(mActiveTab.getUrl()) && mFloatSearchBar.getVisibility() == View.VISIBLE) {
                            loadWebView(mActiveTab.getUrl());
                        }
                        for (Tab tab : mTabController.getTabs()) {
                            tab.getWebView().getSettings()
                                    .setTextZoom(AppSetting.getInstance().getFontSize());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
        addDisposable(disposable);
    }

    @Override
    public void onDownloadListener(String url) {
        mEdSearch.setText(url);
        mIsLoadApk = true;
        if (url.contains(Constants.BAIDU_APK_LOADING)) {
            showLoadingStateView();
        } else {
            if (loadingStateView != null)
                loadingStateView.showContent();
        }
        //MainActivityView.showDownLoadPopupWindow(this, mRootLayout, url, getCompositeDisposable());
    }

    //屏幕旋转监听
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        switch (config.orientation) {
            //竖屏方向
            case Configuration.ORIENTATION_LANDSCAPE:
                //设置全屏
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                break;
            //横屏方向
            case Configuration.ORIENTATION_PORTRAIT:
                //关闭全屏
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mTabsManagerLayout.getVisibility() == View.VISIBLE) {
            hideTabs(true);
            mainSelectTab(mActiveTab);
        } else if (mFloatSearchBar.getVisibility() == View.GONE) {
            exit();
        } else if (mActiveTab.isMain()) {
            mActiveTab.setMain(false);
            switchToTab();
            setPageProgress();
        } else if (mActiveTab.getWebView().canGoBack()) {
            mActiveTab.goBack();
            if (loadingStateView != null)
                loadingStateView.showContent();
            mIsLoadApk = false;
        } else {
            switchToMain();
            if (AppSetting.getInstance().ismIsUpdateDelegate()) {
                recreate();
                AppSetting.getInstance().setmIsUpdateDelegate(false);
            }
            if (mActiveTab.isCreateWindow() && !mActiveTab.canGoBack()) {
                if (mTabController.getTab(mTabController.getIndex()) != null) {
                    mTabController.removeTab(mActiveTab);
                    updateTabIndex();
                } else {
                    switchToMain();
                }
            }
        }
    }

    //更新下标
    public void updateTabIndex() {
        selectTabs(mTabController.getTab(mTabController.getIndex()), false);
        int index = mTabController.getTabPosition(mActiveTab);
        mTabController.setIndex(index - 1);
        if (mActiveTab.getTitle() == null) {
            mEdSearch.setText(mActiveTab.getUrl());
        } else {
            mEdSearch.setText(mActiveTab.getTitle());
        }
        if (!isPrivacyMode) {
            List<TabMagBean> tabMagBeans = DbHelper.getDaoSession().getTabMagBeanDao().queryBuilder().list();
            if (tabMagBeans.size() != 0 && index != 0) {
                tabMagBeans.remove(index);
                SettingHelper.saveTabList(tabMagBeans);
                DbHelper.getDaoSession().getTabMagBeanDao().deleteAll();
                DbHelper.getDaoSession().getTabMagBeanDao().insert(tabMagBeans.get(index));
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //重新创建之后执行
    }

    public void getHttpIntent() {
        //其他应用打开浏览器访问网址
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if (uri != null) {
            String url = uri.getScheme() + "://" + uri.getHost() + uri.getPath() + "?" + uri.getQuery();
            if (!TextUtils.isEmpty(url)) {
                isLoading = false;
                mEdSearch.setText(url);
                search(url);
                if (loadingStateView != null)
                    loadingStateView.showContent();
            }
        }
        //快捷方式桌面进入
        String url = getIntent().getStringExtra("webViewUrl");
        if (!TextUtils.isEmpty(url)) {
            isLoading = false;
            mEdSearch.setText(url);
            loadWebView(url);
        }

        String toSearchView = getIntent().getStringExtra("toSearchView");
        if (toSearchView != null) {
            if (toSearchView.equals("toSearchView")) {
                startActivityForResult(new Intent(MainCCActivity.this, SearchActivity.class), 1);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //检查关闭所有网页
        for (int i = 0; i < mTabController.getTabCount(); i++) {
            if (mTabController.getTab(i) != null && mTabController.getTab(i).getWebView() != null) {
                ViewGroup parent = (ViewGroup) mTabController.getTab(i).getWebView().getParent();
                if (parent != null) {
                    parent.removeView(mTabController.getTab(i).getWebView());
                }
                mTabController.getTab(i).getWebView().removeAllViews();
                mTabController.getTab(i).getWebView().clearHistory();
                mTabController.getTab(i).getWebView().loadUrl("about:blank");
                mTabController.getTab(i).getWebView().stopLoading();
                mTabController.getTab(i).getWebView().setWebChromeClient(null);
                mTabController.getTab(i).getWebView().setWebViewClient(null);
                mTabController.getTab(i).getWebView().destroy();
            }
        }
    }


    //覆盖网页View
    private void showLoadingStateView() {
        if (loadingStateView == null) {
            if (mRootLayout.getHeight() > 0 && mFloatSearchBar.getHeight() > 0) {
                ViewGroup parent = mRootLayout;
                loadingStateView = new StateView(parent.getContext());
                loadingStateView.setLoadingResource(R.layout.loading_layout);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                params.topMargin = mFloatSearchBar.getHeight();
                params.bottomMargin = mFlWindowsNum.getHeight() + viewDiver.getHeight();
                parent.addView(loadingStateView, params);
                loadingStateView.showLoading();
            }
        } else {
            loadingStateView.showLoading();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        for (int i = 0; i < mTabController.getTabCount(); i++) {
            if (mTabController.getTab(i) != null && mTabController.getTab(i).getWebView() != null) {
                mTabController.getTab(i).getWebView().onPause();
                mTabController.getTab(i).getWebView().pauseTimers();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onFontSizeChangeEvent();
        for (int i = 0; i < mTabController.getTabCount(); i++) {
            if (mTabController.getTab(i) != null && mTabController.getTab(i).getWebView() != null) {
                mTabController.getTab(i).getWebView().onResume();
                mTabController.getTab(i).getWebView().resumeTimers();
            }
        }
    }

    //小说模式js交互
    private static class DomJavascriptInterface {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @JavascriptInterface
        public void processDom(String host, String html) {
            RxBus.post(new DomProcessEvent(host, html));
        }

        @JavascriptInterface
        public void toHome() {
            RxBus.post(new MainSearchEvent(""));
        }
    }

}
