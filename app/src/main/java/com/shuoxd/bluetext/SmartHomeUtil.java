package com.shuoxd.bluetext;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;



import org.json.JSONException;
import org.json.JSONObject;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/6/13.
 */

public class SmartHomeUtil {
    public static final String CHARGE_SHINE_LOGIN = "shineLogin";
    public static final String CHARGE_SHINE_OUT = "shineLoginout";

    /**
     * map数组转成String
     *
     * @param map
     * @return
     */
    public static String mapToJsonString(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }




    /**
     * byte数组转为String
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            String hexString = Integer.toHexString(aByte & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result.append(hexString.toUpperCase());
        }
        return result.toString();
    }

    /**
     * 数据解密密钥
     */
    public static byte[] commonkeys = {(byte) 0x5A, (byte) 0xA5, (byte) 0x5A, (byte) 0xA5};

    public static byte[] decodeKey(byte[] src, byte[] keys) {
        if (src == null) return null;
        for (int j = 0; j < src.length; j++)    // Payload数据做掩码处理
        {
            src[j] = (byte) (src[j] ^ keys[j % 4]);
        }
        return src;
    }

    /**
     * 新的密钥
     */
    public static byte[] newkeys = new byte[4];

    public static byte[] createKey() {
        Random randomno = new Random();
        byte[] nbyte = new byte[4];
        randomno.nextBytes(nbyte);
        setNewkeys(nbyte);
        return nbyte;
    }

    public static byte[] getNewkeys() {
        return newkeys;
    }

    private static void setNewkeys(byte[] keys) {
        newkeys = keys;
    }

