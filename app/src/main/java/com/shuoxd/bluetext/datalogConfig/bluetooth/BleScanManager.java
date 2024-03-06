package com.shuoxd.bluetext.datalogConfig.bluetooth;



import static com.shuoxd.bluetext.datalogConfig.bluetooth.BleService.BLE_CONNECTING;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;


import androidx.fragment.app.FragmentActivity;


import com.shuoxd.bluetext.R;
import com.shuoxd.bluetext.SmartHomeUtil;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.bean.BleBrocastPro;
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
                parseRecord(scanRecord, device.getAddress());
            }
        };

    }

    private void parseRecord(byte[] scanRecord, String address) {



        //数据长度
        int tempLenIndex = 0;
        List<BleBrocastPro> datas = new ArrayList<>();
        for (int i = 0; i < scanRecord.length; i = tempLenIndex) {

            //len包含Type data
            int len = scanRecord[i] & 0xff;
            //len包含Type data
            if (len == 0) {
                break;
            }

            byte[] data = new byte[len - 1];
            BleBrocastPro pro = new BleBrocastPro();
            pro.len = len;
            pro.type = scanRecord[i + 1];
            System.arraycopy(scanRecord, i + 2, data, 0, len - 1);
            pro.data = data;





            datas.add(pro);
            tempLenIndex += len + 1;



        }


        BleBean bleBean = new BleBean();
        bleBean.setAddress(address);


        String name1 = "";
        String deviceType = "";
        String bleName = "";

        for (int i = 0; i < datas.size(); i++) {
            BleBrocastPro pro = datas.get(i);
            byte type = pro.type;
            switch (type) {
                case 0x01:
                    break;
                case (byte) 0xff:
                    String tempType = new String(pro.data, StandardCharsets.UTF_8);
                    if (tempType.toLowerCase().contains("g")) {
                        deviceType = tempType;
                        int i1 = tempType.indexOf("#");
                        if (i1 != -1) {
                            name1 = tempType.substring(i1 + 1);
                        }

                    }

                    break;
                case 0x03:
                    break;
                case 0x09:
                    bleName = new String(pro.data, StandardCharsets.UTF_8);

                    break;
                case 0x0A:
                    break;

            }


        }


        bleName = name1 + bleName;





        bleBean.setBleName(bleName);
        bleBean.setType(deviceType);

        if (TextUtils.isEmpty(bleBean.getBleName()) || TextUtils.isEmpty(bleBean.getType()) || TextUtils.isEmpty(bleBean.getAddress())) {
            return;
        }



    /*      33：便携式电源内采集器
            34：ShineWiFi-X2
            43：GroHome Manager
            44：WeLink
            45：Welink-Pro
            46：ShineRFStick-X2
            51：ShineWiLan-X
            94：便携式电源-TB*/

        Log.d("liaojinsha","type:"+bleBean.getType()+"name:"+bleName);

        if (bleBean.getType().toLowerCase().contains("g")) {
            boolean repeatFlag = false;
            for (BleBean ble : scanlisteners.getBleData()) {
                if (ble.getBleName().equals(bleBean.getBleName())) {
                    repeatFlag = true;
                    break;
                }
            }
            if (!repeatFlag) {
                scanlisteners.addBleData(bleBean);
            }
        }


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
