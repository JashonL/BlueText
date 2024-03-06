package com.shuoxd.bluetext.datalogConfig.bluetooth;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static android.content.pm.PackageManager.FEATURE_BLUETOOTH_LE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;

import com.shuoxd.bluetext.MyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author mac
 * @date 2021/11/19
 * <p>
 * 蓝牙相关工具类
 */
public class BluetoothUtils {

    public static final int REQUEST_ENABLE_BT = 1;



    public static void initScanSettings() {
        //创建ScanSettings的build对象用于设置参数
        ScanSettings.Builder builder = new ScanSettings.Builder()
                //设置高功耗模式
                .setScanMode(SCAN_MODE_LOW_LATENCY);
        //android 6.0添加设置回调类型、匹配模式等
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //定义回调类型
            builder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
            //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
            builder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
        }
        //芯片组支持批处理芯片上的扫描
        if (BluetoothAdapter.getDefaultAdapter().isOffloadedScanBatchingSupported()) {
            //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
            //设置为0以立即通知结果
            builder.setReportDelay(0L);
        }
        builder.build();
    }

    /**
     * 高级管理员用于获取一个实例BluetoothAdapter并进行整体蓝牙管理
     *
     * @return
     */
    public static BluetoothManager getBluetoothManager() {
        return (BluetoothManager) MyApplication.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    /**
     * 获取本地设备蓝牙适配器
     *
     * @return
     */
    public static BluetoothAdapter getBluetoothAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothLeScanner getBluetoothLeScanner() {
        return getBluetoothAdapter().getBluetoothLeScanner();
    }


    /**
     * 检查设备是否支持BLE
     *
     * @return
     */
    public static boolean isSupportBle() {
        if (getBluetoothAdapter() != null &&
                MyApplication.getInstance().getPackageManager().hasSystemFeature(FEATURE_BLUETOOTH_LE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查蓝牙是否开启
     *
     * @return
     */
    public static boolean isBluetoothOpen() {
        return getBluetoothAdapter().isEnabled();
    }

    /**
     * 打开蓝牙
     * 这种方式是直接打开没有弹框
     * @return
     */
    public static boolean openBluetooth() {
        if (!isBluetoothOpen()) {
            return getBluetoothAdapter().enable();
        } else {
            return true;
        }
    }

    public static void openBluetooth(Activity activity, int code) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, code);
    }

    public static void cancelDiscovery() {
        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getBluetoothAdapter();
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    public static void startDiscovery() {
        BluetoothUtils.getBluetoothAdapter().startDiscovery();
    }


    /**
     * 关闭蓝牙
     *
     * @return
     */
    public static boolean closeBluetooth() {
        if (!isBluetoothOpen()) {
            return true;
        } else {
            return getBluetoothAdapter().disable();
        }
    }

    /**
     * @return
     */
    public static List<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> devices = getBluetoothAdapter().getBondedDevices();
        if (devices != null && devices.size() > 0) {
            List<BluetoothDevice> list = new ArrayList<>();
            for (BluetoothDevice device : devices) {
                list.add(device);
            }
            return list;
        }
        return null;
    }


    public static List<BluetoothDevice> getConnectedDevices() {
        return getBluetoothManager().getConnectedDevices(BluetoothProfile.GATT);
    }

    public static BluetoothDevice getBluetoothDevice(String mac) {
        BluetoothAdapter adapter = getBluetoothAdapter();
        return adapter != null ? adapter.getRemoteDevice(mac) : null;
    }

    public static int getBluetoothConnectionState(String mac) {
        BluetoothDevice device = getBluetoothDevice(mac);
        return device != null ? getBluetoothConnectionState(device) : -1;
    }

    /**
     * 获取配置文件的当前连接状态到远程设备
     *
     * @param device：远程蓝牙设备，profile为GATT或GATT_SERVER
     * @return：配置文件处于什么连接状态
     */
    public static int getBluetoothConnectionState(BluetoothDevice device) {
        return getBluetoothManager().getConnectionState(device, BluetoothProfile.GATT);
    }

    public static boolean isConnected(BluetoothDevice device) {
        return getBluetoothConnectionState(device) == BluetoothGatt.STATE_CONNECTED;
    }

    /**
     * 获取本地蓝牙地址信息，针对7.0以上的设备有可能获取不真确哈
     *
     * @return
     */
    public static String getMac() {
        String macAddr = getBluetoothAdapter().getAddress();
        return macAddr;
    }

    public static BluetoothGattCharacteristic getCharacter(BluetoothGatt gatt, UUID serviceUUID, UUID characterUUID) {
        BluetoothGattService service = gatt.getService(serviceUUID);//代表蓝牙的GATT服务
        return service != null ? service.getCharacteristic(characterUUID) : null;
    }

    public static boolean setCharacteristicNotification(BluetoothGatt gatt, UUID service, UUID character, boolean enable) {
        BluetoothGattCharacteristic characteristic = getCharacter(gatt, service, character);

        if (characteristic == null) {
            return false;
        }

        //启用或禁用给定特征的通知/指示
        if (!gatt.setCharacteristicNotification(characteristic, enable)) {
            return false;
        }

        //返回具有此特征的描述符列表中给定UUID的描述符
//        UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

        if (descriptor == null) {
            return false;
        }

        byte[] value = (enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!descriptor.setValue(value)) {
            return false;
        }

        if (!gatt.writeDescriptor(descriptor)) {
            return false;
        }

        return true;
    }

}
