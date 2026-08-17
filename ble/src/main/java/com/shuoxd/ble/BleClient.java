package com.shuoxd.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanFilter;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuoxd.ble.callback.BleConnectCallback;
import com.shuoxd.ble.callback.BleNotifyCallback;
import com.shuoxd.ble.callback.BleReadCallback;
import com.shuoxd.ble.callback.BleScanCallback;
import com.shuoxd.ble.callback.BleWriteCallback;
import com.shuoxd.ble.internal.BleConnector;
import com.shuoxd.ble.internal.BleScanner;
import com.shuoxd.ble.model.BleConnectionState;
import com.shuoxd.ble.model.BleDevice;
import com.shuoxd.ble.util.BleUtils;

import java.util.List;

/**
 * BLE facade: scan / connect / communicate.
 * <pre>
 * BleClient.getInstance()
 *     .init(context)
 *     .config()
 *     .setScanTimeoutMs(10000)
 *     .setMtu(100);
 *
 * BleClient.getInstance().startScan(callback);
 * BleClient.getInstance().connect(device, callback);
 * BleClient.getInstance().write(serviceUuid, charUuid, data, true, callback);
 * BleClient.getInstance().enableNotify(serviceUuid, charUuid, true, callback);
 * </pre>
 */
public final class BleClient {

    private static volatile BleClient sInstance;

    private Context appContext;
    private BleConfig config = new BleConfig();
    private BleScanner scanner;
    private BleConnector connector;
    private boolean initialized;

    private BleClient() {
    }

    @NonNull
    public static BleClient getInstance() {
        if (sInstance == null) {
            synchronized (BleClient.class) {
                if (sInstance == null) {
                    sInstance = new BleClient();
                }
            }
        }
        return sInstance;
    }

    /**
     * Must be called once (e.g. in Application.onCreate) before other APIs.
     */
    @NonNull
    public synchronized BleClient init(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
        BleUtils.setEnableLog(config.isEnableLog());
        this.scanner = new BleScanner(appContext, config);
        this.connector = new BleConnector(appContext, config);
        this.initialized = true;
        BleUtils.d("BleClient initialized");
        return this;
    }

    /**
     * Mutable config. Call before scan/connect for best results.
     */
    @NonNull
    public BleConfig config() {
        return config;
    }

    @NonNull
    public BleClient setConfig(@NonNull BleConfig config) {
        this.config = config;
        BleUtils.setEnableLog(config.isEnableLog());
        if (initialized && appContext != null) {
            // Recreate scanner with new config; keep connector to avoid dropping an active link.
            this.scanner = new BleScanner(appContext, config);
        }
        return this;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isBleSupported() {
        ensureInit();
        return BleUtils.isBleSupported(appContext);
    }

    public boolean isBluetoothEnabled() {
        ensureInit();
        return BleUtils.isBluetoothEnabled(appContext);
    }

    // -------------------- Scan --------------------

    public void startScan(@NonNull BleScanCallback callback) {
        startScan(null, callback);
    }

    public void startScan(@Nullable List<ScanFilter> filters, @NonNull BleScanCallback callback) {
        ensureInit();
        scanner.startScan(filters, callback);
    }

    public void stopScan() {
        ensureInit();
        scanner.stopScan();
    }

    public boolean isScanning() {
        ensureInit();
        return scanner.isScanning();
    }

    // -------------------- Connect --------------------

    public void connect(@NonNull BleDevice device, @NonNull BleConnectCallback callback) {
        ensureInit();
        stopScan();
        connector.connect(device, callback);
    }

    public void connect(@NonNull String mac, @NonNull BleConnectCallback callback) {
        ensureInit();
        stopScan();
        connector.connect(mac, callback);
    }

    public void disconnect() {
        ensureInit();
        connector.disconnect();
    }

    public boolean isConnected() {
        return initialized && connector != null && connector.isConnected();
    }

    @NonNull
    public BleConnectionState getConnectionState() {
        if (!initialized || connector == null) {
            return BleConnectionState.DISCONNECTED;
        }
        return connector.getConnectionState();
    }

    @Nullable
    public BleDevice getConnectedDevice() {
        return initialized && connector != null ? connector.getCurrentDevice() : null;
    }

    @Nullable
    public BluetoothGatt getBluetoothGatt() {
        return initialized && connector != null ? connector.getBluetoothGatt() : null;
    }

    @Nullable
    public List<BluetoothGattService> getServices() {
        return initialized && connector != null ? connector.getServices() : null;
    }

    public void requestMtu(int mtu) {
        ensureInit();
        connector.requestMtu(mtu);
    }

    // -------------------- Communicate --------------------

    /**
     * Write bytes to a characteristic.
     *
     * @param withResponse true = WRITE_TYPE_DEFAULT, false = WRITE_TYPE_NO_RESPONSE
     */
    public void write(@NonNull String serviceUuid,
                      @NonNull String characteristicUuid,
                      @NonNull byte[] data,
                      boolean withResponse,
                      @NonNull BleWriteCallback callback) {
        ensureInit();
        connector.write(serviceUuid, characteristicUuid, data, withResponse, callback);
    }

    public void write(@NonNull String serviceUuid,
                      @NonNull String characteristicUuid,
                      @NonNull byte[] data,
                      @NonNull BleWriteCallback callback) {
        write(serviceUuid, characteristicUuid, data, true, callback);
    }

    public void read(@NonNull String serviceUuid,
                     @NonNull String characteristicUuid,
                     @NonNull BleReadCallback callback) {
        ensureInit();
        connector.read(serviceUuid, characteristicUuid, callback);
    }

    public void enableNotify(@NonNull String serviceUuid,
                             @NonNull String characteristicUuid,
                             boolean enable,
                             @NonNull BleNotifyCallback callback) {
        ensureInit();
        connector.enableNotify(serviceUuid, characteristicUuid, enable, callback);
    }

    /**
     * Release GATT and clear callbacks. Call in Application teardown if needed.
     */
    public synchronized void destroy() {
        if (scanner != null) {
            scanner.stopScan();
        }
        if (connector != null) {
            connector.release();
        }
        initialized = false;
        scanner = null;
        connector = null;
        appContext = null;
        BleUtils.d("BleClient destroyed");
    }

    private void ensureInit() {
        if (!initialized || appContext == null || scanner == null || connector == null) {
            throw new IllegalStateException("BleClient not initialized. Call BleClient.getInstance().init(context) first.");
        }
    }
}
