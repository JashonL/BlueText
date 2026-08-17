package com.shuoxd.ble.callback;

import android.bluetooth.BluetoothGatt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuoxd.ble.model.BleConnectionState;
import com.shuoxd.ble.model.BleDevice;

/**
 * BLE connection lifecycle callbacks.
 */
public interface BleConnectCallback {

    void onStartConnect(@NonNull BleDevice device);

    void onConnectSuccess(@NonNull BleDevice device, @NonNull BluetoothGatt gatt);

    void onConnectFail(@NonNull BleDevice device, int status, @NonNull String message);

    void onDisconnected(@NonNull BleDevice device, boolean active, int status);

    void onConnectionStateChanged(@NonNull BleConnectionState state);

    /**
     * Optional: called after {@link BluetoothGatt#discoverServices()} succeeds.
     */
    default void onServicesDiscovered(@NonNull BleDevice device, @NonNull BluetoothGatt gatt) {
    }

    /**
     * Optional: MTU negotiation result.
     */
    default void onMtuChanged(@NonNull BleDevice device, int mtu, @Nullable String error) {
    }
}
