package com.shuoxd.ble;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.shuoxd.ble.callback.BlePermissionCallback;
import com.shuoxd.ble.util.BleUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * BLE runtime permissions by Android version:
 * <ul>
 *     <li>Android 12+: {@code BLUETOOTH_SCAN} + {@code BLUETOOTH_CONNECT}</li>
 *     <li>Android 11 and below: location (required for BLE scan)</li>
 * </ul>
 */
public final class BlePermission {

    public static final int REQUEST_CODE = 0xB1E;

    @Nullable
    private static BlePermissionCallback pendingCallback;

    private BlePermission() {
    }

    @NonNull
    public static String[] requiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        }
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    public static boolean hasPermissions(@NonNull Context context) {
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request missing BLE permissions. Result is delivered to {@code callback}
     * after {@link #handleResult(Activity, int, String[], int[])}.
     */
    public static void request(@NonNull Activity activity, @NonNull BlePermissionCallback callback) {
        if (hasPermissions(activity)) {
            callback.onGranted();
            return;
        }
        pendingCallback = callback;
        ActivityCompat.requestPermissions(activity, requiredPermissions(), REQUEST_CODE);
        BleUtils.d("request BLE permissions");
    }

    /**
     * @return true if this result belongs to a BLE permission request
     */
    public static boolean handleResult(@NonNull Activity activity,
                                       int requestCode,
                                       @NonNull String[] permissions,
                                       @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE) {
            return false;
        }
        BlePermissionCallback callback = pendingCallback;
        pendingCallback = null;
        if (callback == null) {
            return true;
        }
        if (isAllGranted(grantResults)) {
            callback.onGranted();
            return true;
        }
        boolean neverAskAgain = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED
                    && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                neverAskAgain = true;
                break;
            }
        }
        callback.onDenied(neverAskAgain);
        return true;
    }

    public static void clearPendingCallback() {
        pendingCallback = null;
    }

    private static boolean isAllGranted(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public static List<String> missingPermissions(@NonNull Context context) {
        List<String> missing = new ArrayList<>();
        for (String permission : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }
        return missing;
    }
}
