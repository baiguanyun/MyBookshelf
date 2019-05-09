package com.kunfei.bookshelf.view.web;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HandleListDialog extends BaseDialogFragment {
    List<String> titleList = new ArrayList<>();
    OnItemClickListener mOnItemClickListener;
    private RecyclerView mRecyclerView;
    private int LocationX = 0;
    private int LocationY = 0;

    public static HandleListDialog getInstance(int locationX, int locationY, ArrayList<String> titleList) {
        HandleListDialog listDialog = new HandleListDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("intX", locationX);
        bundle.putInt("intY", locationY);
        bundle.putStringArrayList("titleList", titleList);
        listDialog.setArguments(bundle);
        return listDialog;
    }

    public void show(FragmentManager manager) {
        show(manager, HandleListDialog.class.getSimpleName());
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    protected void initParams(Bundle bundle) {
        LocationX = bundle.getInt("intX");
        LocationY = bundle.getInt("intY");
        titleList = bundle.getStringArrayList("titleList");
    }

    @Override
    protected void initPresenter() {
        super.initPresenter();
    }

    @Override
    protected void initDialogStyle(Dialog dialog, Window window) {
        super.initDialogStyle(dialog, window);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        lp.x = LocationX;
        lp.y = LocationY;
        lp.width = DensityUtil.dp2px(dialog.getContext(), 100);
        lp.dimAmount = 0.0f;
        lp.windowAnimations = R.style.alertDialogTheme;
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_list;
    }


    public void bindView(View rootView, Bundle savedInstanceState) {
        mRecyclerView = rootView.findViewById(R.id.popup_list);
    }


    public void afterViewBind(View rootView, Bundle savedInstanceState) {
        listAdapter listAdapter = new listAdapter(new ArrayList<>());
        listAdapter.setOnclickRefreshListener((position, object) -> itemClick(position));
        listAdapter.addData(titleList);
        mRecyclerView.setAdapter(listAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL, false));

    }

    private void itemClick(int position) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClicked(position);
        }
        dismiss();
    }


    public interface OnItemClickListener {
        void onItemClicked(int position);
    }

    public class listAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

        private OnClickItemListener onclickRefreshListener;

        public listAdapter(List<String> data) {
            super(R.layout.item_popup_list, data);
        }

        @Override
        protected void convert(final BaseViewHolder baseViewHolder, final String s) {
            ((TextView) baseViewHolder.getView(R.id.item_title)).setText(s);
            baseViewHolder.itemView.setOnClickListener(v ->
                    onclickRefreshListener.onClick(baseViewHolder.getAdapterPosition(), s));
        }

        public void setOnclickRefreshListener(OnClickItemListener onclickRefreshListener) {
            this.onclickRefreshListener = onclickRefreshListener;
        }

    }
}

