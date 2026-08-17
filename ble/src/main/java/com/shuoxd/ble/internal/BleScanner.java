package com.shuoxd.ble.internal;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuoxd.ble.BleConfig;
import com.shuoxd.ble.callback.BleScanCallback;
import com.shuoxd.ble.model.BleDevice;
import com.shuoxd.ble.util.BleUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * BLE scan implementation.
 */
public class BleScanner {

    private final Context appContext;
    private final BleConfig config;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean scanning = new AtomicBoolean(false);

    @Nullable
    private BleScanCallback scanCallback;
    @Nullable
    private BluetoothLeScanner leScanner;

    private final ScanCallback androidScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            dispatchDevice(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            if (results == null) {
                return;
            }
            for (ScanResult result : results) {
                dispatchDevice(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            BleUtils.e("scan failed, code=" + errorCode);
            scanning.set(false);
            mainHandler.post(() -> {
                if (scanCallback != null) {
                    scanCallback.onScanFailed(errorCode, "BLE scan failed: " + errorCode);
                }
            });
        }
    };

    private final Runnable scanTimeoutRunnable = this::stopScanInternal;

    public BleScanner(@NonNull Context context, @NonNull BleConfig config) {
        this.appContext = context.getApplicationContext();
        this.config = config;
    }

    public boolean isScanning() {
        return scanning.get();
    }

    @SuppressLint("MissingPermission")
    public void startScan(@Nullable List<ScanFilter> filters, @NonNull BleScanCallback callback) {
        if (scanning.get()) {
            BleUtils.w("already scanning");
            return;
        }
        this.scanCallback = callback;

        if (!BleUtils.isBleSupported(appContext)) {
            callback.onScanFailed(-1, "BLE not supported");
            return;
        }
        if (!BleUtils.isBluetoothEnabled(appContext)) {
            callback.onScanFailed(-2, "Bluetooth is disabled");
            return;
        }
        if (!BleUtils.hasScanPermission(appContext)) {
            callback.onScanFailed(-3, "Missing BLE scan permission");
            return;
        }

        BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(appContext);
        if (adapter == null) {
            callback.onScanFailed(-4, "BluetoothAdapter is null");
            return;
        }
        leScanner = adapter.getBluetoothLeScanner();
        if (leScanner == null) {
            callback.onScanFailed(-5, "BluetoothLeScanner is null");
            return;
        }

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> filterList = filters == null ? new ArrayList<>() : filters;

        try {
            scanning.set(true);
            leScanner.startScan(filterList, settings, androidScanCallback);
            mainHandler.post(() -> callback.onScanStarted(true));
            mainHandler.removeCallbacks(scanTimeoutRunnable);
            mainHandler.postDelayed(scanTimeoutRunnable, config.getScanTimeoutMs());
            BleUtils.d("scan started, timeout=" + config.getScanTimeoutMs());
        } catch (SecurityException e) {
            scanning.set(false);
            callback.onScanFailed(-6, "SecurityException: " + e.getMessage());
        } catch (Exception e) {
            scanning.set(false);
            callback.onScanFailed(-7, "startScan error: " + e.getMessage());
        }
    }

    public void stopScan() {
        stopScanInternal();
    }

    @SuppressLint("MissingPermission")
    private void stopScanInternal() {
        if (!scanning.compareAndSet(true, false)) {
            return;
        }
        mainHandler.removeCallbacks(scanTimeoutRunnable);
        try {
            if (leScanner != null) {
                leScanner.stopScan(androidScanCallback);
            }
        } catch (Exception e) {
            BleUtils.w("stopScan exception: " + e.getMessage());
        }
        BleUtils.d("scan stopped");
        mainHandler.post(() -> {
            if (scanCallback != null) {
                scanCallback.onScanFinished();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void dispatchDevice(@Nullable ScanResult result) {
        if (result == null || result.getDevice() == null) {
            return;
        }
        String mac = result.getDevice().getAddress();
        String name = BleUtils.safeDeviceName(result.getDevice());
        if (name == null && result.getScanRecord() != null) {
            name = result.getScanRecord().getDeviceName();
        }
        byte[] record = result.getScanRecord() == null ? null : result.getScanRecord().getBytes();
        BleDevice device = new BleDevice(name, mac, result.getRssi(), result.getDevice(), record);
        mainHandler.post(() -> {
            if (scanCallback != null && scanning.get()) {
                scanCallback.onScanning(device);
            }
        });
    }
}
