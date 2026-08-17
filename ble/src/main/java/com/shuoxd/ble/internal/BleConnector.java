package com.shuoxd.ble.internal;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuoxd.ble.BleConfig;
import com.shuoxd.ble.callback.BleConnectCallback;
import com.shuoxd.ble.callback.BleNotifyCallback;
import com.shuoxd.ble.callback.BleReadCallback;
import com.shuoxd.ble.callback.BleWriteCallback;
import com.shuoxd.ble.model.BleConnectionState;
import com.shuoxd.ble.model.BleDevice;
import com.shuoxd.ble.util.BleUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * BLE connect + GATT read/write/notify.
 */
public class BleConnector {

    private static final UUID CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final Context appContext;
    private final BleConfig config;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable
    private BluetoothGatt bluetoothGatt;
    @Nullable
    private BleDevice currentDevice;
    @Nullable
    private BleConnectCallback connectCallback;
    @Nullable
    private BleWriteCallback writeCallback;
    @Nullable
    private BleReadCallback readCallback;
    @Nullable
    private BleNotifyCallback notifyCallback;

    private BleConnectionState connectionState = BleConnectionState.DISCONNECTED;
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    private boolean activeDisconnect;
    private int requestedMtu;

    private final Runnable connectTimeoutRunnable = () -> {
        if (connectionState == BleConnectionState.CONNECTING && currentDevice != null) {
            BleUtils.e("connect timeout");
            closeGatt();
            notifyConnectFail(currentDevice, -100, "Connect timeout");
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            BleUtils.d("onConnectionStateChange status=" + status + ", newState=" + newState);
            mainHandler.removeCallbacks(connectTimeoutRunnable);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                boolean canRetry = connectionState == BleConnectionState.CONNECTING
                        && reconnectAttempt.get() < config.getReconnectCount();
                closeGatt();
                if (canRetry && currentDevice != null) {
                    scheduleReconnect();
                    return;
                }
                if (currentDevice != null) {
                    notifyConnectFail(currentDevice, status, "Connection state change failed: " + status);
                }
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                setState(BleConnectionState.CONNECTED);
                reconnectAttempt.set(0);
                boolean discovering = gatt.discoverServices();
                BleUtils.d("discoverServices=" + discovering);
                if (currentDevice != null && connectCallback != null) {
                    mainHandler.post(() -> connectCallback.onConnectSuccess(currentDevice, gatt));
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                BleDevice device = currentDevice;
                boolean wasActive = activeDisconnect;
                closeGatt();
                setState(BleConnectionState.DISCONNECTED);
                if (device != null && connectCallback != null) {
                    mainHandler.post(() -> connectCallback.onDisconnected(device, wasActive, status));
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            BleUtils.d("onServicesDiscovered status=" + status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                if (currentDevice != null) {
                    notifyConnectFail(currentDevice, status, "discoverServices failed: " + status);
                }
                return;
            }
            setState(BleConnectionState.SERVICES_DISCOVERED);
            if (currentDevice != null && connectCallback != null) {
                mainHandler.post(() -> connectCallback.onServicesDiscovered(currentDevice, gatt));
            }
            if (requestedMtu > 23) {
                requestMtuInternal(requestedMtu);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            byte[] value = characteristic.getValue() == null
                    ? new byte[0]
                    : characteristic.getValue().clone();
            BleWriteCallback cb = writeCallback;
            writeCallback = null;
            if (cb == null) {
                return;
            }
            mainHandler.post(() -> {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    cb.onWriteSuccess(value);
                } else {
                    cb.onWriteFailure(status, "write failed: " + status);
                }
            });
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            byte[] value = characteristic.getValue() == null
                    ? new byte[0]
                    : characteristic.getValue().clone();
            BleReadCallback cb = readCallback;
            readCallback = null;
            if (cb == null) {
                return;
            }
            mainHandler.post(() -> {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    cb.onReadSuccess(value);
                } else {
                    cb.onReadFailure(status, "read failed: " + status);
                }
            });
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] value = characteristic.getValue() == null
                    ? new byte[0]
                    : characteristic.getValue().clone();
            BleNotifyCallback cb = notifyCallback;
            if (cb != null) {
                mainHandler.post(() -> cb.onCharacteristicChanged(value));
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor,
                                      int status) {
            BleNotifyCallback cb = notifyCallback;
            if (cb == null) {
                return;
            }
            mainHandler.post(() -> {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    cb.onNotifySuccess();
                } else {
                    cb.onNotifyFailure(status, "enable notify failed: " + status);
                }
            });
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            BleUtils.d("onMtuChanged mtu=" + mtu + ", status=" + status);
            if (currentDevice == null || connectCallback == null) {
                return;
            }
            String error = status == BluetoothGatt.GATT_SUCCESS ? null : ("mtu failed: " + status);
            int resultMtu = status == BluetoothGatt.GATT_SUCCESS ? mtu : 23;
            mainHandler.post(() -> connectCallback.onMtuChanged(currentDevice, resultMtu, error));
        }
    };

