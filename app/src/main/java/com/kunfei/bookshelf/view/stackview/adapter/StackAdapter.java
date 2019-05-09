package com.kunfei.bookshelf.view.stackview.adapter;

import android.content.Context;
import android.view.LayoutInflater;

import com.kunfei.bookshelf.view.stackview.widget.TabStackView;

import java.util.ArrayList;
import java.util.List;


public abstract class StackAdapter<T> extends TabStackView.Adapter<TabStackView.ViewHolder> {
    protected final Context mContext;
    protected final LayoutInflater mInflater;
    private List<T> mData;

    public StackAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = new ArrayList<>();
    }

    public void updateData(List<T> data) {
        setData(data);
    }

    public List<T> getData() {
        return mData;
    }

    public void setData(List<T> data) {
        if (data == null) {
            return;
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(int index, T t, boolean notify) {
        if (index < 0 || index > mData.size()) {
            return;
        }
        mData.add(index, t);
        if (notify) notifyDataSetChanged();
    }

    public void addData(T t, boolean notify) {
        mData.add(t);
        if (notify) notifyDataSetChanged();
    }

    public void removeData(T t, boolean notify) {
        if (mData.contains(t)) {
            mData.remove(t);
            if (notify) notifyDataSetChanged();
        }
    }

    public void removeData(int index, boolean notify) {
        if (index < 0 || index > mData.size()) {
            return;
        }
        mData.remove(index);
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    protected void onBindViewHolder(TabStackView.ViewHolder holder, int position) {
        T data = mData.get(position);
        bindView(data, position, holder);
    }

    public abstract void bindView(T data, int position, TabStackView.ViewHolder holder);

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public T getItem(int position) {
        return mData.get(position);
    }
}
