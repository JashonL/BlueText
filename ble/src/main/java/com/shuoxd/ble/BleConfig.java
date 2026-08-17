package com.shuoxd.ble;

/**
 * Runtime configuration for {@link BleClient}.
 */
public class BleConfig {

    private boolean enableLog = true;
    private long scanTimeoutMs = 10_000L;
    private long connectTimeoutMs = 10_000L;
    private long operateTimeoutMs = 5_000L;
    private int reconnectCount = 0;
    private long reconnectIntervalMs = 2_000L;
    private int mtu = 23;
    private boolean autoConnect = false;

    public boolean isEnableLog() {
        return enableLog;
    }

    public BleConfig setEnableLog(boolean enableLog) {
        this.enableLog = enableLog;
        return this;
    }

    public long getScanTimeoutMs() {
        return scanTimeoutMs;
    }

    public BleConfig setScanTimeoutMs(long scanTimeoutMs) {
        this.scanTimeoutMs = Math.max(1_000L, scanTimeoutMs);
        return this;
    }

    public long getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public BleConfig setConnectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = Math.max(1_000L, connectTimeoutMs);
        return this;
    }

    public long getOperateTimeoutMs() {
        return operateTimeoutMs;
    }

    public BleConfig setOperateTimeoutMs(long operateTimeoutMs) {
        this.operateTimeoutMs = Math.max(1_000L, operateTimeoutMs);
        return this;
    }

    public int getReconnectCount() {
        return reconnectCount;
    }

    public BleConfig setReconnectCount(int reconnectCount) {
        this.reconnectCount = Math.max(0, reconnectCount);
        return this;
    }

    public long getReconnectIntervalMs() {
        return reconnectIntervalMs;
    }

    public BleConfig setReconnectIntervalMs(long reconnectIntervalMs) {
        this.reconnectIntervalMs = Math.max(200L, reconnectIntervalMs);
        return this;
    }

    public int getMtu() {
        return mtu;
    }

    /**
     * Preferred ATT MTU. Actual negotiated value may be lower.
     */
    public BleConfig setMtu(int mtu) {
        this.mtu = Math.max(23, Math.min(517, mtu));
        return this;
    }

    public boolean isAutoConnect() {
        return autoConnect;
    }

    public BleConfig setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
        return this;
    }
}
