package com.kunfei.bookshelf.view.adapter.browser;

import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.HomeMenuBean;
import com.kunfei.bookshelf.view.web.OnItemCkListener;
import com.kunfei.bookshelf.view.web.TabController;

import java.util.List;

import androidx.core.content.ContextCompat;

public class HomeMenuAdapter extends BaseQuickAdapter<HomeMenuBean, BaseViewHolder> {

    private final TabController controller;
    private OnItemCkListener onItemCkListener;

    public HomeMenuAdapter(List<HomeMenuBean> data, TabController controller) {
        super(R.layout.item_gridview_menu, data);
        this.controller = controller;
    }

    public void setOnItemCkListener(OnItemCkListener onItemCkListener) {
        this.onItemCkListener = onItemCkListener;
    }

    @Override
    protected void convert(final BaseViewHolder viewHolder, final HomeMenuBean bean) {
        ImageView mImage = viewHolder.itemView.findViewById(R.id.image);
        TextView mText = viewHolder.itemView.findViewById(R.id.text);

        if (controller == null) {
            //改变图标颜色
            if (viewHolder.getAdapterPosition() == 3 || viewHolder.getAdapterPosition() == 7) {
                mText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.home_null_text));
                viewHolder.itemView.setEnabled(false);
            } else {
                mText.setTextColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.textColorPrimary));
                viewHolder.itemView.setEnabled(true);
            }
        }
        mImage.setBackgroundResource(bean.getIcon());
        mText.setText(bean.getName());

        viewHolder.itemView.setOnClickListener(v ->
                onItemCkListener.onClick(viewHolder.getAdapterPosition(), viewHolder.itemView, bean));
    }
}
