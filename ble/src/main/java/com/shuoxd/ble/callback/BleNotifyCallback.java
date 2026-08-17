package com.shuoxd.ble.callback;

import androidx.annotation.NonNull;

/**
 * Notify / indicate callbacks.
 */
public interface BleNotifyCallback {

    void onNotifySuccess();

    void onNotifyFailure(int status, @NonNull String message);

    void onCharacteristicChanged(@NonNull byte[] data);
}
