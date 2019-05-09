package com.kunfei.bookshelf.view.web;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import com.kunfei.bookshelf.utils.LogHelper;
import com.tencent.smtt.sdk.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;


public class TabController {
    private static final String POSITIONS = "positions";
    private static final String CURRENT = "current";
    private static long sNextId = 1;
    private final String TAG = "UcController";
    // Maximum number of tabs.
    private int mMaxTabs;
    // Private array of WebViews that are used as tabs.
    private ArrayList<Tab> mTabs;
    private ArrayList<Tab> mPrivacyModeTabs;
    // Queue of most recently viewed tabs.
    private ArrayList<Tab> mTabQueue;
    // Current position in mTabs.
    private int mCurrentTab = -1;
    private OnThumbnailUpdatedListener mOnThumbnailUpdatedListener;
    /// M: add for browser memory optimization, maintain the free tabs index @ {
    private CopyOnWriteArrayList<Integer> mFreeTabIndex = new CopyOnWriteArrayList<Integer>();
    /// @ }
    // the main browser controller
    private TabWebController mController;
    private int index;
    private boolean isPrivacyMode;

    /**
     * Construct a new TabControl object
     */
    public TabController(Context context, TabWebController controller) {
        mController = controller;
        mMaxTabs = 20;
        mTabs = new ArrayList<Tab>(mMaxTabs);
        mPrivacyModeTabs = new ArrayList<Tab>(mMaxTabs);
        mTabQueue = new ArrayList<Tab>(mMaxTabs);
    }

