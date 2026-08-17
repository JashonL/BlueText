package com.shuoxd.ble.callback;

import androidx.annotation.NonNull;

import com.shuoxd.ble.model.BleDevice;

/**
 * BLE scan callbacks.
 */
public interface BleScanCallback {

    void onScanStarted(boolean success);

    void onScanning(@NonNull BleDevice device);

    void onScanFinished();

    void onScanFailed(int errorCode, @NonNull String message);
}
