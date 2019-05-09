package com.kunfei.bookshelf.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.kunfei.bookshelf.MApplication;
import com.kunfei.bookshelf.help.BlurTransformation;

import androidx.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 图片加载器
 */

public class GlideUtils {

    /**
     * 设置加载中以及加载失败图片
     *
     * @param mContext
     * @param path
     * @param mImageView
     */
    public static void loadImageView(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions();
        Glide.with(mContext)
                .load(path)
                .apply(options)
                .into(mImageView);
    }

    public static void loadImageViewDef(Context mContext, Object path, ImageView mImageView) {
        RequestOptions options = new RequestOptions();
        Glide.with(mContext)
                .load(path)
                .apply(options)
                .into(mImageView);
    }

    /**
     * 加载gif图片
     *
     * @param mContext
     * @param path
     * @param mImageView
     */
    public static void loadImageViewGif(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions();
        Glide.with(mContext)
                .asGif()
                .load(path)
                .apply(options)
                .into(mImageView);
    }

    /**
     * 圆形图标
     *
     * @param mContext
     * @param path
     * @param mImageView
     */
    public static void cropCircleLoadImageView(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .skipMemoryCache(true) // 不使用内存缓存
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
                .circleCrop();
        Glide.with(mContext)
                .load(path)
                .apply(options)
                .into(mImageView);
    }

    /**
     * 模糊图片
     *
     * @param mContext
     * @param path
     * @param mImageView
     */
    public static void blurTransImageView(Context mContext, String path, ImageView mImageView) {
        RequestOptions options = new RequestOptions()
                .transform(new BlurTransformation(mContext, 70));
        Glide.with(mContext)
                .load(path)
                .apply(options)
                .into(mImageView);
    }

    public static void saveImage(Context context, String url) {
        Glide.with(context)
                .asBitmap()
                .load(url)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target,
                                                boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        if (resource == null) {
                            return true;
                        }
                        Observable.just(FileUtils.saveImageToGallery(
                                context, resource))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(path -> {
                                    if (!path.equals("")) {
                                        Toast.makeText(MApplication.getInstance(), "已保存到图库", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MApplication.getInstance(), "保存失败", Toast.LENGTH_SHORT).show();
                                    }
                                }, throwable -> LogHelper.e(throwable.getMessage()));
                        return false;
                    }
                }).submit();
    }


}