    synchronized static long getNextId() {
        return sNextId++;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Return the current tab's main WebView. This will always return the main
     * WebView for a given tab and not a subwindow.
     *
     * @return The current tab's WebView.
     */
    public WebView getCurrentWebView() {
        Tab t = getTab(mCurrentTab);
        if (t == null) {
            return null;
        }
        return t.getWebView();
    }

    /**
     * return the list of tabs
     */
    public List<Tab> getTabs() {
        mController.onTabCountChanged();
        if (isPrivacyMode) {
            return mPrivacyModeTabs;
        } else {
            return mTabs;
        }
    }

    public void setTabs(ArrayList<Tab> mTabs) {
        this.mTabs = mTabs;
    }

    public boolean isPrivacyMode() {
        return isPrivacyMode;
    }

    public void setPrivacyMode(boolean privacyMode) {
        isPrivacyMode = privacyMode;
    }

    public ArrayList<Tab> getmPrivacyModeTabs() {
        return mPrivacyModeTabs;
    }

    public void setmPrivacyModeTabs(ArrayList<Tab> mPrivacyModeTabs) {
        this.mPrivacyModeTabs = mPrivacyModeTabs;
    }

    /**
     * Return the tab at the specified position.
     *
     * @return The Tab for the specified position or null if the tab does not
     * exist.
     */
    public Tab getTab(int position) {
        if (isPrivacyMode) {
            if (position >= 0 && position < mPrivacyModeTabs.size()) {
                return mPrivacyModeTabs.get(position);
            }
        } else {
            if (position >= 0 && position < mTabs.size()) {
                return mTabs.get(position);
            }
        }
        return null;
    }

    /**
     * Return the current tab.
     *
     * @return The current tab.
     */
    public Tab getCurrentTab() {
        return getTab(mCurrentTab);
    }

    /**
     * Return the current tab position.
     *
     * @return The current tab position
     */
    public int getCurrentPosition() {
        return mCurrentTab;
    }

    /**
     * Given a Tab, find it's position
     *
     * @return position of Tab or -1 if not found
     */
    public int getTabPosition(Tab tab) {
        if (tab == null) {
            LogHelper.i("mTabStackView", "tab==null");
            return -1;
        }
        if (isPrivacyMode) {
            return mPrivacyModeTabs.indexOf(tab);
        } else {
            return mTabs.indexOf(tab);
        }
    }

    public boolean canCreateNewTab() {
        if (isPrivacyMode) {
            return mMaxTabs > mPrivacyModeTabs.size();
        } else {
            return mMaxTabs > mTabs.size();
        }
    }

    public Tab createNewTab() {
        return createNewTab(null);
    }

    public Tab createNewTab(Bundle state) {
        if (!canCreateNewTab()) {
            return null;
        }
        final ExplorerWebView w = createNewWebView();
        // Create a new tab and add it to the tab list
        Tab t = new Tab(mController, w, state);
        if (isPrivacyMode) {
            mPrivacyModeTabs.add(t);
        } else {
            mTabs.add(t);
        }
        if (mController != null) {
            mController.onTabCountChanged();
        }
        // Initially put the tab in the background.
        t.putInBackground();
        return t;
    }

    public boolean removeTab(int index) {
        Log.e(TAG, "removeTab :: index =:" + index + ",getTabCount() =:" + getTabCount());
        if (index < 0 || index >= getTabCount()) {
            return false;
        }
        if (isPrivacyMode) {
            return removeTab(mPrivacyModeTabs.get(index));
        } else {
            return removeTab(mTabs.get(index));
        }
    }

    public void removeTabAll() {
        for (int i = 0; i < mTabs.size(); i++) {
            removeTab(mTabs.get(i));
        }
    }

    /**
     * Remove the tab from the list. If the tab is the current tab shown, the
     * last created tab will be shown.
     *
     * @param t The tab to be removed.
     */
    public boolean removeTab(Tab t) {
        if (t == null) {
            return false;
        }
        if (isPrivacyMode) {
            mPrivacyModeTabs.remove(t);
        } else {
            // Remove t from our list of tabs.
            mTabs.remove(t);
        }
        if (mCurrentTab <= 0) {
            mCurrentTab = 0;
        } else {
            mCurrentTab--;
        }

        // destroy the tab
        t.destroy();

        // Remove it from the queue of viewed tabs.
        mTabQueue.remove(t);
        if (mController != null) {
            mController.onTabCountChanged();
        }
        return true;
    }

    /**
     * Destroy all the tabs and subwindows
     */
    public void destroy() {
        for (Tab t : mTabs) {
            t.destroy();
        }
        mTabs.clear();
        mTabQueue.clear();
    }

    /**
     * Returns the number of tabs created.
     *
     * @return The number of tabs created.
     */
    public int getTabCount() {
        if (isPrivacyMode) {
            return mPrivacyModeTabs.size();
        } else {
            return mTabs.size();
        }
    }

    /**
     * save the tab state:
     * current position
     * position sorted array of tab ids
     * for each tab id, save the tab state
     *
     * @param outState
     */
    public void saveState(Bundle outState) {
        final int numTabs = getTabCount();
        if (numTabs == 0) {
            return;
        }
        long[] ids = new long[numTabs];
        int i = 0;
        for (Tab tab : mTabs) {
            Bundle tabState = tab.saveState();
            if (tabState != null) {
                ids[i++] = tab.getId();
                String key = Long.toString(tab.getId());
                outState.putBundle(key, tabState);
            } else {
                ids[i++] = -1;
                // Since we won't be restoring the thumbnail, delete it
                // tab.deleteThumbnail();
            }
        }
        if (!outState.isEmpty()) {
            outState.putLongArray(POSITIONS, ids);
            Tab current = getCurrentTab();
            long cid = -1;
            if (current != null) {
                cid = current.getId();
            }
            outState.putLong(CURRENT, cid);
        }
    }

    /**
     * Check if the state can be restored.  If the state can be restored, the
     * current tab id is returned.  This can be passed to restoreState below
     * in order to restore the correct tab.  Otherwise, -1 is returned and the
     * state cannot be restored.
     */
    public long canRestoreState(Bundle inState, boolean restoreIncognitoTabs) {
        final long[] ids = (inState == null) ? null : inState.getLongArray(POSITIONS);
        if (ids == null) {
            return -1;
        }
        final long oldcurrent = inState.getLong(CURRENT);
        long current = -1;
        if (restoreIncognitoTabs || (hasState(oldcurrent, inState))) {
            current = oldcurrent;
        } else {
            // pick first non incognito tab
            for (long id : ids) {
                if (hasState(id, inState)) {
                    current = id;
                    break;
                }
            }
        }
        return current;
    }

    private boolean hasState(long id, Bundle state) {
        if (id == -1) return false;
        Bundle tab = state.getBundle(Long.toString(id));
        return ((tab != null) && !tab.isEmpty());
    }

    /**
     * Restore the state of all the tabs.
     *
     * @param currentId            The tab id to restore.
     * @param inState              The saved state of all the tabs.
     * @param restoreIncognitoTabs Restoring private browsing tabs
     * @param restoreAll           All webviews get restored, not just the current tab
     *                             (this does not override handling of incognito tabs)
     */
    public void restoreState(Bundle inState, long currentId,
                             boolean restoreIncognitoTabs, boolean restoreAll) {
        if (currentId == -1) {
            return;
        }
        long[] ids = inState.getLongArray(POSITIONS);
        long maxId = -Long.MAX_VALUE;
        HashMap<Long, Tab> tabMap = new HashMap<Long, Tab>();
        for (long id : ids) {
            if (id > maxId) {
                maxId = id;
            }
            final String idkey = Long.toString(id);
            Bundle state = inState.getBundle(idkey);
            if (state == null || state.isEmpty()) {
                // Skip tab
                continue;
            } else if (id == currentId || restoreAll) {
                Tab t = createNewTab(state);
                if (t == null) {
                    // We could "break" at this point, but we want
                    // sNextId to be set correctly.
                    continue;
                }
                tabMap.put(id, t);
                // Me must set the current tab before restoring the state
                // so that all the client classes are set.
                if (id == currentId) {
                    setCurrentTab(t);
                }
            } else {
                // Create a new tab and don't restore the state yet, add it
                // to the tab list
                Tab t = new Tab(mController, state);
                tabMap.put(id, t);
                mTabs.add(t);
                if (mController != null) {
                    mController.onTabCountChanged();
                }
                // added the tab to the front as they are not current
                mTabQueue.add(0, t);
            }
        }

        // make sure that there is no id overlap between the restored
        // and new tabs
        sNextId = maxId + 1;

        if (mCurrentTab == -1) {
            if (getTabCount() > 0) {
                setCurrentTab(getTab(0));
            }
        }
        // restore parent/child relationships
        /*
        for (long id : ids) {
            final Tab tab = tabMap.get(id);
            final Bundle b = inState.getBundle(Long.toString(id));
            if ((b != null) && (tab != null)) {
                final long parentId = b.getLong(Tab.PARENTTAB, -1);
                if (parentId != -1) {
                    final Tab parent = tabMap.get(parentId);
                    if (parent != null) {
                        parent.addChildTab(tab);
                    }
                }
            }
        }
        */
    }

    /**
     * Free the memory in this order, 1) free the background tabs; 2) free the
     * WebView cache;
     */
    public void freeMemory() {
        if (getTabCount() == 0) return;

        // free the least frequently used background tabs
        Vector<Tab> tabs = null;
        tabs = getHalfLeastUsedTabs(getCurrentTab());
        mFreeTabIndex.clear();
        if (tabs.size() > 0) {
            Log.w(TAG, "Free " + tabs.size() + " tabs in the browser");
            for (Tab t : tabs) {
                mFreeTabIndex.add(getTabPosition(t) + 1);
                // store the WebView's state.
                t.saveState();
                // destroy the tab
                t.destroy();
            }
        }

        // free the WebView's unused memory (this includes the cache)
        Log.w(TAG, "Free WebView's unused memory and cache");
        WebView view = getCurrentWebView();
        if (view != null) {
            view.clearCache(true);
        }
    }

    /**
     * returns the number of visible webviews
     *
     * @return visibleWebview the number of visible webviews
     */
    public int getVisibleWebviewNums() {
        int visibleWebview = 0;
        if (mTabs.size() == 0) {
            return visibleWebview;
        }

        for (Tab t : mTabs) {
            if (t != null && t.getWebView() != null) {
                visibleWebview++;
            }
        }
        return visibleWebview;
    }

    /// M: add for browser memory optimization @ {

    /**
     * @return obtain the free tab indexs
     */
    protected CopyOnWriteArrayList<Integer> getFreeTabIndex() {
        return mFreeTabIndex;
    }

    private Vector<Tab> getHalfLeastUsedTabs(Tab current) {
        Vector<Tab> tabsToGo = new Vector<Tab>();

        // Don't do anything if we only have 1 tab or if the current tab is
        // null.
        if (getTabCount() == 1 || current == null) {
            return tabsToGo;
        }

        if (mTabQueue.size() == 0) {
            return tabsToGo;
        }

        // Rip through the queue starting at the beginning and tear down half of
        // available tabs which are not the current tab or the parent of the
        // current tab.
        int openTabCount = 0;
        for (Tab t : mTabQueue) {
            if (t != null && t.getWebView() != null) {
                openTabCount++;
                if (t != current) {
                    tabsToGo.add(t);
                }
            }
        }

        openTabCount /= 2;
        if (tabsToGo.size() > openTabCount) {
            tabsToGo.setSize(openTabCount);
        }

        return tabsToGo;
    }
    /// @ }

    /// M: Get the list of least used tabs except the current one and its parent
    private Vector<Tab> getLessUsedTabs(Tab current) {
        Vector<Tab> tabsToGo = new Vector<Tab>();

        // Don't do anything if we only have 1 tab or if the current tab is
        // null.
        if (getTabCount() == 1 || current == null) {
            return tabsToGo;
        }

        if (mTabQueue.size() == 0) {
            return tabsToGo;
        }

        // Rip through the queue starting at the beginning and tear down half of
        // available tabs which are not the current tab or the parent of the
        // current tab.
        int openTabCount = 0;
        for (Tab t : mTabQueue) {
            if (t != null && t.getWebView() != null) {
                openTabCount++;
                if (t != current) {
                    tabsToGo.add(t);
                }
            }
        }

        return tabsToGo;
    }

    public Tab getLeastUsedTab(Tab current) {
        if (getTabCount() == 1 || current == null) {
            return null;
        }
        if (mTabQueue.size() == 0) {
            return null;
        }
        // find a tab which is not the current tab or the parent of the
        // current tab
        for (Tab t : mTabQueue) {
            if (t != null && t.getWebView() != null) {
                if (t != current) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * Show the tab that contains the given WebView.
     *
     * @param view The WebView used to find the tab.
     */
    public Tab getTabFromView(WebView view) {
        for (Tab t : mTabs) {
            if (t.getWebView() == view) {
                return t;
            }
        }
        return null;
    }

    /**
     * Stop loading in all opened WebView including subWindows.
     */
    public void stopAllLoading() {
        for (Tab t : mTabs) {
            final WebView webview = t.getWebView();
            if (webview != null) {
                webview.stopLoading();
            }
        }
    }

    // This method checks if a tab matches the given url.
    private boolean tabMatchesUrl(Tab t, String url) {
        return url.equals(t.getUrl()) || url.equals(t.getOriginalUrl());
    }

    /**
     * Return the tab that matches the given url.
     *
     * @param url The url to search for.
     */
    public Tab findTabWithUrl(String url) {
        if (url == null) {
            return null;
        }
        // Check the current tab first.
        Tab currentTab = getCurrentTab();
        if (currentTab != null && tabMatchesUrl(currentTab, url)) {
            return currentTab;
        }
        // Now check all the rest.
        for (Tab tab : mTabs) {
            if (tabMatchesUrl(tab, url)) {
                return tab;
            }
        }
        return null;
    }

    /**
     * Recreate the main WebView of the given tab.
     */
    public void recreateWebView(Tab t) {
        final WebView w = t.getWebView();
        if (w != null) {
            t.destroy();
        }
        // Create a new WebView. If this tab is the current tab, we need to put
        // back all the clients so force it to be the current tab.
        t.setWebView(createNewWebView(), false);
        if (getCurrentTab() == t) {
            setCurrentTab(t, true);
        }
    }

    /**
     * Creates a new WebView and registers it with the global settings.
     */
    private ExplorerWebView createNewWebView() {
        return mController.getWebViewFactory().createWebView();
    }

    /**
     * Put the current tab in the background and set newTab as the current tab.
     *
     * @param newTab The new tab. If newTab is null, the current tab is not
     *               set.
     */
    public boolean setCurrentTab(Tab newTab) {
        return setCurrentTab(newTab, false);
    }

    /**
     * If force is true, this method skips the check for newTab == current.
     */
    private boolean setCurrentTab(Tab newTab, boolean force) {
        Tab current = getTab(mCurrentTab);
        if (current == newTab && !force) {
            return true;
        }
        if (current != null) {
            current.putInBackground();
            mCurrentTab = -1;
        }
        if (newTab == null) {
            return false;
        }

        // Move the newTab to the end of the queue
        int index = mTabQueue.indexOf(newTab);
        if (index != -1) {
            mTabQueue.remove(index);
        }
        mTabQueue.add(newTab);
        if (isPrivacyMode) {
            mCurrentTab = mPrivacyModeTabs.indexOf(newTab);
        } else {
            // Display the new current tab
            mCurrentTab = mTabs.indexOf(newTab);
        }
        ExplorerWebView mainView = newTab.getWebView();
        boolean needRestore = mainView == null;
        if (needRestore) {
            // Same work as in createNewTab() except don't do new Tab()
            mainView = createNewWebView();
            newTab.setWebView(mainView);
        }
        newTab.putInForeground();
        return true;
    }

    // Used by Tab.onJsAlert() and friends
    public void setActiveTab(Tab tab) {
        if (isPrivacyMode) {
            mCurrentTab = mPrivacyModeTabs.indexOf(tab);
        } else {
            mCurrentTab = mTabs.indexOf(tab);
        }
        WebView webView = getCurrentWebView();
        if (webView != null && webView.getParent() != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
        }
    }

    public OnThumbnailUpdatedListener getOnThumbnailUpdatedListener() {
        return mOnThumbnailUpdatedListener;
    }

    public void setOnThumbnailUpdatedListener(OnThumbnailUpdatedListener listener) {
        mOnThumbnailUpdatedListener = listener;
        for (Tab t : mTabs) {
            ExplorerWebView web = t.getWebView();
            if (web != null) {
                // web.setPictureListener(listener != null ? t : null);
            }
        }
    }

    public interface OnThumbnailUpdatedListener {
        void onThumbnailUpdated(Tab t);
    }
}
