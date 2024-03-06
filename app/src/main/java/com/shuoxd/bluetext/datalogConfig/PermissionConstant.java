package com.shuoxd.bluetext.datalogConfig;

import android.Manifest;

public class PermissionConstant {
    public static final int RC_CAMERA = 0X01;
    public static final int RC_LOCATION = 0X02;
    public static final int CHANGE_WIFI_STATE = 0X03;

    //ble
    public static final String[] BLE_SCAN = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT};
}
