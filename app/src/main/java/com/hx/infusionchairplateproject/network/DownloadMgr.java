package com.hx.infusionchairplateproject.network;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.hx.infusionchairplateproject.EntiretyApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * APK下载管理类
 */
public class DownloadMgr {
    //    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int MAXIMUM_POOL_SIZE = 3;
    Executor mExecutor = Executors.newFixedThreadPool(MAXIMUM_POOL_SIZE);
    static DownloadMgr mDownloadMgr;
    static Object obj = new Object();
    HashMap<String, DownloadTask> mTasks = new HashMap<String, DownloadTask>();

    public static void init() {
        getInstance();
    }

    public static DownloadMgr getInstance() {
        synchronized (obj) {
            if (mDownloadMgr == null) {
                mDownloadMgr = new DownloadMgr();
            }
        }
        return mDownloadMgr;
    }

    public void addTask(String downloadUrl, String filePath, Callback callback) {
        if (!mTasks.containsKey(downloadUrl)) {
            mTasks.put(downloadUrl, new DownloadTask(downloadUrl, filePath, callback));
        }
        mTasks.get(downloadUrl).startDownload();
    }

    public void removeTask(String downloadUrl, String filePath, Callback callback) {
        if (mTasks.containsKey(downloadUrl)) {
            mTasks.get(downloadUrl).cancel();
        }
        mTasks.remove(downloadUrl);
    }

    public class DownloadTask implements Runnable {
        private String downloadUrl;
        private String filePath;
        Callback callback;

        public DownloadTask(String downloadUrl, String filePath, Callback callback) {
            this.downloadUrl = downloadUrl;
            this.filePath = filePath;
            this.callback = callback;
        }

        public void startDownload() {
            mExecutor.execute(this);
        }


        @Override
        public void run() {
            runResumable(downloadUrl, filePath, callback);
        }

        synchronized boolean cancel() {
            if (thread == null) {
                return false;
            }

            thread.interrupt();
            return true;
        }
    }

    Thread thread;

    public void runResumable(String downloadUrl, String filePath, Callback callback) {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        thread = Thread.currentThread();
        final Context ctx = EntiretyApplication.context;
        String msg = "";
        boolean interrupted = false;

        HttpURLConnection conn = null;
        long resumePosition = 0;
        final File file = new File(filePath);

        try {
            //20160720 add
            final File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            callback.onStart(downloadUrl);

            //非wifi环境不下载
            if (!isWifiActive(ctx)) {
                msg = "请在wifi环境下下载";
                callback.onFailed(downloadUrl, true, msg);
                return;
            }

            resumePosition = file.exists() ? file.length() : 0;
            // Create connection object
            conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setConnectTimeout(50000);
            conn.setReadTimeout(50000);

            conn.setDoInput(true);
            conn.setUseCaches(false);

            // Make the request
            conn.setRequestMethod("GET");
//            conn.setRequestProperty("User-Agent", "Java/Android");
//            conn.setRequestProperty("Connection", "close");
//            conn.setRequestProperty("Http-version", "HTTP/1.1");
//            conn.setRequestProperty("Cache-Control", "no-transform");
//            if (resumePosition > 0) {
//                //断点续传的关键设置Range
//                conn.setRequestProperty("Range", "bytes=" + resumePosition + "-");
//            }

            conn.connect();

            final int responseCode = conn.getResponseCode();
            Log.i("responseCode", "==" + responseCode);
            if (responseCode == 416) {
                msg = "已经下载！";
                callback.onFailed(downloadUrl, true, msg);
                return;
            }
            if (responseCode != 200 && responseCode != 206) {
                msg = "网络繁忙，请稍后再试！";
                callback.onFailed(downloadUrl, true, msg);

                return;
            }

            long fileLength = conn.getContentLength();
            /**
             * 本地apk是完整的话 就不需要累加了 resumePosition > 0 为false
             * responseCode为200代表服务器不支持分块传输 这里就也不累加了
             */
            if (resumePosition >= fileLength || responseCode == 200) {
                resumePosition = 0;
            }
            InputStream is = new BufferedInputStream(conn.getInputStream());
            FileOutputStream fos = new FileOutputStream(file, resumePosition > 0);
            try {
                int read = 0;
                long progress = resumePosition;
                byte[] buffer = new byte[4096 * 2];
                while ((read = is.read(buffer)) > 0 && !(interrupted = Thread.interrupted())) {
                    try {
                        fos.write(buffer, 0, read);
                    } catch (Exception e) {
                        msg = "磁盘空间已满，无法下载";
                        throw e;
                    }

                    // progress
                    progress += read;
                    callback.onProgress(downloadUrl, progress, fileLength);

                }
            } finally {
                fos.flush();
                is.close();
            }

            if (file.exists()) {
                //检验数据是否完整
                if (file.length() == fileLength + resumePosition) {
                    callback.onSuccess(downloadUrl, file.length());
                    return;
                }
            }

        } catch (Exception e) {
            interrupted = interrupted || Thread.interrupted() || (e instanceof InterruptedIOException && !(e instanceof SocketTimeoutException));
            msg = "网络异常，下载失败;";

            if (interrupted) {
                msg = "下载被中断！";
            }
            deleteFile(filePath);//下载失败删除对应文件apk

            callback.onFailed(downloadUrl, true, msg);
        } finally {
            disconnect(conn);
        }
    }

    static void disconnect(HttpURLConnection conn) {
        try {
            if (conn == null) {
                return;
            }

            conn.disconnect();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static abstract class Callback {


        public void onStart(String url) {
        }

        public void onProgress(String url, long progress, long total) {
        }

        public void onSuccess(String url, long l) {
        }

        public void onFailed(String url, boolean cancelled, String msg) {
        }
    }

    public boolean isWifiActive(Context ctx) {
        try {
            ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mgr.getActiveNetworkInfo();
            return (info != null) ? info.getType() == ConnectivityManager.TYPE_WIFI : false;
        } catch (Exception e) {
            return false;
        }
    }

    public void cancelAllTask() {
        try {
            if (mTasks != null) {
                for (String taskKey : mTasks.keySet()) {
                    mTasks.get(taskKey).cancel();
                }
            }
        } catch (Exception e) {
//            CommonUtil.printStackTrace(e);
        }

    }

    public void cancelTask(String key) {
        try {
            if (mTasks != null) {
                mTasks.get(key).cancel();
            }
        } catch (Exception e) {
//            CommonUtil.printStackTrace(e);
        }
    }

    public static boolean checkNetAvailable(Context ctx) {
        try {
            ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mgr.getActiveNetworkInfo();
            return (info != null) ? true : false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }
}