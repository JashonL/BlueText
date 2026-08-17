package com.shuoxd.ble.callback;

import androidx.annotation.NonNull;

/**
 * Write characteristic result.
 */
public interface BleWriteCallback {

    void onWriteSuccess(@NonNull byte[] data);

    void onWriteFailure(int status, @NonNull String message);
}
