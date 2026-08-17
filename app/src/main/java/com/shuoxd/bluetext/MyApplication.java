package com.shuoxd.bluetext;

import android.app.Application;

import com.shuoxd.ble.BleClient;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleService;
import com.shuoxd.bluetext.datalogConfig.bluetooth.BleSession;

public class MyApplication extends Application {

    private static MyApplication instance;

    /** @deprecated Prefer {@link #getBleSession()}; kept for legacy config pages. */
    private BleService gBleServer;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BleClient.getInstance()
                .init(this)
                .config()
                .setEnableLog(true)
                .setScanTimeoutMs(10_000)
                .setConnectTimeoutMs(10_000)
                .setMtu(100)
                .setReconnectCount(1);
        // Ensure session singleton is created after BleClient.init
        BleSession.getInstance();
    }

    public BleSession getBleSession() {
        return BleSession.getInstance();
    }

    public BleService getgBleServer() {
        return gBleServer;
    }

    public void setgBleServer(BleService gBleServer) {
        this.gBleServer = gBleServer;
    }
}
