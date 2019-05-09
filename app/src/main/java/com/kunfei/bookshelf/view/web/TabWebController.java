package com.kunfei.bookshelf.view.web;

/**
 * Created by Administrator on 2018/06/15 0015.
 */

public interface TabWebController extends WebViewController {
    void onWebsiteIconClicked(String url);

    void selectTab(Tab tab);

    void closeTab(Tab tab);

    void onTabCountChanged();

    void onTabDataChanged(Tab tab);
}
