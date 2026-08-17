package com.shuoxd.bluetext.datalogConfig.bluetooth;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.UUID;

/**
 * Kept for legacy callers ({@code BlueToothScanActivity} bind/start).
 * Implementation is delegated to {@link BleSession} (BleClient).
 */
public class BleService extends Service {

    public static final UUID SERVICE_UUID = UUID.fromString(BleSession.SERVICE_UUID);
    public static final UUID RX_TX_CHAR_UUID = UUID.fromString(BleSession.CHAR_UUID);
    public static final String BLE_CONNECTING = BleSession.BLE_CONNECTING;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void connect(String address) {
        BleSession.getInstance().connect(address);
    }

    public void writeCharacteristic(byte[] value) {
        BleSession.getInstance().writeCharacteristic(value);
    }

    public void disconnect() {
        BleSession.getInstance().disconnect();
    }

    public void close() {
        BleSession.getInstance().disconnect();
    }
}
