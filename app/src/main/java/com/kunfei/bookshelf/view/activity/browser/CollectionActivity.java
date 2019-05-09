package com.kunfei.bookshelf.view.activity.browser;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kunfei.basemvplib.impl.IPresenter;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.MBaseActivity;
import com.kunfei.bookshelf.base.RxBus;
import com.kunfei.bookshelf.bean.CollectionBean;
import com.kunfei.bookshelf.bean.event.LoadWebUrlEvent;
import com.kunfei.bookshelf.utils.LogHelper;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class CollectionActivity extends MBaseActivity {

    private static RecyclerAdapter<CollectionBean> beanRecyclerAdapter;
    private static Activity activity;
    private RecyclerView mRecycler;
    private TextView mTvDataNull;
    private List<CollectionBean> beanList;

    public static void start(Context context) {
        Intent intent = new Intent(context, CollectionActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection);
        mTvDataNull = findViewById(R.id.tv_data_null);
        mRecycler = findViewById(R.id.recycler);
        setToolbar("我的收藏");
        activity = this;
        beanList = new LocalDataSource().getLiteOrm().query(CollectionBean.class);
        initView();
    }

    @Override
    protected IPresenter initInjector() {
        return null;
    }

    @Override
    protected void onCreateActivity() {

    }

    @Override
    protected void initData() {

    }

    private void initView() {
        if (beanList.size() != 0) {
            Collections.reverse(beanList);
            beanRecyclerAdapter = new RecyclerAdapter<CollectionBean>() {
                @Override
                public ItemWrapper getItemHolder(int position) {
                    return new ItemWrapper(R.layout.item_web_collection,
                            CollectionFHolder.class);
                }
            };
            mRecycler.setLayoutManager(new LinearLayoutManager(this));
            mRecycler.setAdapter(beanRecyclerAdapter);
            mRecycler.setVisibility(View.VISIBLE);
            beanRecyclerAdapter.refresh(beanList);
        } else {
            mTvDataNull.setVisibility(View.VISIBLE);
        }
    }

    static class CollectionFHolder extends RecyclerHolder<CollectionBean> {

        TextView mTvTitle;
        TextView mTvWebUrl;
        ImageView mIvDelete;

        CollectionFHolder(View convertView) {
            super(convertView);
            mTvTitle = convertView.findViewById(R.id.tv_title);
            mTvWebUrl = convertView.findViewById(R.id.tv_web_url);
            mIvDelete = convertView.findViewById(R.id.iv_delete);
        }

        @Override
        public void onBindView(final CollectionBean history) {
            mTvTitle.setText(history.getTitle());
            mTvWebUrl.setText(history.getUrl());
            itemView.setOnClickListener(v -> {
                if (history.getUrl() != null && !TextUtils.isEmpty(history.getUrl())) {
                    RxBus.post(new LoadWebUrlEvent(history.getUrl()));
                    activity.finish();
                }
            });
            mIvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.isLogin() && history.getCollectionId() != 0) {
                        Disposable disposable = PostsRepository.removeCollection(history.getCollectionId())
                                .subscribe(new Consumer<ResponseWrapper>() {
                                    @Override
                                    public void accept(ResponseWrapper responseWrapper) throws Exception {
                                        ToastUtils.show("删除成功");
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        LogHelper.e("error", throwable);
                                        ToastUtils.show(throwable);
                                    }
                                });
                    }
                    LocalDataSource.getInstance().delete(history);
                    beanRecyclerAdapter.remove(getAdapterPosition());
                }
            });
        }
    }

}