    public BleConnector(@NonNull Context context, @NonNull BleConfig config) {
        this.appContext = context.getApplicationContext();
        this.config = config;
        this.requestedMtu = config.getMtu();
    }

    @Nullable
    public BleDevice getCurrentDevice() {
        return currentDevice;
    }

    @Nullable
    public BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }

    public BleConnectionState getConnectionState() {
        return connectionState;
    }

    public boolean isConnected() {
        return connectionState == BleConnectionState.CONNECTED
                || connectionState == BleConnectionState.SERVICES_DISCOVERED;
    }

    @SuppressLint("MissingPermission")
    public void connect(@NonNull BleDevice device, @NonNull BleConnectCallback callback) {
        connect(device, config.isAutoConnect(), callback);
    }

    @SuppressLint("MissingPermission")
    public void connect(@NonNull BleDevice device, boolean autoConnect, @NonNull BleConnectCallback callback) {
        if (!BleUtils.isBluetoothEnabled(appContext)) {
            callback.onConnectFail(device, -1, "Bluetooth is disabled");
            return;
        }
        if (!BleUtils.hasConnectPermission(appContext)) {
            callback.onConnectFail(device, -2, "Missing BLUETOOTH_CONNECT permission");
            return;
        }
        if (device.getDevice() == null) {
            callback.onConnectFail(device, -3, "BluetoothDevice is null");
            return;
        }
        if (isConnected() && currentDevice != null
                && currentDevice.getMac().equalsIgnoreCase(device.getMac())) {
            BleUtils.w("already connected to " + device.getMac());
            callback.onConnectSuccess(device, bluetoothGatt);
            return;
        }

        disconnectInternal(true);
        this.connectCallback = callback;
        this.currentDevice = device;
        this.activeDisconnect = false;
        this.reconnectAttempt.set(0);
        this.requestedMtu = config.getMtu();

        setState(BleConnectionState.CONNECTING);
        mainHandler.post(() -> callback.onStartConnect(device));
        openGatt(device.getDevice(), autoConnect);
    }

    @SuppressLint("MissingPermission")
    public void connect(@NonNull String mac, @NonNull BleConnectCallback callback) {
        android.bluetooth.BluetoothAdapter adapter = BleUtils.getBluetoothAdapter(appContext);
        if (adapter == null) {
            callback.onConnectFail(new BleDevice(null, mac, 0, null, null), -3, "BluetoothAdapter is null");
            return;
        }
        BluetoothDevice remote;
        try {
            remote = adapter.getRemoteDevice(mac);
        } catch (IllegalArgumentException e) {
            callback.onConnectFail(new BleDevice(null, mac, 0, null, null), -4, "Invalid MAC: " + mac);
            return;
        }
        BleDevice device = new BleDevice(BleUtils.safeDeviceName(remote), mac, 0, remote, null);
        connect(device, callback);
    }

    @SuppressLint("MissingPermission")
    private void openGatt(@NonNull BluetoothDevice device, boolean autoConnect) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bluetoothGatt = device.connectGatt(appContext, autoConnect, gattCallback,
                        BluetoothDevice.TRANSPORT_LE);
            } else {
                bluetoothGatt = device.connectGatt(appContext, autoConnect, gattCallback);
            }
            mainHandler.removeCallbacks(connectTimeoutRunnable);
            mainHandler.postDelayed(connectTimeoutRunnable, config.getConnectTimeoutMs());
            BleUtils.d("connectGatt to " + device.getAddress());
        } catch (SecurityException e) {
            if (currentDevice != null) {
                notifyConnectFail(currentDevice, -5, "SecurityException: " + e.getMessage());
            }
        } catch (Exception e) {
            if (currentDevice != null) {
                notifyConnectFail(currentDevice, -6, "connectGatt error: " + e.getMessage());
            }
        }
    }

    private void scheduleReconnect() {
        int attempt = reconnectAttempt.incrementAndGet();
        BleUtils.d("reconnect attempt " + attempt);
        if (currentDevice == null || currentDevice.getDevice() == null) {
            return;
        }
        setState(BleConnectionState.CONNECTING);
        mainHandler.postDelayed(() -> {
            if (currentDevice != null
                    && currentDevice.getDevice() != null
                    && connectionState == BleConnectionState.CONNECTING) {
                openGatt(currentDevice.getDevice(), config.isAutoConnect());
            }
        }, config.getReconnectIntervalMs());
    }

    public void disconnect() {
        disconnectInternal(true);
    }

    @SuppressLint("MissingPermission")
    private void disconnectInternal(boolean notify) {
        activeDisconnect = true;
        mainHandler.removeCallbacks(connectTimeoutRunnable);
        if (bluetoothGatt != null) {
            setState(BleConnectionState.DISCONNECTING);
            try {
                bluetoothGatt.disconnect();
            } catch (Exception e) {
                BleUtils.w("disconnect exception: " + e.getMessage());
            }
            closeGatt();
        }
        setState(BleConnectionState.DISCONNECTED);
        if (!notify) {
            connectCallback = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void closeGatt() {
        if (bluetoothGatt != null) {
            try {
                bluetoothGatt.close();
            } catch (Exception e) {
                BleUtils.w("gatt close exception: " + e.getMessage());
            }
            bluetoothGatt = null;
        }
    }

    public void requestMtu(int mtu) {
        this.requestedMtu = Math.max(23, Math.min(517, mtu));
        if (isConnected()) {
            requestMtuInternal(requestedMtu);
        }
    }

    @SuppressLint("MissingPermission")
    private void requestMtuInternal(int mtu) {
        if (bluetoothGatt == null) {
            return;
        }
        boolean ok = bluetoothGatt.requestMtu(mtu);
        BleUtils.d("requestMtu(" + mtu + ")=" + ok);
    }

    @Nullable
    public List<BluetoothGattService> getServices() {
        return bluetoothGatt == null ? null : bluetoothGatt.getServices();
    }

    @SuppressLint("MissingPermission")
    public void write(@NonNull String serviceUuid,
                      @NonNull String characteristicUuid,
                      @NonNull byte[] data,
                      boolean withResponse,
                      @NonNull BleWriteCallback callback) {
        if (!ensureReady(callback)) {
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
        if (characteristic == null) {
            callback.onWriteFailure(-10, "Characteristic not found");
            return;
        }
        int writeType = withResponse
                ? BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                : BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
        characteristic.setWriteType(writeType);
        characteristic.setValue(data);
        this.writeCallback = callback;
        boolean ok = bluetoothGatt.writeCharacteristic(characteristic);
        if (!ok) {
            this.writeCallback = null;
            callback.onWriteFailure(-11, "writeCharacteristic returned false");
        }
    }

    @SuppressLint("MissingPermission")
    public void read(@NonNull String serviceUuid,
                     @NonNull String characteristicUuid,
                     @NonNull BleReadCallback callback) {
        if (bluetoothGatt == null || !isConnected()) {
            callback.onReadFailure(-20, "Not connected");
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
        if (characteristic == null) {
            callback.onReadFailure(-21, "Characteristic not found");
            return;
        }
        this.readCallback = callback;
        boolean ok = bluetoothGatt.readCharacteristic(characteristic);
        if (!ok) {
            this.readCallback = null;
            callback.onReadFailure(-22, "readCharacteristic returned false");
        }
    }

    @SuppressLint("MissingPermission")
    public void enableNotify(@NonNull String serviceUuid,
                             @NonNull String characteristicUuid,
                             boolean enable,
                             @NonNull BleNotifyCallback callback) {
        if (bluetoothGatt == null || !isConnected()) {
            callback.onNotifyFailure(-30, "Not connected");
            return;
        }
        BluetoothGattCharacteristic characteristic = findCharacteristic(serviceUuid, characteristicUuid);
        if (characteristic == null) {
            callback.onNotifyFailure(-31, "Characteristic not found");
            return;
        }
        boolean setNotify = bluetoothGatt.setCharacteristicNotification(characteristic, enable);
        if (!setNotify) {
            callback.onNotifyFailure(-32, "setCharacteristicNotification failed");
            return;
        }
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor == null) {
            // Some firmwares omit CCCD; local notification may still work.
            this.notifyCallback = callback;
            callback.onNotifySuccess();
            return;
        }
        byte[] value;
        int props = characteristic.getProperties();
        if (enable) {
            if ((props & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
            } else {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
        } else {
            value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
        }
        descriptor.setValue(value);
        this.notifyCallback = callback;
        boolean ok = bluetoothGatt.writeDescriptor(descriptor);
        if (!ok) {
            this.notifyCallback = null;
            callback.onNotifyFailure(-33, "writeDescriptor returned false");
        }
    }

    private boolean ensureReady(@NonNull BleWriteCallback callback) {
        if (bluetoothGatt == null || !isConnected()) {
            callback.onWriteFailure(-9, "Not connected");
            return false;
        }
        return true;
    }

    @Nullable
    private BluetoothGattCharacteristic findCharacteristic(@NonNull String serviceUuid,
                                                           @NonNull String characteristicUuid) {
        if (bluetoothGatt == null) {
            return null;
        }
        try {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUuid));
            if (service == null) {
                return null;
            }
            return service.getCharacteristic(UUID.fromString(characteristicUuid));
        } catch (IllegalArgumentException e) {
            BleUtils.e("invalid uuid: " + e.getMessage());
            return null;
        }
    }

    private void setState(@NonNull BleConnectionState state) {
        this.connectionState = state;
        BleConnectCallback cb = connectCallback;
        if (cb != null) {
            mainHandler.post(() -> cb.onConnectionStateChanged(state));
        }
    }

    private void notifyConnectFail(@NonNull BleDevice device, int status, @NonNull String message) {
        setState(BleConnectionState.DISCONNECTED);
        BleConnectCallback cb = connectCallback;
        if (cb != null) {
            mainHandler.post(() -> cb.onConnectFail(device, status, message));
        }
    }

    public void release() {
        disconnectInternal(false);
        connectCallback = null;
        writeCallback = null;
        readCallback = null;
        notifyCallback = null;
        currentDevice = null;
        mainHandler.removeCallbacksAndMessages(null);
    }
}
