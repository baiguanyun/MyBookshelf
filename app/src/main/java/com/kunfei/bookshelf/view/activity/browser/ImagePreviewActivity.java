package com.kunfei.bookshelf.view.activity.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.utils.GlideUtils;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;


/***
 * 单张图片预览界面
 */
public class ImagePreviewActivity extends AppCompatActivity {

    private final static String BUNDLE_IMAGE = "bundle_image";
    private final static String LIST_IMAGE = "list_image";
    private final static String IMAGE_INDEX = "IMAGE_index";
    private final static String IMAGEURL = "imageUrl";
    private PhotoView mPhotoView;
    private Button mBtnSaveImage;
    private Bundle bundle;
    private ArrayList<String> mImages;
    private int index;

    //单张
    public static void start(Context context, String imgUrl) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(IMAGEURL, imgUrl);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    //多张
    public static void start(Context context, int index, ArrayList<String> images) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(LIST_IMAGE, images);
        bundle.putInt(IMAGE_INDEX, index);
        intent.putExtra(BUNDLE_IMAGE, bundle);
        context.startActivity(intent);
        ((Activity) context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String url = getIntent().getStringExtra(IMAGEURL);
        if (url != null) {
            setContentView(R.layout.image_preview);
            mPhotoView = (PhotoView) findViewById(R.id.photoView);
            mBtnSaveImage = (Button) findViewById(R.id.btn_save_image);

            //启用缩放功能
            mPhotoView.enable();

            GlideUtils.loadImageViewDef(this, url, mPhotoView);

            mPhotoView.setOnClickListener(v -> finish());

            mBtnSaveImage.setOnClickListener(v -> {
                GlideUtils.saveImage(this, url);
            });
        } else {
            HackyViewPager viewPager = new HackyViewPager(this);
            setContentView(viewPager);
            bundle = getIntent().getBundleExtra(BUNDLE_IMAGE);
            mImages = bundle.getStringArrayList(LIST_IMAGE);
            index = bundle.getInt(IMAGE_INDEX);
            viewPager.setAdapter(new SamplePagerAdapter(mImages));
            viewPager.setCurrentItem(index);
        }
    }


    class SamplePagerAdapter extends PagerAdapter {

        private ArrayList<String> urls;

        public SamplePagerAdapter(ArrayList<String> urls) {
            this.urls = urls;
        }

        @Override
        public int getCount() {
            return urls.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View rootView = View.inflate(ImagePreviewActivity.this,
                    R.layout.viewpager_item, null);
            PhotoView image = rootView.findViewById(R.id.pv_image);
            TextView indicator = rootView.findViewById(R.id.indicator);
            mPhotoView.enable();
            GlideUtils.loadImageView(ImagePreviewActivity.this, urls.get(position), mPhotoView);
            CharSequence text = getString(R.string.viewpager_indicator, position + 1, getCount());
            indicator.setText(text);
            container.addView(rootView,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            return rootView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public class HackyViewPager extends ViewPager {
        private boolean isLocked;

        public HackyViewPager(Context context) {
            super(context);
            isLocked = false;
        }

        public HackyViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
            isLocked = false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (!isLocked) {
                try {
                    return super.onInterceptTouchEvent(ev);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return !isLocked && super.onTouchEvent(event);
        }
    }


}
