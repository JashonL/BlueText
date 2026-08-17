package com.shuoxd.ble.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * BLE helpers: adapter, permission, logging.
 */
public final class BleUtils {

    private static final String TAG = "ShuoxdBle";
    private static boolean sEnableLog = true;

    private BleUtils() {
    }

    public static void setEnableLog(boolean enable) {
        sEnableLog = enable;
    }

    public static void d(@NonNull String msg) {
        if (sEnableLog) {
            Log.d(TAG, msg);
        }
    }

    public static void e(@NonNull String msg) {
        if (sEnableLog) {
            Log.e(TAG, msg);
        }
    }

    public static void w(@NonNull String msg) {
        if (sEnableLog) {
            Log.w(TAG, msg);
        }
    }

    @Nullable
    public static BluetoothManager getBluetoothManager(@NonNull Context context) {
        return (BluetoothManager) context.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Nullable
    public static BluetoothAdapter getBluetoothAdapter(@NonNull Context context) {
        BluetoothManager manager = getBluetoothManager(context);
        return manager == null ? null : manager.getAdapter();
    }

    public static boolean isBleSupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean isBluetoothEnabled(@NonNull Context context) {
        BluetoothAdapter adapter = getBluetoothAdapter(context);
        return adapter != null && adapter.isEnabled();
    }

    public static boolean hasScanPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasConnectPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    @SuppressLint("MissingPermission")
    @Nullable
    public static String safeDeviceName(@Nullable android.bluetooth.BluetoothDevice device) {
        if (device == null) {
            return null;
        }
        try {
            return device.getName();
        } catch (SecurityException e) {
            return null;
        }
    }

    @NonNull
    public static String bytesToHex(@Nullable byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
