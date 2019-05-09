package com.kunfei.bookshelf.help;

import com.kunfei.bookshelf.constant.Constants;

/**
 * Created by baymax on 2019/4/5.
 */
public class SettingHelper {
    public static boolean getInterceptStatus() {
        return PreferencesHelper.getInstance().getBoolean("InterceptStatus", true);
    }

    /**
     * 是否第一次打开 App
     */
    public static boolean isFirstOpen() {
        return PreferencesHelper.getInstance().getBoolean("firstOpen", true);
    }

    /**
     * 获取外网ip
     *
     * @return
     */
    public static String getIp() {
        return PreferencesHelper.getInstance().getString("ip", "113.91.37.103");
    }


    /**
     * 保存外网ip
     */
    public static void saveIp(String ip) {
        PreferencesHelper.getInstance().putString("ip", ip);
    }


    /**
     * 粘贴板赋值
     *
     * @param context
     */
    public static void saveCopyText(String context) {
        PreferencesHelper.getInstance().putString("copy_text", context);
    }

    /**
     * 获取粘贴板
     *
     * @return
     */
    public static String getCopyText() {
        return PreferencesHelper.getInstance().getString("copy_text", "");
    }


    /**
     * 是否第一次显示对话框
     */
    public static boolean isFirstOpenDialog() {
        return PreferencesHelper.getInstance().getBoolean("isFirstOpenDialog", true);
    }

    /**
     * 对话框显示之后保存
     */
    public static void saveOpenDialog() {
        PreferencesHelper.getInstance().putBoolean("isFirstOpenDialog", false);
    }

    /**
     * 制定url加载网页
     */
    public static boolean isLoadFiction() {
        return PreferencesHelper.getInstance().getBoolean("LoadFiction", true);
    }

    /**
     * 制定url加载网页
     */
    public static void saveLoadFiction() {
        PreferencesHelper.getInstance().putBoolean("LoadFiction", false);
    }

    /**
     * 保存默认搜索引擎
     */
    public static void saveDefaultSearch(String defaultSearch) {
        PreferencesHelper.getInstance().putString("defaultSearch", defaultSearch);
    }

    public static String getDefaultSearch() {
        return PreferencesHelper.getInstance().getString("defaultSearch", Constants.DEF_BAIDU);
    }

    public static void saveFontSize(int fontSize) {
        PreferencesHelper.getInstance().putInt("fontSize", fontSize);
    }

    public static int getFontSize() {
        return PreferencesHelper.getInstance().getInt("fontSize", 100);
    }

    /**
     * 是否第一次打开 App
     */
    public static boolean isFirstStart() {
        return PreferencesHelper.getInstance().getBoolean("firstStart", true);
    }

    /**
     * 选择android还是电脑
     */
    public static int isUaCheck() {
        return PreferencesHelper.getInstance().getInt("uaCheck", 0);
    }

    /**
     * 保存选择android还是电脑
     */
    public static void saveUaCheck(int isCheck) {
        PreferencesHelper.getInstance().putInt("uaCheck", isCheck);
    }

    /**
     * 获取隐私模式状态
     */
    public static boolean isPrivacyMode() {
        return PreferencesHelper.getInstance().getBoolean("privacyMode", false);
    }

    /**
     * 保存隐私模式状态
     */
    public static void savePrivacyMode(boolean isCheck) {
        PreferencesHelper.getInstance().putBoolean("privacyMode", isCheck);
    }

    /**
     * 保存最后一次访问的网页
     *
     * @param value
     */
    public static void saveHistory(String value) {
        PreferencesHelper.getInstance().putString("historyUrl", value);
    }


    /**
     * 获取最后一次访问的网页
     *
     * @return
     */
    public static String getHistoryUrl() {
        return PreferencesHelper.getInstance().getString("historyUrl", "");
    }

    /**
     * 小说模式下的字体大小
     */
    public static void saveXiaoShuoFontSize(double fontSizeScale) {
        PreferencesHelper.getInstance().putString("xiaoShuofontSize", fontSizeScale + "");
    }

    /**
     * @return 小说模式下的字体大小
     */
    public static double geteXiaoShuoFontSize() {
        return Double.parseDouble(PreferencesHelper.getInstance().getString("xiaoShuofontSize", "1"));
    }

    /**
     * @param isDayMode 是否白天模式
     */
    public static void saveIsDayMode(boolean isDayMode) {
        PreferencesHelper.getInstance().putBoolean("isDayMode", isDayMode);
    }

    /**
     * @return 是否白天模式
     */
    public static boolean getIsDayMode() {
        return PreferencesHelper.getInstance().getBoolean("isDayMode", true);
    }

    /**
     * 隐私模式网页提示
     */
    public static void savePrivacyHint() {
        PreferencesHelper.getInstance().putBoolean("privacyHint", false);
    }

    /**
     * 隐私模式网页提示
     */
    public static boolean getPrivacyHint() {
        return PreferencesHelper.getInstance().getBoolean("privacyHint", true);
    }

    /**
     * @param isDayMode 小说是否白天模式
     */
    public static void saveFictionIsDayMode(boolean isDayMode) {
        PreferencesHelper.getInstance().putBoolean("isFictionDayMode", isDayMode);
    }


    /**
     * @return 小说是否白天模式
     */
    public static boolean getFictionIsDayMode() {
        return PreferencesHelper.getInstance().getBoolean("isFictionDayMode", true);
    }


    /**
     * @return 获取退出类型
     * 0 按两次退出
     * 1 对话框退出
     */
    public static int getExitType() {
        return PreferencesHelper.getInstance().getInt("exitType", 0);
    }


    /**
     * @return 获取退出类型
     * 0 对话框退出
     * 1 按两次退出
     */
    public static void saveExitType(int type) {
        PreferencesHelper.getInstance().putInt("exitType", type);
    }

    /**
     * 退出是否删除历史记录
     *
     * @return
     */
    public static boolean getExitWithHistory() {
        return PreferencesHelper.getInstance().getBoolean("exitWithHistory", false);
    }

    public static void saveExitWithHistory(boolean delete) {
        PreferencesHelper.getInstance().putBoolean("exitWithHistory", delete);
    }

    public static void saveInterceptStatus(boolean delete) {
        PreferencesHelper.getInstance().putBoolean("InterceptStatus", delete);
    }

    /**
     * 保持apk路径
     */
    public static void saveApkFilePath(String filPath) {
        PreferencesHelper.getInstance().putString("apkFilePath", filPath);
    }
}
