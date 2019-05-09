package com.kunfei.bookshelf.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BaseBrowserActivity extends AppCompatActivity {

    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public CompositeDisposable getCompositeDisposable() {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }

        return this.mCompositeDisposable;
    }

    public void addDisposable(Disposable disposable) {
        if (this.mCompositeDisposable == null) {
            this.mCompositeDisposable = new CompositeDisposable();
        }

        this.mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        if (this.mCompositeDisposable != null) {
            this.mCompositeDisposable.dispose();
        }
        super.onDestroy();
    }

    /**
     * 界面跳转
     *
     * @param tarActivity
     */
    protected void intentActivity(Class<? extends AppCompatActivity> tarActivity) {
        Intent intent = new Intent(this, tarActivity);
        startActivity(intent);
    }

    /**
     * 展示一个SnackBar
     */
    public void show(String message) {
        //去掉虚拟按键
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //隐藏虚拟按键栏
                | View.SYSTEM_UI_FLAG_IMMERSIVE //防止点击屏幕时,隐藏虚拟按键栏又弹了出来
        );
        Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_INDEFINITE).show();
    }
}
