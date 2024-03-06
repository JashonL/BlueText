package com.shuoxd.bluetext.datalogConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

public class LocalUtil {

    //打开gps定位
    public static final int OPEN_GPS_CODE = 102;

    /**
     * 检查GPS是否打开
     *
     */
    public static boolean checkGPSIsOpen(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }



    /**
     * 去手机设置打开GPS
     *
     * @param activity
     */
    public static void goToOpenGPS(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        activity.startActivityForResult(intent, OPEN_GPS_CODE);

    }




}
