package com.kunfei.bookshelf.view.activity.browser;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseBrowserActivity;
import com.kunfei.bookshelf.bean.SearchHistoryBean;
import com.kunfei.bookshelf.bean.SearchplBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.SettingHelper;
import com.kunfei.bookshelf.utils.GlideUtils;
import com.kunfei.bookshelf.utils.LogHelper;
import com.kunfei.bookshelf.utils.SoftInputUtil;
import com.kunfei.bookshelf.utils.StringUtils;
import com.kunfei.bookshelf.view.web.SearchTipsGroupView;
import com.kunfei.bookshelf.view.web.XRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class SearchActivity extends BaseBrowserActivity implements SearchTipsGroupView.OnItemClick {

    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_KEY_WORDS = "extra_key_words";
    private static final String TAG = LogHelper.makeLogTag(SearchActivity.class);
    private static RecyclerAdapter<Object> mRecyclerAdapter;
    private static String changeStr = "";
    private static boolean isSearch;
    private AppCompatEditText mEdSearch;
    private Button mBtnSearch;
    private ImageView mIvSearchSwitch;
    private XRecyclerView xRecyclerView;
    private RelativeLayout mRlHeadRoot;
    private TextView mTvDeleteHistory;
    private CharSequence edText = "";
    private ImageView mIvDelete;
    private RelativeLayout rlCopyContext;
    private TextView tvContext;
    private RelativeLayout mRlHeadHotSearch;
    private SearchTipsGroupView mSearchTips;
    //搜索联想列表点击事件
    private OnTipsItemListener mOnTipsItemListener = new OnTipsItemListener() {
        @Override
        public void onItemClick(String url, String wd) {
            saveSearchHistoryBeanIfNotExist(wd);
            SoftInputUtil.hideIMM(mEdSearch);

            Intent intent = new Intent();
            intent.putExtra("webUrl", url);
            setResult(2, intent);
            finish();
        }
    };

    //制定位置修改颜色
    private static void setChangeTextView(String name, String changeStr, TextView textView) {
        if (name != null && name.contains(changeStr)) {
            int index = name.indexOf(changeStr);
            int len = changeStr.length();
            Spanned temp = Html.fromHtml(name.substring(0, index)
                    + "<font color=#4DA1EA>"
                    + name.substring(index, index + len) + "</font>"
                    + name.substring(index + len, name.length()));
            textView.setText(temp);
        } else {
            textView.setText(name);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initView();
        setHistoryView();
        setListener();
        setExtraUrl();
        setCopyText();
    }

    //显示粘贴的内容
    private void setCopyText() {
        String text = StringUtils.getCopyText(this);
        if (text == null) {
            return;
        }
        if (!TextUtils.isEmpty(text) && !text.equals("") && !StorageHelper.getCopyText().equals(text)) {
            rlCopyContext.setVisibility(View.VISIBLE);
            tvContext.setText(text);
            StorageHelper.saveCopyText(text);
        }
    }

    private void initView() {
        setToolbar("搜索");
        rlCopyContext = (RelativeLayout) findViewById(R.id.rl_copy_context);
        tvContext = (TextView) findViewById(R.id.tv_context);
        mIvDelete = (ImageView) findViewById(R.id.iv_delete);
        mEdSearch = (AppCompatEditText) findViewById(R.id.ed_search);
        mBtnSearch = (Button) findViewById(R.id.btn_search);
        xRecyclerView = (XRecyclerView) findViewById(R.id.recycler);
        mIvSearchSwitch = findViewById(R.id.iv_search_witch);
        mRlHeadHotSearch = findViewById(R.id.rl_head_hot_search);
        mSearchTips = findViewById(R.id.search_tips);
        mRlHeadRoot = findViewById(R.id.rl_head_root);
        mTvDeleteHistory = findViewById(R.id.tv_delete_history);

        String keyWords = getIntent().getStringExtra(EXTRA_KEY_WORDS);
        if (keyWords != null) {
            mEdSearch.setHint(keyWords);
        }
        List<String> list = MainApp.getSearchBeanList();
        if (list != null) {
            String[] array = (String[]) list.toArray(new String[list.size()]);
            mSearchTips.initViews(array, SearchActivity.this);
            mRlHeadHotSearch.setVisibility(View.VISIBLE);
        } else {
            mRlHeadHotSearch.setVisibility(View.GONE);
        }

        mRecyclerAdapter = new RecyclerAdapter<Object>(mOnTipsItemListener) {
            @Override
            public ItemWrapper getItemHolder(int position) {
                if (mRecyclerAdapter.getItem(position) instanceof SearchBean.UrlBean) {
                    return new ItemWrapper(R.layout.item_search_keywords_text, SearchHolder.class);
                } else if (mRecyclerAdapter.getItem(position) instanceof String) {
                    return new ItemWrapper(R.layout.item_search_keywords_wd, WdHolder.class);
                } else if (mRecyclerAdapter.getItem(position) instanceof SearchHistoryBean) {
                    return new ItemWrapper(R.layout.item_search_text, SearchHistoryBeanHolder.class);
                } else if (mRecyclerAdapter.getItem(position) instanceof WebViewHistory) {
                    return new ItemWrapper(R.layout.item_web_history, WebViewHistoryHolder.class);
                } else {
                    return new ItemWrapper(R.layout.item_search_keywords, SearchApkHolder.class);
                }
            }
        };
        xRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        xRecyclerView.setAdapter(mRecyclerAdapter);

        mEdSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                edText = charSequence;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeStr = charSequence.toString();
                if (TextUtils.isEmpty(changeStr)) {
                    setHistoryView();
                    mRlHeadHotSearch.setVisibility(View.VISIBLE);
                } else {
                    setMatch();
                    mRlHeadRoot.setVisibility(View.GONE);
                }
                if (changeStr.equals("")) {
                    mIvDelete.setVisibility(View.GONE);
                } else {
                    mIvDelete.setVisibility(View.VISIBLE);
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable editable) {
                if (edText.length() == 0) {
                    mBtnSearch.setText("取消");
                    mBtnSearch.setTextColor(ContextCompat.getColor(SearchActivity.this, R.color.text_color));
                    mBtnSearch.setBackgroundColor(ContextCompat.getColor(SearchActivity.this, R.color.white_f));
                } else {
                    mBtnSearch.setText("搜索");
                    mBtnSearch.setTextColor(ContextCompat.getColor(SearchActivity.this, R.color.white_f));
                    mBtnSearch.setBackgroundColor(ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary));
                }
            }
        });
        mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edText = "";
                mEdSearch.setText(edText);
                mBtnSearch.setText("取消");
                mBtnSearch.setTextColor(ContextCompat.getColor(SearchActivity.this, R.color.text_color));
                mBtnSearch.setBackgroundColor(ContextCompat.getColor(SearchActivity.this, R.color.white_f));
            }
        });
        rlCopyContext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(StringUtils.getCopyText(SearchActivity.this));
            }
        });
        mTvDeleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(SearchActivity.this)
                        .setTitle("删除")
                        .setMessage("是否清空历史记录")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DbHelper.getDaoSession().getSearchHistoryBeanBeanDao().deleteAll();
                                //TODO

                                //mRecyclerAdapter.clear();
                                mRlHeadRoot.setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
    }

    private void setExtraUrl() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (!TextUtils.isEmpty(url)) {
            mEdSearch.setText(url);
            mEdSearch.setCursorVisible(true);
            mEdSearch.setFocusableInTouchMode(true);
            mEdSearch.setSelection(0);
            mEdSearch.selectAll();
            mBtnSearch.setText("进入");
            mBtnSearch.setTextColor(ContextCompat.getColor(SearchActivity.this, R.color.white_f));
            mBtnSearch.setBackgroundColor(ContextCompat.getColor(SearchActivity.this, R.color.colorPrimary));
        }
    }

    //输入框监听获取数据
    private void setMatch() {
        Disposable disposable = Observable.just(new LocalDataSource().getLiteOrm().query(SearchHistoryBean.class))
                .flatMap(histories -> {
                    List<Object> objectList = new ArrayList<>();
                    for (int i = 0; i < histories.size(); i++) {
                        if (histories.get(i).getContent().contains(changeStr)) {
                            objectList.add(histories.get(i));
                        }
                    }
                    isSearch = true;
                    return Observable.just(objectList);
                }).flatMap((Function<List<Object>, ObservableSource<List<Object>>>) objectList -> {
                    List<WebViewHistory> viewHistories = new LocalDataSource().getLiteOrm().query(WebViewHistory.class);
                    for (int i = 0; i < viewHistories.size(); i++) {
                        if (i < 4) {
                            if (viewHistories.get(i).getTitle().contains(changeStr) || viewHistories.get(i).getWebUrl().contains(changeStr)) {
                                objectList.add(viewHistories.get(i));
                            }
                        }
                    }
                    return Observable.just(objectList);
                }).subscribe(new Consumer<List<Object>>() {
                    @Override
                    public void accept(List<Object> objectList) throws Exception {
                        if (objectList.size() != 0) {
                            mRlHeadRoot.setVisibility(View.GONE);
                            mRlHeadHotSearch.setVisibility(View.GONE);
                        } else {
                            mRlHeadHotSearch.setVisibility(View.VISIBLE);
                        }
                        mRecyclerAdapter.refresh(objectList);
                        Disposable disposable = PostsRepository.searchMatch(changeStr)
                                .subscribe(searchBeans -> {
                                    LogHelper.i("guanjianc", searchBeans.getWd().size());
                                    if (searchBeans.getApp().size() == 0 && searchBeans.getUrl().size() == 0
                                            && searchBeans.getWd() == null) {
                                        return;
                                    }
                                    if (mRecyclerAdapter == null) {
                                        return;
                                    }
                                    List<Object> objectLists = new ArrayList<>();
                                    if (searchBeans.getApp().size() != 0) {
                                        objectLists.addAll(searchBeans.getApp());
                                    }
                                    if (searchBeans.getUrl().size() != 0) {
                                        objectLists.addAll(searchBeans.getUrl());
                                    }
                                    if (searchBeans.getWd() != null) {
                                        objectLists.addAll(searchBeans.getWd());
                                    }
                                    xRecyclerView.setAdapter(mRecyclerAdapter);
                                    mRecyclerAdapter.addAll(objectLists);
                                    if (objectLists.size() != 0) {
                                        mRlHeadRoot.setVisibility(View.GONE);
                                        mRlHeadHotSearch.setVisibility(View.GONE);
                                    } else {
                                        mRlHeadHotSearch.setVisibility(View.VISIBLE);
                                    }
                                }, throwable -> {
                                    LogHelper.e("error", throwable);
                                });
                        addDisposable(disposable);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogHelper.e("error", throwable);
                    }
                });
        addDisposable(disposable);
    }

    //显示历史消息
    private void setHistoryView() {
        Disposable disposable = Observable.just(new LocalDataSource().getLiteOrm().query(SearchHistoryBean.class))
                .flatMap(histories -> {
                    Collections.reverse(histories);
                    List<Object> objectList = new ArrayList<>();
                    for (int i = 0; i < histories.size(); i++) {
                        objectList.add(histories.get(i));
                    }
                    return Observable.just(objectList);
                }).subscribe(new Consumer<List<Object>>() {
                    @Override
                    public void accept(List<Object> objects) throws Exception {
                        if (objects.size() == 0) {
                            mRlHeadRoot.setVisibility(View.GONE);
                        } else {
                            isSearch = false;
                            mRlHeadRoot.setVisibility(View.VISIBLE);
                            mRecyclerAdapter.refresh(objects);
                        }
                    }
                });
        addDisposable(disposable);
    }

    //设置界面所有监听事件
    private void setListener() {
        mEdSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.closeKeybord(mEdSearch, SearchActivity.this);
                if (v.getText().toString().length() == 0) {
                    String str = getIntent().getStringExtra(EXTRA_KEY_WORDS);
                    if (str != null) {
                        search(str);
                    }
                } else {
                    search(v.getText().toString());
                }
                return true;
            }
            return false;
        });

        mBtnSearch.setOnClickListener(v -> {
            Utils.closeKeybord(mEdSearch, SearchActivity.this);
            search(mEdSearch.getText().toString());
        });


        mIvSearchSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disposable disposable = AppSetting.getInstance().getSearchPlatforms()
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
                                ToastUtils.show(throwable);
                            }
                        });
                addDisposable(disposable);
            }
        });
    }

    //选择搜索引擎对话框
    private void showSearchPlatformDialog(List<SearchplBean> searchPlatforms) {
        int checkedItem = 0;
        List<String> sps = new ArrayList<>();
        for (SearchplBean sp : searchPlatforms) {
            sps.add(sp.getName());
            if (AppSetting.getInstance().getDefaultSearch().equals(sp.getUrl())) {
                checkedItem = sps.size() - 1;
            }
        }
        new AlertDialog.Builder(SearchActivity.this)
                .setSingleChoiceItems(sps.toArray(new String[sps.size()]), checkedItem,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SettingHelper.saveDefaultSearch(searchPlatforms.get(which).getUrl());
                                //RxBus.post(SearchSwitchEvent.class);
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    //开始搜索
    private void search(String text) {
        if (text.length() == 0) {
            SoftInputUtil.hideIMM(mEdSearch);
            finish();
        } else {
            if (TextUtils.isEmpty(text)) {
                Toast.makeText(SearchActivity.this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!AppSetting.getInstance().isPrivacyMode()) {
                saveSearchHistoryBeanIfNotExist(text);
                saveKeywordsBean(text);
            }
            Intent intent = new Intent();
            intent.putExtra("webUrl", text);
            setResult(2, intent);
            finish();
        }
    }

    //保存搜索记录，排重
    private void saveSearchHistoryBeanIfNotExist(String text) {
        if (TextUtils.isEmpty(text) || text.equals("")) {
            return;
        }
        List<SearchHistoryBean> histories = new LocalDataSource().getLiteOrm().query(SearchHistoryBean.class);
        if (histories.size() != 0) {
            for (int i = 0; i < histories.size(); i++) {
                if (histories.get(i).getContent().equals(text)) {
                    new LocalDataSource().getLiteOrm().delete(histories.get(i));
                }
            }
        }
        new LocalDataSource().getLiteOrm().save(new SearchHistoryBean(text));
    }

    //热搜标签点击事件
    @Override
    public void onClick(int position) {
        List<String> list = MainApp.getSearchBeanList();
        if (list == null) {
            return;
        }
        Utils.closeKeybord(mEdSearch, SearchActivity.this);
        search(list.get(position));
        StatisticsManagement.getInstance().updateClickCount(StatisticsManagement.HOTSEARCH);
    }

    interface OnTipsItemListener {
        void onItemClick(String url, String wd);
    }

    //历史记录适配器
    static class SearchApkHolder extends RecyclerHolder<SearchBean.AppBean> {

        private ImageView mIvIcon;
        private TextView mTvApkName;
        private TextView mDownload;
        private OnTipsItemListener mListener;

        public SearchApkHolder(View itemView, OnTipsItemListener listener) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            mTvApkName = (TextView) itemView.findViewById(R.id.tv_apk_name);
            mDownload = (TextView) itemView.findViewById(R.id.download);
            mListener = listener;
        }

        @Override
        public void onBindView(SearchBean.AppBean appBean) {
            GlideUtils.loadImageViewDef(itemView.getContext(), appBean.getIcon_url(), mIvIcon);
            setChangeTextView(appBean.getName(), changeStr, mTvApkName);
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(appBean.getApk_url(), appBean.getName());
                }
            });
        }
    }

    static class SearchHolder extends RecyclerHolder<SearchBean.UrlBean> {

        private TextView mTvName;
        private OnTipsItemListener mListener;

        public SearchHolder(View itemView, OnTipsItemListener listener) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mListener = listener;
        }

        @Override
        public void onBindView(SearchBean.UrlBean urlBean) {
            setChangeTextView(urlBean.getName(), changeStr, mTvName);
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(urlBean.getUrl(), urlBean.getName());
                }
            });
        }
    }

    //关键字
    static class WdHolder extends RecyclerHolder<String> {

        private TextView mTvName;
        private OnTipsItemListener mListener;

        public WdHolder(View itemView, OnTipsItemListener listener) {
            super(itemView);
            mTvName = (TextView) itemView.findViewById(R.id.tv_name);
            mListener = listener;
        }

        @Override
        public void onBindView(String name) {
            setChangeTextView(name, changeStr, mTvName);
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(name, name);
                }
            });
        }
    }

    static class SearchHistoryBeanHolder extends RecyclerHolder<SearchHistoryBean> {
        private TextView mTvSearch;
        private ImageView mIvDelete;
        private OnTipsItemListener mListener;

        public SearchHistoryBeanHolder(View itemView, OnTipsItemListener listener) {
            super(itemView);
            mTvSearch = itemView.findViewById(R.id.tv_search);
            mIvDelete = itemView.findViewById(R.id.iv_delete);
            mListener = listener;
        }

        @Override
        public void onBindView(SearchHistoryBean history) {
            mIvDelete.setVisibility(isSearch ? View.GONE : View.VISIBLE);
            if (isSearch) {
                setChangeTextView(history.getContent(), changeStr, mTvSearch);
            } else {
                mTvSearch.setText(history.getContent());
            }
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(history.getContent(), history.getContent());
                }
            });
            mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocalDataSource.getInstance().getLiteOrm()
                            .delete(new WhereBuilder(SearchHistoryBean.class)
                                    .and()
                                    .equals("id", history.getId()));
                    mRecyclerAdapter.remove(getLayoutPosition());
                }
            });
        }
    }

    static class WebViewHistoryHolder extends RecyclerHolder<WebViewHistory> {
        private TextView tvWebUrl;
        private TextView tvTitle;
        private OnTipsItemListener mListener;

        public WebViewHistoryHolder(View itemView, OnTipsItemListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvWebUrl = itemView.findViewById(R.id.tv_web_url);
            mListener = listener;
        }

        @Override
        public void onBindView(WebViewHistory history) {
            setChangeTextView(history.getTitle(), changeStr, tvTitle);
            setChangeTextView(history.getWebUrl(), changeStr, tvWebUrl);
            itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(history.getWebUrl(), history.getTitle());
                }
            });
        }

    }
}
