package com.shuoxd.ble.callback;

import androidx.annotation.NonNull;

/**
 * Read characteristic result.
 */
public interface BleReadCallback {

    void onReadSuccess(@NonNull byte[] data);

    void onReadFailure(int status, @NonNull String message);
}
