package com.shuoxd.bluetext.datalogConfig.bluetooth;

import static com.shuoxd.bluetext.DatalogApUtil.int2Byte;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.shuoxd.ble.BleClient;
import com.shuoxd.ble.callback.BleConnectCallback;
import com.shuoxd.ble.callback.BleNotifyCallback;
import com.shuoxd.ble.callback.BleWriteCallback;
import com.shuoxd.ble.model.BleConnectionState;
import com.shuoxd.ble.model.BleDevice;
import com.shuoxd.bluetext.CRC16;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleDisconnectedEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.ConnBleFailMsg;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.NotFoundEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * App-level BLE session on top of {@link BleClient}:
 * connect / MTU / notify / write, plus datalog packet assemble + AES decrypt.
 */
public class BleSession {

    private static final String TAG = "BleSession";

    public static final String SERVICE_UUID = "000000FF-0000-1000-8000-00805f9b34fb";
    public static final String CHAR_UUID = "0000ff01-0000-1000-8000-00805f9b34fb";

    public static final String BLE_CONNECTING = "ACTION_BLE_CONNECTING";

    private static volatile BleSession sInstance;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Queue<byte[]> writeQueue = new LinkedList<>();

    private byte[] receviceData;
    private int negotiatedMtu = 23;
    private boolean writing;
    private boolean hasClickConnected;
    private boolean notifyReady;
    private String connectedMac;

    private final Runnable enableNotifyRunnable = this::enableNotifyInternal;

    private BleSession() {
    }

    @NonNull
    public static BleSession getInstance() {
        if (sInstance == null) {
            synchronized (BleSession.class) {
                if (sInstance == null) {
                    sInstance = new BleSession();
                }
            }
        }
        return sInstance;
    }

    public boolean isConnected() {
        return BleClient.getInstance().isConnected();
    }

    @Nullable
    public String getConnectedMac() {
        return connectedMac;
    }

    public void connect(@NonNull String mac) {
        BleClient client = BleClient.getInstance();
        if (!client.isInitialized()) {
            EventBus.getDefault().post(new ConnBleFailMsg());
            return;
        }

        if (mac.equalsIgnoreCase(connectedMac) && hasClickConnected && client.isConnected()) {
            EventBus.getDefault().post(new NotFoundEvent(true));
            return;
        }

        hasClickConnected = true;
        notifyReady = false;
        connectedMac = mac;
        client.stopScan();
        client.config().setMtu(100);

        client.connect(mac, new BleConnectCallback() {
            @Override
            public void onStartConnect(@NonNull BleDevice device) {
                Log.d(TAG, "onStartConnect " + device.getMac());
            }

            @Override
            public void onConnectSuccess(@NonNull BleDevice device, @NonNull BluetoothGatt gatt) {
                Log.d(TAG, "onConnectSuccess " + device.getMac());
            }

            @Override
            public void onConnectFail(@NonNull BleDevice device, int status, @NonNull String message) {
                Log.e(TAG, "onConnectFail status=" + status + ", " + message);
                hasClickConnected = false;
                EventBus.getDefault().post(new ConnBleFailMsg());
            }

            @Override
            public void onDisconnected(@NonNull BleDevice device, boolean active, int status) {
                Log.d(TAG, "onDisconnected active=" + active + ", status=" + status);
                hasClickConnected = false;
                writing = false;
                writeQueue.clear();
                EventBus.getDefault().post(new BleDisconnectedEvent());
            }

            @Override
            public void onConnectionStateChanged(@NonNull BleConnectionState state) {
                Log.d(TAG, "state=" + state);
            }

            @Override
            public void onServicesDiscovered(@NonNull BleDevice device, @NonNull BluetoothGatt gatt) {
                BluetoothGattService service = gatt.getService(
                        java.util.UUID.fromString(SERVICE_UUID));
                if (service == null) {
                    Log.e(TAG, "service not found");
                    EventBus.getDefault().post(new NotFoundEvent(false));
                    return;
                }
                // Match legacy BleService: notify UI ready, then negotiate MTU + notify.
                EventBus.getDefault().post(new NotFoundEvent(true));
                client.requestMtu(100);
                // Fallback if MTU callback is missing on some firmwares
                scheduleEnableNotify(800);
            }

            @Override
            public void onMtuChanged(@NonNull BleDevice device, int mtu, @Nullable String error) {
                Log.d(TAG, "onMtuChanged mtu=" + mtu + ", error=" + error);
                if (error == null) {
                    negotiatedMtu = mtu;
                }
                scheduleEnableNotify(100);
            }
        });
    }

    private void scheduleEnableNotify(long delayMs) {
        mainHandler.removeCallbacks(enableNotifyRunnable);
        mainHandler.postDelayed(enableNotifyRunnable, delayMs);
    }

