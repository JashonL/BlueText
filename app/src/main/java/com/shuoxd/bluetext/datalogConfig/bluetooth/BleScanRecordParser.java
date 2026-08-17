package com.shuoxd.bluetext.datalogConfig.bluetooth;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.shuoxd.bluetext.datalogConfig.bean.BleBrocastPro;
import com.shuoxd.bluetext.datalogConfig.bluetooth.bean.BleBean;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * BLE advertising (scanRecord) parser.
 * Preserves the original {@link BleScanManager} AD parse / filter rules.
 */
public final class BleScanRecordParser {

    private static final String TAG = "BleScanRecordParser";

    private BleScanRecordParser() {
    }

    /**
     * Parse BLE AD structures from scanRecord.
     *
     * @return BleBean if name/type/address are valid and name length is 10–20; otherwise null
     */
    @Nullable
    public static BleBean parse(@Nullable byte[] scanRecord, @Nullable String address) {
        if (scanRecord == null || TextUtils.isEmpty(address)) {
            return null;
        }

        int tempLenIndex = 0;
        List<BleBrocastPro> datas = new ArrayList<>();
        for (int i = 0; i < scanRecord.length; i = tempLenIndex) {
            // len contains Type + data
            int len = scanRecord[i] & 0xff;
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
                    deviceType = tempType;
                    int i1 = tempType.indexOf("#");
                    if (i1 != -1) {
                        name1 = tempType.substring(i1 + 1);
                    }
                    break;
                case 0x03:
                    break;
                case 0x09:
                    bleName = new String(pro.data, StandardCharsets.UTF_8);
                    break;
                case 0x0A:
                    break;
                default:
                    break;
            }
        }

        bleName = name1 + bleName;

        bleBean.setBleName(bleName);
        bleBean.setType(deviceType);

        if (TextUtils.isEmpty(bleBean.getBleName())
                || TextUtils.isEmpty(bleBean.getType())
                || TextUtils.isEmpty(bleBean.getAddress())) {
            return null;
        }

    /*      33：便携式电源内采集器
            34：ShineWiFi-X2
            43：GroHome Manager
            44：WeLink
            45：Welink-Pro
            46：ShineRFStick-X2
            51：ShineWiLan-X
            94：便携式电源-TB*/

        Log.d(TAG, "type:" + bleBean.getType() + "name:" + bleName);

        if (bleBean.getBleName().length() >= 10 && bleBean.getBleName().length() <= 20) {
            return bleBean;
        }
        return null;
    }
}