    /**
     * @param buffer 有效数据数组
     * @return
     */
    public static byte getCheckSum(byte[] buffer) {
        int sum = 0;
        int length = buffer.length;
        for (int i = 0; i < length - 2; i++) {
            sum += (int) buffer[i];
        }
        return (byte) (sum & 0xff);
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray byte[]
     * @return String
     */
    public static String bytetoString(byte[] bytearray) {
        try {
            int length = 0;
            for (int i = 0; i < bytearray.length; ++i) {
                if (bytearray[i] == 0) {
                    length = i;
                    break;
                }
            }
            return new String(bytearray, 0, length, "ascii");
        } catch (Exception e) {
            return "";
        }
    }


    /**
     * 不显示科学计数法
     */
    public static String showDouble(double value) {
        DecimalFormat df = new DecimalFormat("0.#####");
        return df.format(value);
    }



    /**
     * 获取资源文件绝对路径
     *
     * @param id
     * @return
     */
    public static String getResourcesUri(@NonNull Context context, @DrawableRes int id) {
        Resources resources = context.getResources();
        String uriPath = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                resources.getResourcePackageName(id) + "/" +
                resources.getResourceTypeName(id) + "/" +
                resources.getResourceEntryName(id);
        return uriPath;
    }

    /**
     * 根据毫秒值返回时间
     *
     * @param ms        毫秒值
     * @param strFormat 时间的格式
     * @return
     */
    public static String getTimeByMills(long ms, String strFormat) {
        Date date = new Date(ms);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat(strFormat);
        String time = sdf.format(date);
        return time;
    }






    /**
     * 判断集合是否为空
     */
    public static boolean isEmpty(Collection collection) {
        return null == collection || collection.isEmpty();
    }


    public static final String TUYA_DEMO = "zk007";



    public static List<String> getWeeks(Context context) {
        List<String> weeks = new ArrayList<>();
        weeks.add(context.getString(R.string.m386周一));
        weeks.add(context.getString(R.string.m387周二));
        weeks.add(context.getString(R.string.m388周三));
        weeks.add(context.getString(R.string.m389周四));
        weeks.add(context.getString(R.string.m390周五));
        weeks.add(context.getString(R.string.m391周六));
        weeks.add(context.getString(R.string.m392周日));
        return weeks;
    }



    public static double divide(double v1, double v2, int scale) {
        if (v2 == 0) return 0;
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(v2 + "");
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 匹配是否为数字
     */
    public static boolean isNumeric(String str) {
        // 该正则表达式可以匹配所有的数字 包括负数
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        String bigStr;
        try {
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            return false;//异常 说明包含非数字。
        }

        Matcher isNum = pattern.matcher(bigStr); // matcher是全匹配
        return isNum.matches();
    }


    /**
     * 判断是否为合法IP * @return the ip
     */
    public static boolean isboolIp(String ipAddress) {
        if (TextUtils.isEmpty(ipAddress))
            return false;
        Pattern pattern = Pattern.compile("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
        Matcher matcher = pattern.matcher(ipAddress);
        return matcher.find();
    }


    /**
     * 只有数字
     *
     * @param string
     * @return
     */
    public static boolean isNumberiZidai(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (!Character.isDigit(string.charAt(i))) return false;
        }
        return true;

    }


    /**
     * 字母 数字 下划线 空格
     *
     * @param s
     * @return
     */
    public static boolean isLetterDigit2(String s) {
        String regex = "[a-z,0-9A-Z_ -]*";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(s).matches();
    }


    /**
     * 是否包含特殊字符
     *
     * @return
     */
    public static boolean specificSymbol(String content) {
        //是否包含特殊字符
        boolean allSpecific = false;

        String regex = ".,?!:@;+=#/()_-`^*&..$<>[]{}";
        for (int i = 0; i < content.length(); i++) {
            boolean isNumChart = isLetterDigit2(String.valueOf(content.charAt(i)));
            boolean isSpecific = false;
            if (!isNumChart) {
                for (int j = 0; j < regex.length(); j++) {
                    if (regex.charAt(j) == content.charAt(i)) {
                        isSpecific = true;
                        break;
                    }
                }
            }

            if (!isNumChart && !isSpecific) {
                allSpecific = true;
                break;
            }
        }
        return allSpecific;

    }


    public static String ByteToString(byte[] bytes) {


        if (bytes == null) return "";

        StringBuilder strBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            if (aByte != 0) {
                strBuilder.append((char) aByte);
            } else {
                break;
            }

        }
        return strBuilder.toString();
    }

    /**
     * wifi输入限制
     */
    public static boolean isWiFiLetter(String s) {
        boolean isChinese = isContainChinese(s);
        if (isChinese) return false;
        boolean isValidate = validateLegalString(s);
        return !isValidate;
    }


    /**
     * 判断字符串中是否包含中文
     *
     * @param str 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        return m.find();
    }


    /**
     * 验证字符串内容是否包含下列非法字符<br>
     * `~!#%^&*=+\\|{};:'\",<>/?○●★☆☉♀♂※¤╬の〆
     *
     * @param content 字符串内容
     * @return 't'代表不包含非法字符，otherwise代表包含非法字符。
     */
    public static boolean validateLegalString(String content) {
        String illegal = "\\'\",/";
        boolean isLegalChar = false;
        for (int i = 0; i < content.length(); i++) {
            for (int j = 0; j < illegal.length(); j++) {
                if (content.charAt(i) == illegal.charAt(j)) {
                    isLegalChar = true;
                    break;
                }
            }
        }
        return isLegalChar;
    }

    /**
     * 获取字母列表
     */

    public static List<String> getLetter() {
        List<String> letters = new ArrayList<>();
        for (int i = 0; i < 26; i++) {
            char letter = (char) ('A' + i);
            letters.add(String.valueOf(letter));
        }
        return letters;
    }






    //将16进制的字符串转成int数值，不足的补0
    public static int hexStringToInter(String value) {
        if (TextUtils.isEmpty(value)) return 0;
        return Integer.parseInt(value, 16);
    }


    /**
     * 返回循环条件
     */

    public static String getLoopValue(Context context, String loopType, String loopValue) {
        StringBuilder loopStyle = new StringBuilder();
        if ("-1".equals(loopType)) {
            loopStyle = new StringBuilder(context.getString(R.string.m384单次));
        } else if ("0".equals(loopType)) {
            loopStyle = new StringBuilder(context.getString(R.string.m186每天));
        } else if ("1".equals(loopType)) {
            if (loopValue.equals("1111111")) {
                loopStyle = new StringBuilder(context.getString(R.string.m186每天));
            } else {
                char[] chars = loopValue.toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (String.valueOf(chars[i]).equals("1")) {
                        String week = SmartHomeUtil.getWeeks(context).get(i);
                        loopStyle.append(week).append(",");
                    }
                }
            }
        } else {
            return null;
        }
        String cycleDay = loopStyle.toString();
        if (cycleDay.endsWith(",")) {
            cycleDay = cycleDay.substring(0, cycleDay.length() - 1);
        }
        return cycleDay;
    }



