package com.shuoxd.bluetext.datalogConfig.bluetooth;



import static com.shuoxd.bluetext.DatalogApUtil.int2Byte;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.shuoxd.bluetext.CRC16;
import com.shuoxd.bluetext.DatalogApUtil;
import com.shuoxd.bluetext.SmartHomeUtil;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleDisconnectedEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.BleEvent;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.ConnBleFailMsg;
import com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus.NotFoundEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.UUID;


/**
 * @author ZhuLiuBin
 * Date: 2021/11/26
 */
public class BleService extends Service {

    private final static String TAG = BleService.class.getSimpleName();
    public static final UUID SERVICE_UUID = UUID.fromString("000000FF-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_TX_CHAR_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");

    //服务UUID
    private final String SERVICE_ID = "000000FF-0000-1000-8000-00805f9b34fb";
    //写入UUID
    private final String WRITE_ID = "0000ff01-0000-1000-8000-00805f9b34fb";

    public static final String BLE_CONNECTING = "ACTION_BLE_CONNECTING";


    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private String mBluetoothDeviceAddress;
    private boolean hasClickConnected = false;

    private BleDevice mBleDevice;


    private byte[] receviceData;

    //蓝牙GATT特性
    private BluetoothGattCharacteristic writeChar = null;


 /*   private final BluetoothGattCallback mGattCallBack = new BluetoothGattCallback() {

        //连接状态回调
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.v(TAG, String.format("onConnectionStateChange: status = %d, newState = %d", status, newState));
            //操作成功的情况下
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    //搜索GATT服务
                    mBluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    EventBus.getDefault().post(new BleDisconnectedEvent());
                }
            } else {
                //连接失败 异常断开
                EventBus.getDefault().post(new ConnBleFailMsg());
            }
        }

        //发现到服务回调
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.v(TAG, String.format("onServicesDiscovered: status = %d", status));
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取指定uuid的service
                BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
                mBluetoothGatt.requestMtu(400);
                //获取到服务不为空
                if (gattService != null) {
                    enableNotification(gattService);
                } else {
                    //获取服务失败
                    Log.d(TAG, "service not found!");
                    mBluetoothGatt.discoverServices();
                    EventBus.getDefault().post(new NotFoundEvent(false));
                }
            }
        }

        //特征写入回调
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.v(TAG, String.format("onCharacteristicRead: service = %s, character = %s, value = %s, status = %d",
                    characteristic.getService().getUuid(),
                    characteristic.getUuid(),
                    SmartHomeUtil.bytetoString(characteristic.getValue()),
                    status));
            //读取到值，在这里读数据
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String value = SmartHomeUtil.bytesToHexString(characteristic.getValue());
                Log.d(TAG, "onCharacteristicRead: value:" + value);
            }

        }


        //外设特征值改变回调
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            try {
                //获取外设修改的特征值
                byte[] dataValues = characteristic.getValue();
                if (dataValues.length < 8) {
                    return;
                }
                LogUtil.d("蓝牙回应数据......................" + SmartHomeUtil.bytesToHexString(dataValues));
                //数据校验
                int receiveLen = DatalogApUtil.byte2Int(new byte[]{dataValues[0], dataValues[1]});
                if (receiveLen != dataValues.length - 2) {
                    dataCompase(dataValues);
                } else {
                    //AES解密
                    int datalen = dataValues.length - 10;
                    byte[] databytes = new byte[datalen];
                    System.arraycopy(dataValues, 8, databytes, 0, datalen);
                    byte[] bytes = DatalogApUtil.msgDesCodeByAESCBC(databytes);
                    LogUtil.d("解密的数据......................" + SmartHomeUtil.bytesToHexString(bytes));
                    System.arraycopy(databytes, 0, dataValues, 8, datalen);
                    receviceData = dataValues;
                    checkAndshow();
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };*/


