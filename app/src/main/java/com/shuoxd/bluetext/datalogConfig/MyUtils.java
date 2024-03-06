package com.shuoxd.bluetext.datalogConfig;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyUtils {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";


    public static void showTotalTime(Context mContext, TextView textView) {
        final Calendar c = Calendar.getInstance();
        StringBuilder sb = new StringBuilder();
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(mContext,
                // 绑定监听器
                (view, year, monthOfYear, dayOfMonth) -> {
                    sb.append(year).append("-").append((monthOfYear + 1) < 10 ? ("0" + (monthOfYear + 1)) : (monthOfYear + 1))
                            .append("-").append((dayOfMonth < 10 ? ("0" + dayOfMonth) : dayOfMonth));
                    sb.append(" ").append(new SimpleDateFormat("HH:mm:ss").format(new Date()));
                    textView.setText(sb.toString());
                    // 创建一个TimePickerDialog实例，并把它显示出来
                    new TimePickerDialog(mContext,
                            // 绑定监听器
                            (view1, hourOfDay, minute) -> {
                                int second = c.get(Calendar.SECOND);
                                try {
                                    sb.replace(11, sb.length(), "");
                                    sb.append(" ").append(hourOfDay < 10 ? "0" + hourOfDay : hourOfDay)
                                            .append(":").append(minute < 10 ? "0" + minute : minute)
                                            .append(":").append(second < 10 ? "0" + second : second);
                                    textView.setText(sb.toString());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            // 设置初始时间
                            , c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),
                            // true表示采用24小时制
                            true) {
                        @Override
                        protected void onStop() {
//                                super.onStop();
                        }
                    }.show();
                }
                // 设置初始日期
                , c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                .get(Calendar.DAY_OF_MONTH)) {
            @Override
            protected void onStop() {
//                super.onStop();
            }
        }.show();
    }



    public static String getFormatDate(String dateFromat, Date date) {
        if (TextUtils.isEmpty(dateFromat)) {
            dateFromat = DATE_FORMAT;
        }
        if (date == null) {
            date = new Date();
        }
        return getDateFormat(dateFromat).format(date);
    }

    /**
     * 获取SimpleDateFormat对象，线程安全
     *
     * @param dateFormat："yyyy-MM-dd HH:mm:ss"
     * @return
     */
    public static DateFormat getDateFormat(String dateFormat) {
        return new SimpleDateFormat(dateFormat);
    }



}
