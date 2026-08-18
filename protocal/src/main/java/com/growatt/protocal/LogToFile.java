package com.growatt.protocal;

import android.content.Context;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 将日志记录到文件中
 */
public final class LogToFile {

    /**
     * 日志文件夹
     */
    private static String LOG_FILE_DIR = "device_log";

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private LogToFile() {
    }

    private final static class Holder {
        private final static LogToFile INSTANCE = new LogToFile();
    }

    public static LogToFile instance() {
        return Holder.INSTANCE;
    }

    /**
     * 遍历删除过期的文件
     * 超过15天的文件就是过期文件
     */
    public void deleteExpiredFiles() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Context context = ApplicationImpl.INSTANCE().getApplicationContext();
                if (context == null) {
                    return;
                }
                File fileDir = new File(context.getFilesDir(), LOG_FILE_DIR);
                long currentTimeMillis = System.currentTimeMillis();
                if (fileDir.exists()) {
                    File[] listFiles = fileDir.listFiles();
                    if (listFiles != null) {
                        for (File file : listFiles) {
                            String formatDate = "";
                            String fileName = file.getName();
                            if (fileName.contains("_")) {
                                formatDate = fileName.substring(fileName.lastIndexOf("_") + 1);
                            } else {
                                formatDate = fileName;
                            }
                            long fileDate = from_yyyy_MM_dd_format(formatDate).getTime();
                            if (currentTimeMillis - fileDate > 15 * 24 * 60 * 60 * 1000) {
                                file.delete();
                            }
                        }
                    }
                }
            }
        });
    }

    private File getLogFile() {
        return getShareLogFile(0, true);
    }

    /**
     * 单线程池，统一在一个线程中记录日志，避免多线程操作日志出现混乱
     *
     * @param logMsg 日志内容
     */
    public void addLogToFile(String logMsg) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(getLogFile(), true);
                    fileOutputStream.write(String.format("%s---------%s", yyyy_MM_dd_HH_mm_ss_SSS_format(System.currentTimeMillis()), logMsg).getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.write("\n".getBytes(StandardCharsets.UTF_8));
                    fileOutputStream.flush();
                } catch (Exception e) {

                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (Exception Exception) {
                        }
                    }
                }
            }
        });
    }

    /**
     * 获取日志文件
     * 运维用户：文件位置：/data/data/<app包名>/files/device_log/用户名_年月日.txt
     * 终端用户：文件位置：/data/data/<app包名>/files/device_log/年月日.txt
     *
     * @param reduceDays 减少的天数，0代表当天
     */
    public @Nullable File getShareLogFile(int reduceDays, boolean isCreate) {
        Context context = ApplicationImpl.INSTANCE().getApplicationContext();
        if (context == null) {
            return null;
        }
        File fileDir = new File(context.getFilesDir(), LOG_FILE_DIR);
        if (!fileDir.exists()) {
            if (!fileDir.mkdir()) {
                //建文件夹失败，返回null
                return null;
            }
        }
        String date = yyyy_MM_dd_format(System.currentTimeMillis() - (long) reduceDays * 24 * 60 * 60 * 1000);
        File logFile = new File(fileDir, String.format("%s.txt", date));
        if (logFile.exists()) {
            return logFile;
        } else {
            try {
                if (isCreate && logFile.createNewFile()) {
                    return logFile;
                }
            } catch (IOException ignored) {

            }
        }
        return null;
    }

    private final SimpleDateFormat yyyy_MM_dd_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat yyyy_MM_dd_HH_mm_ss_SSS_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

    private String yyyy_MM_dd_format(Long time) {
        return yyyy_MM_dd_format.format(new Date(time));
    }

    private String yyyy_MM_dd_HH_mm_ss_SSS_format(Long time) {
        return yyyy_MM_dd_HH_mm_ss_SSS_format.format(new Date(time));
    }

    private Date from_yyyy_MM_dd_format(String date) {
        try {
            return yyyy_MM_dd_format.parse(date);
        } catch (ParseException e) {
            return new Date();
        }
    }

}
