package com.kunfei.bookshelf.view.adapter.browser;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.help.SettingHelper;
import com.kunfei.bookshelf.view.stackview.adapter.StackAdapter;
import com.kunfei.bookshelf.view.stackview.widget.TabStackView;
import com.kunfei.bookshelf.view.web.Tab;
import com.kunfei.bookshelf.view.web.TabWebController;

import java.util.ArrayList;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class TabAdapter extends StackAdapter<Tab> {
    private TabWebController mController;
    private List<Tab> mTabs;

    public TabAdapter(Context context, TabWebController controller) {
        super(context);
        mController = controller;
        mTabs = new ArrayList<Tab>();
    }

    @Override
    public Tab getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public void bindView(Tab tab, int position, TabStackView.ViewHolder holder) {
        TabViewHolder pagerViewHolder = (TabViewHolder) holder;
        pagerViewHolder.bind(tab);
    }

    @Override
    protected TabStackView.ViewHolder onCreateView(ViewGroup parent, int viewType) {
        CardView card = (CardView) mInflater.inflate(R.layout.layout_recycler_card, parent, false);
        card.setCardElevation(4);
        card.setRadius(16);
        mInflater.inflate(R.layout.layout_tab, card, true);
        return new TabViewHolder(card);
    }

    class TabViewHolder extends TabStackView.ViewHolder implements View.OnClickListener {

        private View content;
        private ImageView ivPagePreview, ivWebsiteIcon, ivClose;
        private TextView tvPosition;
        private Tab tab;
        private RelativeLayout rlPageHead;

        TabViewHolder(View view) {
            super(view);
            content = view;
            rlPageHead = (RelativeLayout) view.findViewById(R.id.rlPageHead);
            ivPagePreview = (ImageView) view.findViewById(R.id.ivPagePreview);
            ivWebsiteIcon = (ImageView) view.findViewById(R.id.ivWebsiteIcon);
            ivClose = (ImageView) view.findViewById(R.id.ivPageClose);
            tvPosition = (TextView) view.findViewById(R.id.tvPagerUC);
        }

        public void bind(Tab tab) {
            String title = tab.getTitle();
            if (tab.isMain()) {
                tvPosition.setText("首页");
            } else {
                tvPosition.setText(title);
            }
            if (tab.isMain()) {
                ivWebsiteIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bottom_home));
            } else {
                Bitmap favicon = tab.getFavicon();
                if (favicon != null) {
                    ivWebsiteIcon.setImageBitmap(favicon);
                }
            }
            Bitmap preview = tab.getScreenshot();
            if (preview != null) {
                ivPagePreview.setImageBitmap(preview);
            } else {
                if (!tvPosition.getText().toString().equals("首页")) {
                    //TODO
                    //ivPagePreview.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.bg_tabs_default));
                }
            }
            if (SettingHelper.isPrivacyMode()) {
                rlPageHead.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple));
                tvPosition.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.white_f));
                ivClose.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_sc_delete_white));
            } else {
                rlPageHead.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white_f));
                tvPosition.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.text_color));
                ivClose.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_sc_delete_black));
            }
            ivClose.setOnClickListener(this);
            content.setOnClickListener(this);
            this.tab = tab;
        }

        @Override
        public void onClick(View view) {
            if (view == content) {
                if (mController != null) {
                    mController.selectTab(tab);
                }
            } else if (view == ivClose) {
                if (mController != null) {
                    mController.closeTab(tab);
                }
            }
        }
    }
}
