package com.shuoxd.bluetext.datalogConfig.bluetooth;



import static com.shuoxd.bluetext.datalogConfig.bluetooth.BleService.BLE_CONNECTING;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;

import androidx.fragment.app.FragmentActivity;


import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.bluetooth.bean.BleBean;
import com.shuoxd.bluetext.datalogConfig.bluetooth.constant.BluetoothConstant;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhuLiuBin
 * Date: 2021/11/26
 */
public class BleScanManager {


    private Context context;

    private final String TAG = BleScanManager.class.getSimpleName();

    private ScanCallback mScanCallback;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    private List<BleBean> mBleList = new ArrayList<>();


    private Scanlisteners scanlisteners;

    private ConnetedListeners connetedListeners;

    public BleScanManager(Context context, Scanlisteners scanlisteners, ConnetedListeners connListners) {
        this.context = context;
        this.scanlisteners = scanlisteners;
        this.connetedListeners = connListners;
        registerBluetoothReceiver();
    }

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, final Intent intent) {
            String action = intent.getAction();
            // 获取设备对象
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (action) {
                case BluetoothDevice.ACTION_FOUND://发现设备
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    if (mLeScanCallback != null) {
                        mLeScanCallback.onLeScan(device, rssi, null);
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED://已连接
//                    connetedListeners.setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_2);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED://已断开连接
                    connetedListeners.setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
                    break;
                case BLE_CONNECTING://连接中
                    connetedListeners.setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_3);
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_OFF:
                            connetedListeners.setBleConnStatus(BluetoothConstant.BLUETOOTH_CONNET_STATUS_1);
//                            baseView.showBleDialog();
                            break;
                        case BluetoothAdapter.STATE_ON:
//                            baseView.hideBleDialog();
                            BluetoothUtils.startDiscovery();
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
        }
    };

    public void initCallBack() {
        mBleList.clear();
        //android 5.0 扫描回调：
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, android.bluetooth.le.ScanResult result) {
                if (mLeScanCallback != null) {
                    mLeScanCallback.onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
                }
            }
        };

        //5.0以下
        mLeScanCallback = (device, rssi, scanRecord) -> {
            if (scanRecord != null) {
                parseRecord(scanRecord);
            }
        };

    }

    private void parseRecord(byte[] scanRecord) {
        int index = 3;
        int startLen = scanRecord[index] & 0xff;
        if (startLen == 0) {
            return;
        }
        if (startLen != 11) {
            return;
        }
        if ((scanRecord[4] & 0xff) != 9) {
            return;
        }
        BleBean bleBean = new BleBean();
        for (int i = 0; i < 6; i++) {
            byte[] valueArr = new byte[startLen - 1];  //0b 11-1=10
            int type = scanRecord[index + 1] & 0xff;
            if (type == 9) {
                System.arraycopy(scanRecord, index + 2, valueArr, 0, valueArr.length);
                String value = new String(valueArr, StandardCharsets.UTF_8);
                Log.d(TAG, "parseData: name:" + value);
                bleBean.setBleName(value);
                index += 1 + scanRecord[index] & 0xff;
                startLen = scanRecord[index] & 0xff;
            } else if (type == 255) {
                System.arraycopy(scanRecord, index + 2, valueArr, 0, valueArr.length);
                String value = new String(valueArr, StandardCharsets.UTF_8);
                //判断设备类型  34才是逆变器
                if (value.contains("G:") || value.contains("g:")) {
                    bleBean.setType(value);
                    Log.d(TAG, "parseData: type:" + value);
                } else if (value.contains("M:")) {
                    //String st = String.format("%02X ", data[i]);
                    String address = String.format("%02X", valueArr[2]) + ":"
                            + String.format("%02X", valueArr[3]) + ":"
                            + String.format("%02X", valueArr[4]) + ":"
                            + String.format("%02X", valueArr[5]) + ":"
                            + String.format("%02X", valueArr[6]) + ":"
                            + String.format("%02X", valueArr[7]);
                    bleBean.setAddress(address);
                    Log.d(TAG, "parseData: address:" + address);
                }
                index += 1 + scanRecord[index] & 0xff;
                startLen = scanRecord[index] & 0xff;
            } else {
                System.arraycopy(scanRecord, index + 2, valueArr, 0, valueArr.length);
                //String value = new String(valueArr, StandardCharsets.UTF_8);
                index += 1 + scanRecord[index] & 0xff;
                startLen = scanRecord[index] & 0xff;
            }
        }

        boolean repeatFlag = false;

        for (BleBean ble : scanlisteners.getBleData()) {
            if (ble.getBleName().equals(bleBean.getBleName())) {
                repeatFlag = true;
                break;
            }
        }

        if (!repeatFlag && "g:12".equalsIgnoreCase(bleBean.getType())) {
            scanlisteners.addBleData(bleBean);
        }

//        baseView.showConnDeviceDialog();

    }

    public void startBleScan() {
        BluetoothUtils.getBluetoothLeScanner().stopScan(mScanCallback);
        BluetoothUtils.getBluetoothLeScanner().startScan(mScanCallback);
//        BluetoothUtils.startDiscovery();
        // 搜索10s
        new Handler().postDelayed(() -> {
            try {
                BluetoothUtils.getBluetoothLeScanner().stopScan(mScanCallback);
                scanlisteners.scanStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 10000);

    }


    public void stopBleScan() {
        BluetoothUtils.getBluetoothLeScanner().stopScan(mScanCallback);
    }


    public void registerBluetoothReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BLE_CONNECTING);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mBleReceiver, intentFilter);
    }

    public void unRegisterBleReceiver() {
        mBleList.clear();
        BluetoothUtils.cancelDiscovery();
        context.unregisterReceiver(mBleReceiver);
        mLeScanCallback = null;
        mScanCallback = null;
    }

    public boolean scanBluetooth() {
        //判断是否支持蓝牙
        boolean supportBle = BluetoothUtils.isSupportBle();
        if (!supportBle) {
            CircleDialogUtils.showCommentDialog((FragmentActivity) context, context.getString(R.string.温馨提示),
                    context.getString(R.string.not_support_bluetooth),
                    context.getString(R.string.all_ok), "", Gravity.CENTER,
                    v -> ((Activity) context).finish(), null, null);
        }
        return supportBle;
    }


    public interface Scanlisteners {
        List<BleBean> getBleData();

        void addBleData(BleBean bleBean);

        void scanStop();
    }


    public interface ConnetedListeners {
        void setBleConnStatus(String status);
    }


}
