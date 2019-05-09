package com.kunfei.bookshelf.view.activity.browser;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.github.nukc.stateview.StateView;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.base.BaseBrowserActivity;
import com.kunfei.bookshelf.base.RxBus;
import com.kunfei.bookshelf.bean.WebHistoryBean;
import com.kunfei.bookshelf.bean.event.LoadWebUrlEvent;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.view.adapter.browser.WebHistoryAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class WebHistoryActivity extends BaseBrowserActivity {

    private RecyclerView recyclerView;
    private WebHistoryAdapter adapter;
    private TextView tvDelete;
    private StateView stateView;

    public static void start(Context context) {
        Intent intent = new Intent(context, WebHistoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_history);
        recyclerView = findViewById(R.id.recycler_view);
        //setToolbar("历史记录");
        stateView = StateView.inject(this);
        stateView.showLoading();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WebHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        List<WebHistoryBean> webHistoryBeanList = DbHelper.getDaoSession().getWebHistoryBeanDao().queryBuilder().list();
        stateView.showContent();
        if (webHistoryBeanList.size() == 0) {
            stateView.showEmpty();
        } else {
            Collections.reverse(webHistoryBeanList);
            adapter.addData(webHistoryBeanList);
        }
        tvDelete = findViewById(R.id.tv_delete);
        tvDelete.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("删除")
                    .setMessage("确认删除所有历史记录？")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DbHelper.getDaoSession().getWebHistoryBeanDao().deleteAll();
                            adapter.getData().clear();
                            stateView.showEmpty();
                            dialog.dismiss();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

        });
        adapter.setOnItemCkListener((position, view1, object) -> {
            WebHistoryBean history = adapter.getData().get(position);
            RxBus.post(new LoadWebUrlEvent(history.getWebUrl()));
            finish();
        });
    }
}
