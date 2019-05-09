package com.kunfei.bookshelf.view.web;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kunfei.bookshelf.R;
import com.kunfei.bookshelf.bean.CollectionBean;
import com.kunfei.bookshelf.bean.HomeMenuBean;
import com.kunfei.bookshelf.dao.DbHelper;
import com.kunfei.bookshelf.help.SettingHelper;
import com.kunfei.bookshelf.utils.GlideUtils;
import com.kunfei.bookshelf.utils.TimeUtils;
import com.kunfei.bookshelf.view.activity.MainActivity;
import com.kunfei.bookshelf.view.adapter.browser.HomeMenuAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivityView {
    public static void showWebMenu(Activity activity, TabController tabController) {
        final BottomSheetDialog dialog = new BottomSheetDialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.bottom_dialog_menu, null);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        ImageView btnDiscount = view.findViewById(R.id.btn_discount);
        TextView tvName = view.findViewById(R.id.tv_name);

        int[] icon = {R.drawable.ic_menu_my_collection, R.drawable.ic_meun_hostory, R.drawable.shujia, R.drawable.ic_menu_add_collecttion, R.drawable.ic_moon,
                R.drawable.ic_menu_xzgl, R.drawable.ic_menu_fx, R.drawable.ic_menu_sx, R.drawable.ic_menu_sz, R.drawable.ic_menu_tc};
        String[] iconName = activity.getResources().getStringArray(R.array.web_menu_array);
        List<HomeMenuBean> menuBeans = new ArrayList<>();
        for (int i = 0; i < iconName.length; i++) {
            HomeMenuBean homeMenuBean = new HomeMenuBean();
            if (i == 4) {
                if (SettingHelper.getIsDayMode()) {
                    homeMenuBean.setIcon(R.drawable.ic_moon);
                    homeMenuBean.setName("夜间模式");
                } else {
                    homeMenuBean.setIcon(R.drawable.ic_sun);
                    homeMenuBean.setName("白天模式");
                }
            } else if (i == 3) {
                if (tabController == null) {
                    homeMenuBean.setIcon(R.drawable.ic_menu_tjsc_off);
                } else {
                    homeMenuBean.setIcon(R.drawable.ic_menu_add_collecttion);
                }
                homeMenuBean.setName(iconName[i]);
            } else if (i == 7) {
                if (tabController == null) {
                    homeMenuBean.setIcon(R.drawable.ic_menu_sx_off);
                } else {
                    homeMenuBean.setIcon(R.drawable.ic_menu_sx);
                }
                homeMenuBean.setName(iconName[i]);
            } else {
                homeMenuBean.setName(iconName[i]);
                homeMenuBean.setIcon(icon[i]);
            }
            menuBeans.add(homeMenuBean);
        }
        HomeMenuAdapter adapter = new HomeMenuAdapter(menuBeans, tabController);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 5));
        recyclerView.setAdapter(adapter);
        btnDiscount.setOnClickListener(v -> dialog.dismiss());
        adapter.setOnItemCkListener(new OnItemCkListener() {
            @Override
            public void onClick(int position, View view, Object object) {
                if (position == 3 || position == 7) {
                    if (tabController == null) {
                        return;
                    }
                }
                switch (position) {
                    case 0:
                        //CollectionActivity.start(activity);
                        break;
                    case 1:
                        //WebHistoryActivity.start(activity);
                        break;
                    case 2:
                        //ShelfActivity.start(activity);
                        break;
                    case 3:
                        if (TextUtils.isEmpty(tabController.getCurrentTab().getWebView().getUrl()) ||
                                TextUtils.isEmpty(tabController.getCurrentTab().getWebView().getTitle()) ||
                                !tabController.getCurrentTab().getWebView().getUrl().startsWith("http")) {
                            Toast.makeText(activity, "收藏网址错误", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DbHelper.getDaoSession().getCollectionBeanDao().save(new CollectionBean(tabController.getCurrentTab().getWebView().getTitle(),
                                tabController.getCurrentTab().getWebView().getUrl(), TimeUtils.getNowString(), "false"));
                        Toast.makeText(activity, "已添加到本地收藏", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        //夜间模式切换设置数据
                        /*if (tabController != null) {
                            AppSetting.getInstance().setmIsUpdateDelegate(true);
                            AppSetting.getInstance().setmIsShowBlack(true);
                            AppSetting.getInstance().setmIsLoadTabs(true);
                            AppSetting.getInstance().setUrl(tabController.getCurrentTab().getWebView().getUrl());
                        }
                        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            AppSetting.getInstance().setIsDayMode(true);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            AppSetting.getInstance().setIsDayMode(false);
                        }
                        activity.recreate();*/
                        ((MainActivity) activity).setNightTheme(!((MainActivity) activity).isNightTheme());
                        break;

                    case 5:
                        //DownloadManageActivity.start(activity);
                        break;
                    case 6:
                        //分享对话框
//                        DialogUtils.shareUDialog(activity,
//                                tabController != null ? tabController.getCurrentTab().getWebView().getTitle() : "CC浏览器应用宝下载",
//                                tabController != null ? tabController.getCurrentTab().getWebView().getUrl() : Constants.MYAPP_SHARE_URL,
//                                false, null);
                        break;

                    case 7:
                        tabController.getCurrentTab().getWebView().reload();
                        break;
                    case 8:
                        //SettingActivity.start(activity);
                        break;
                    case 9:
                        if (SettingHelper.getExitType() == 0) {
                            //DialogUtils.exitAppDialog(activity);
                        } else {
                            //StorageHelper.saveTabList(null);
                            SettingHelper.saveHistory("");
                            activity.finish();
                        }
                        break;
                }
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        dialog.getDelegate().findViewById(com.google.android.material.R.id.design_bottom_sheet).setBackgroundColor(activity.getResources().getColor(R.color.transparent));
        dialog.show();
    }
}
