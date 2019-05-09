package com.kunfei.bookshelf.view.adapter.browser;

import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.WebHistoryBean;
import com.kunfei.bookshelf.utils.GlideUtils;
import com.kunfei.bookshelf.utils.Utils;
import com.kunfei.bookshelf.view.web.OnItemCkListener;

import java.util.List;

import androidx.core.content.ContextCompat;

public class WebHistoryAdapter extends BaseQuickAdapter<WebHistoryBean, BaseViewHolder> {

    private OnItemCkListener onItemCkListener;

    public WebHistoryAdapter(List<WebHistoryBean> data) {
        super(R.layout.item_web_history, data);
    }

    public void setOnItemCkListener(OnItemCkListener onItemCkListener) {
        this.onItemCkListener = onItemCkListener;
    }

    @Override
    protected void convert(final BaseViewHolder viewHolder, final WebHistoryBean history) {
        ImageView ivIcon = viewHolder.getView(R.id.iv_icon);
        TextView tvTitle = viewHolder.getView(R.id.tv_title);
        TextView tvWebUrl = viewHolder.getView(R.id.tv_web_url);
        tvTitle.setText(history.getTitle().equals("") ? "无标题" : history.getTitle());
        tvWebUrl.setText(history.getWebUrl());
        viewHolder.itemView.setOnClickListener(v ->
                onItemCkListener.onClick(viewHolder.getAdapterPosition(), viewHolder.itemView, history));
        if (history.getIcon() != null && !TextUtils.isEmpty(history.getIcon())) {
            GlideUtils.loadImageViewDef(viewHolder.itemView.getContext(), Utils.stringToBitmap(history.getIcon()), ivIcon);
        } else {
            ivIcon.setImageDrawable(ContextCompat.getDrawable(viewHolder.itemView.getContext(), R.drawable.ic_history));
        }
    }
}