    private void dataCompase(byte[] data) {
        if (receviceData == null) return;
        byte[] compose = new byte[receviceData.length + data.length];
        System.arraycopy(receviceData, 0, compose, 0, receviceData.length);
        System.arraycopy(data, 0, compose, receviceData.length, data.length);
        receviceData = compose;
        boolean ischeck = checkAndshow();
        if (ischeck){
            try {
                aesPase();
                EventBus.getDefault().post(new BleEvent(receviceData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private boolean checkAndshow() {
        //数据校验
        int receiveLen = DatalogApUtil.byte2Int(new byte[]{receviceData[0], receviceData[1]});
        boolean b_len = receiveLen == receviceData.length - 2;
        //crc校验
        //获取crc效验
        byte crcL = receviceData[receviceData.length - 1];
        byte crcH = receviceData[receviceData.length - 2];

        //获取CRC之外的数据
        byte[] originalByte = Arrays.copyOfRange(receviceData, 0, receviceData.length - 2);
        int crc = CRC16.calcCrc16(originalByte);
        byte[] crcBytes = int2Byte(crc);
        boolean b_crc = crcBytes[0] == crcH && crcBytes[1] == crcL;
        //            EventBus.getDefault().post(new BleEvent(bytes));
        return b_crc && b_len;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance().enableLog(true)
                .setReConnectCount(1, 5000)
                .setSplitWriteNum(600)
                .setConnectOverTime(10000)
                .setOperateTimeout(5000);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.d(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        Log.d(TAG, "ble success initialize.");
        return true;
    }

//    public boolean connect(final String address) {
//        if (mBluetoothAdapter == null || address == null) {
//            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        // Previously connected device.  Try to reconnect.
//        if (address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            if (mBluetoothGatt.connect()) {
//                broadcastUpdate(BLE_CONNECTING);
//                return true;
//            } else {
//                mBluetoothGatt = null;
//                EventBus.getDefault().post(new ConnBleFailMsg());
//                return false;
//            }
//        }
//
//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//        if (device == null) {
//            Log.d(TAG, "Device not found.  Unable to connect");
//            return false;
//        }
//        // We want to directly connect to the device, so we are setting the autoConnect
//        // parameter to false.
//        mBluetoothGatt = device.connectGatt(this, false, mGattCallBack);
//        mBluetoothDeviceAddress = address;
//        broadcastUpdate(BLE_CONNECTING);
//        return true;
//    }

    // fastble lib
    public void connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        // Previously connected device.  Try to reconnect.
        if (address.equals(mBluetoothDeviceAddress) && hasClickConnected) {
            if (BleManager.getInstance().isConnected(address)) {
                broadcastUpdate(BLE_CONNECTING);
                return;
            } else {
                hasClickConnected = false;
                EventBus.getDefault().post(new ConnBleFailMsg());
                return;
            }
        }
        hasClickConnected = true;

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        connectFast(address);
        mBluetoothDeviceAddress = address;
        broadcastUpdate(BLE_CONNECTING);

    }

    private void connectFast(String mac) {
        BleManager.getInstance().connect(mac, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "on start connect");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "on start connect fail");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "on start connect success");
                //校验设备是否拥有我们所需的服务与特征
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                //是否需要更新
                if (null == service) {
                    Log.e(TAG, "service==null ");
                    return;
                }
                mBleDevice = bleDevice;
                setMtu();
                EventBus.getDefault().post(new NotFoundEvent(true));
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "on start dis connect");
                EventBus.getDefault().post(new BleDisconnectedEvent());
            }

        });
    }

    private void setMtu() {
        BleManager.getInstance().setMtu(mBleDevice, 100, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.d(TAG, "on set mtu fail");
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.d(TAG, "mtu changed mtu :" + mtu);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setNotify();
                    }
                }, 100);
            }
        });
    }

    private void setNotify() {
        BleManager.getInstance().notify(mBleDevice, SERVICE_ID, WRITE_ID, new BleNotifyCallback() {
            @Override
            public void onNotifySuccess() {
                Log.d(TAG, "on notify success");
            }

            @Override
            public void onNotifyFailure(BleException exception) {
                Log.d(TAG, "on notify fail");
            }

            @Override
            public void onCharacteristicChanged(byte[] data) {
                try {
                    //获取外设修改的特征值
                    byte[] dataValues = data;
                    if (dataValues.length < 8) {
                        return;
                    }
                    //数据校验
                    int receiveLen = DatalogApUtil.byte2Int(new byte[]{dataValues[0], dataValues[1]});

                    if (receiveLen != dataValues.length - 2) {
                        dataCompase(dataValues);
                    } else {
                        receviceData = dataValues;
                        boolean ischeck = checkAndshow();
                        if (ischeck){
                            //AES解密
                            aesPase();
                            EventBus.getDefault().post(new BleEvent(receviceData));

                        }else {
                            receviceData = new byte[0];
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void aesPase() throws Exception {
        //减10是因为：有效数据=总数据-总长度(2)-协议标识（2）-实际数据长度（2）-设备地址（1）-功能码（1）-功能码-crc（2）
        int datalen = receviceData.length - 10;
        byte[] databytes = new byte[datalen];
        System.arraycopy(receviceData, 8, databytes, 0, datalen);
        byte[] bytes = DatalogApUtil.msgDesCodeByAESCBC(databytes);

        //还要减去补0的数据
        //获取真实的数据长度
        byte[] realByte = {receviceData[4],receviceData[5]};
        byte[]crc={receviceData[receviceData.length-2],receviceData[receviceData.length-1]};

        int realLen = DatalogApUtil.byte2Int(realByte)-2;//内容长度包含了设备地址和功能码所以要减去
        byte[]dataBytes=new byte[realLen];
        if (realLen >= 0) System.arraycopy(bytes, 0, dataBytes, 0, realLen);


        datalen = dataBytes.length + 10;
        byte[]allData=new byte[datalen];
        System.arraycopy(receviceData, 0, allData, 0, 8);
        System.arraycopy(dataBytes, 0, allData, 8, dataBytes.length);
        System.arraycopy(crc, 0, allData, 8+dataBytes.length, crc.length);
        receviceData=allData;
    }

    public void writeCharacteristic(byte[] value) {
        try {
//        byte[] bytes = DatalogApUtil.sendMsgByAes(value);
            receviceData = new byte[0];
            BleManager.getInstance().write(mBleDevice, SERVICE_ID, WRITE_ID, value, false, new BleWriteCallback() {
                @Override
                public void onWriteSuccess(int current, int total, byte[] justWrite) {
                    Log.d(TAG, "on write success current : " + current + " total : " + total);
                }

                @Override
                public void onWriteFailure(BleException exception) {
                    Log.d(TAG, "on write fail :" + exception);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置订阅notificationGattCharacteristic值改变的通知
     */
    public void enableNotification(BluetoothGattService gattService) {
        BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(RX_TX_CHAR_UUID);
        if (characteristic == null) {
            Log.d(TAG, "enableTXNotification characteristic not found!");
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        EventBus.getDefault().post(new NotFoundEvent(true));

        Log.d(TAG, "success to enableNotification.");
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.d(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
//        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
//            Log.d(TAG, "BluetoothAdapter not initialized");
//            return;
//        }
//        mBluetoothGatt.disconnect();

        BleManager.getInstance().disconnectAllDevice();
    }

    public BleService() {
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

}
