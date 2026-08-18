package com.growatt.protocal;

import android.util.Log;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 本地通讯管理类
 */
public final class LocalManager {

    private final static String LOG_TAG = "Local_log";
    /**
     * 打印Log日志到文件
     */
    public static void log(String msg) {
        Log.i(LOG_TAG, new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US).format(new Date(System.currentTimeMillis())) + "----------" + msg);
        LogToFile.instance().addLogToFile(msg);
    }




}
