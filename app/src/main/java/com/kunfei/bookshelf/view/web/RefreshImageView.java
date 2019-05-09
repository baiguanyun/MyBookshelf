package com.kunfei.bookshelf.view.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.DensityUtil;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;


public class RefreshImageView extends AppCompatImageView {
    private String text = "";  //要显示的文字
    private int color = R.color.text_color;   //文字的颜色
    private int textSize = 8;
    private Context context;

    public RefreshImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void setText(String text) {
        this.text = text;       //设置文字
    }

    public void setColor(int color) {

        this.color = color;    //设置文字颜色
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setTextSize(DensityUtil.dp2px(context, textSize));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        paint.setColor(ContextCompat.getColor(MApplication.getInstance(), color));
        canvas.drawText(text, getWidth() / 2, getHeight() / 2 + 10, paint);  //绘制文字
    }

}