    private void enableNotifyInternal() {
        if (notifyReady || !BleClient.getInstance().isConnected()) {
            return;
        }
        notifyReady = true;
        BleClient.getInstance().enableNotify(SERVICE_UUID, CHAR_UUID, true, new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                Log.d(TAG, "notify success");
            }

            @Override
            public void onNotifyFailure(int status, @NonNull String message) {
                Log.e(TAG, "notify fail: " + message);
                notifyReady = false;
            }

            @Override
            public void onCharacteristicChanged(@NonNull byte[] data) {
                handleNotifyData(data);
            }
        });
    }

    public void writeCharacteristic(@Nullable byte[] value) {
        if (value == null || value.length == 0) {
            return;
        }
        if (!BleClient.getInstance().isConnected()) {
            Log.e(TAG, "write while disconnected");
            return;
        }
        receviceData = new byte[0];
        synchronized (writeQueue) {
            int chunkSize = Math.max(20, Math.min(500, negotiatedMtu - 3));
            if (value.length <= chunkSize) {
                writeQueue.offer(value);
            } else {
                for (int offset = 0; offset < value.length; offset += chunkSize) {
                    int len = Math.min(chunkSize, value.length - offset);
                    writeQueue.offer(Arrays.copyOfRange(value, offset, offset + len));
                }
            }
            if (!writing) {
                writing = true;
                flushNextWrite();
            }
        }
    }

    private void flushNextWrite() {
        byte[] next;
        synchronized (writeQueue) {
            next = writeQueue.poll();
            if (next == null) {
                writing = false;
                return;
            }
        }
        final byte[] chunk = next;
        BleClient.getInstance().write(SERVICE_UUID, CHAR_UUID, chunk, false, new BleWriteCallback() {
            @Override
            public void onWriteSuccess(@NonNull byte[] data) {
                Log.d(TAG, "write success len=" + data.length);
                flushNextWrite();
            }

            @Override
            public void onWriteFailure(int status, @NonNull String message) {
                Log.e(TAG, "write fail: " + message);
                synchronized (writeQueue) {
                    writeQueue.clear();
                    writing = false;
                }
            }
        });
    }

    public void disconnect() {
        writing = false;
        writeQueue.clear();
        hasClickConnected = false;
        notifyReady = false;
        mainHandler.removeCallbacks(enableNotifyRunnable);
        BleClient.getInstance().disconnect();
    }

    private void handleNotifyData(@NonNull byte[] dataValues) {
        try {
            if (dataValues.length < 8) {
                return;
            }
            int receiveLen = DatalogApUtil.byte2Int(new byte[]{dataValues[0], dataValues[1]});
            if (receiveLen != dataValues.length - 2) {
                dataCompase(dataValues);
            } else {
                receviceData = dataValues;
                boolean ischeck = checkAndshow();
                if (ischeck) {
                    aesPase();
                    EventBus.getDefault().post(new BleEvent(receviceData));
                } else {
                    receviceData = new byte[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dataCompase(byte[] data) {
        if (receviceData == null) {
            return;
        }
        byte[] compose = new byte[receviceData.length + data.length];
        System.arraycopy(receviceData, 0, compose, 0, receviceData.length);
        System.arraycopy(data, 0, compose, receviceData.length, data.length);
        receviceData = compose;
        boolean ischeck = checkAndshow();
        if (ischeck) {
            try {
                aesPase();
                EventBus.getDefault().post(new BleEvent(receviceData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkAndshow() {
        if (receviceData == null || receviceData.length < 4) {
            return false;
        }
        int receiveLen = DatalogApUtil.byte2Int(new byte[]{receviceData[0], receviceData[1]});
        boolean bLen = receiveLen == receviceData.length - 2;

        byte crcL = receviceData[receviceData.length - 1];
        byte crcH = receviceData[receviceData.length - 2];
        byte[] originalByte = Arrays.copyOfRange(receviceData, 0, receviceData.length - 2);
        int crc = CRC16.calcCrc16(originalByte);
        byte[] crcBytes = int2Byte(crc);
        boolean bCrc = crcBytes[0] == crcH && crcBytes[1] == crcL;
        return bCrc && bLen;
    }

    private void aesPase() throws Exception {
        int datalen = receviceData.length - 10;
        byte[] databytes = new byte[datalen];
        System.arraycopy(receviceData, 8, databytes, 0, datalen);
        byte[] bytes = DatalogApUtil.msgDesCodeByAESCBC(databytes);

        byte[] realByte = {receviceData[4], receviceData[5]};
        byte[] crc = {receviceData[receviceData.length - 2], receviceData[receviceData.length - 1]};

        int realLen = DatalogApUtil.byte2Int(realByte) - 2;
        byte[] dataBytes = new byte[Math.max(realLen, 0)];
        if (realLen >= 0) {
            System.arraycopy(bytes, 0, dataBytes, 0, realLen);
        }

        datalen = dataBytes.length + 10;
        byte[] allData = new byte[datalen];
        System.arraycopy(receviceData, 0, allData, 0, 8);
        System.arraycopy(dataBytes, 0, allData, 8, dataBytes.length);
        System.arraycopy(crc, 0, allData, 8 + dataBytes.length, crc.length);
        receviceData = allData;
    }
}