    /**
     * 获取24小时
     */
    public static List<String> getHours() {
        List<String> hours = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            if (hour < 10) hours.add("0" + hour);
            else hours.add(String.valueOf(hour));
        }
        return hours;
    }

    /**
     * 获取60分钟
     */
    public static List<String> getMins() {
        List<String> getMins = new ArrayList<>();
        for (int min = 0; min < 60; min++) {
            if (min < 10) getMins.add("0" + min);
            else getMins.add(String.valueOf(min));
        }
        return getMins;
    }

    /**
     * 获取新语言
     *
     * @return
     */
    public static List<String> getZones() {
        List<String> zones = new ArrayList<>();
        zones.add("UTC-12:00");
        zones.add("UTC-11:00");
        zones.add("UTC-10:00");
        zones.add("UTC-09:00");
        zones.add("UTC-08:00");
        zones.add("UTC-07:00");
        zones.add("UTC-06:00");
        zones.add("UTC-05:00");
        zones.add("UTC-04:00");
        zones.add("UTC-03:00");
        zones.add("UTC-02:00");
        zones.add("UTC-01:00");
        zones.add("UTC+00:00");
        zones.add("UTC+01:00");
        zones.add("UTC+02:00");
        zones.add("UTC+03:00");
        zones.add("UTC+04:00");
        zones.add("UTC+05:00");
        zones.add("UTC+06:00");
        zones.add("UTC+07:00");
        zones.add("UTC+08:00");
        zones.add("UTC+09:00");
        zones.add("UTC+10:00");
        zones.add("UTC+11:00");
        zones.add("UTC+12:00");
        return zones;
    }


    /**
     * 判断字符串是否是"null"
     */


    public static boolean isStringEmpty(String s) {
        return "null".equals(s);
    }


    //将int数值转成4位的16进制的字符串，不足的补0
    public static String integerToHexstring(int value, int scale) {
        String hex = Integer.toHexString(value);
        if (hex.length() < scale) {
            StringBuilder suff = new StringBuilder();
            for (int i = 0; i < scale - hex.length(); i++) {
                suff.append("0");
            }
            hex = suff.toString() + hex;
        }
        return hex;
    }

    public static String insertChart(String res) {
        if (res == null) return "";
        char[] chars = res.toCharArray();
        StringBuilder newString = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            newString.append(chars[i]);
            if (i != chars.length - 1) {
                newString.append("_");
            }
        }
        return newString.toString();
    }


    /**
     * 将byte[2]转成byte[2]
     *
     * @return
     */
    public static int byte2Int(byte[] b) {
        int value = 0;
        if (b.length > 0) {
//            value = (b[0] & 0xff << 8) | (b[1] & 0xff);
            value = 0x000000ff & b[0];
            return value;

        }
        return value;
    }





    public static String getWeekByLoop(Context context, String loop) {
        if ("1111111".equals(loop)) {
            return context.getString(R.string.m186每天);
        } else if ("0000000".equals(loop)) {
            return context.getString(R.string.m384单次);
        } else {
            String[] weeks = {context.getString(R.string.m386周一), context.getString(R.string.m387周二),
                    context.getString(R.string.m388周三), context.getString(R.string.m389周四), context.getString(R.string.m390周五),
                    context.getString(R.string.m391周六), context.getString(R.string.m392周日)};
            char[] chars = loop.toCharArray();
            StringBuilder loopStyle = new StringBuilder();
            for (int i = 0; i < chars.length; i++) {
                if (String.valueOf(chars[i]).equals("1")) {
                    String week = weeks[i];
                    loopStyle.append(week).append(",");
                }
            }
            String cycleDay = loopStyle.toString();
            if (cycleDay.endsWith(",")) {
                cycleDay = cycleDay.substring(0, cycleDay.length() - 1);
            }
            return cycleDay;
        }


    }


}
