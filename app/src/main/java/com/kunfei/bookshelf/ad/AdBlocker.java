package com.kunfei.bookshelf.ad;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.export.external.interfaces.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class AdBlocker {
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init() {
        /*FileDownloadQueueSet queueSet = new FileDownloadQueueSet(new FileDownloadListener() {
            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                LogHelper.i("adblockqiu", task.getTargetFilePath());
                Observable.just(task.getTargetFilePath())
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String s) throws Exception {
                                try {
                                    FileInputStream stream = new FileInputStream(task.getTargetFilePath());
                                    InputStreamReader inputStreamReader = new InputStreamReader(stream);
                                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                    String line;
                                    while ((line = bufferedReader.readLine()) != null) {
                                        AD_HOSTS.add(line);
                                    }
                                    bufferedReader.close();
                                    inputStreamReader.close();
                                    stream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
            }

            @Override
            protected void warn(BaseDownloadTask task) {
            }
        });
        queueSet.disableCallbackProgressTimes();
        queueSet.setAutoRetryTimes(1);
        queueSet.downloadTogether(FileDownloader.getImpl().create("http://jstapi.fbkjapp.com/content/host.txt")
                .setPath(FileDownloadUtils.generateFilePath(FileDownloadUtils.getDefaultSaveRootPath(), "host.txt")).setTag("txt"));
        queueSet.start();*/
    }

    public static boolean isAd(String url) {
        try {
            return isAdHost(getHost(url)) || AD_HOSTS.contains(Uri.parse(url).getLastPathSegment());
        } catch (MalformedURLException e) {
            Log.d("AmniX", e.toString());
            return false;
        }

    }

    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public static String getHost(String url) throws MalformedURLException {
        return new URL(url).getHost();
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

}