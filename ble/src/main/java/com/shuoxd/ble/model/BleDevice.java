package com.shuoxd.ble.model;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Scanned or connected BLE device snapshot.
 */
public class BleDevice implements Parcelable {

    private final String name;
    private final String mac;
    private final int rssi;
    @Nullable
    private final BluetoothDevice device;
    private final byte[] scanRecord;

    public BleDevice(@Nullable String name,
                     @NonNull String mac,
                     int rssi,
                     @Nullable BluetoothDevice device,
                     @Nullable byte[] scanRecord) {
        this.name = name;
        this.mac = mac;
        this.rssi = rssi;
        this.device = device;
        this.scanRecord = scanRecord;
    }

    protected BleDevice(Parcel in) {
        name = in.readString();
        mac = in.readString();
        rssi = in.readInt();
        device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        scanRecord = in.createByteArray();
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };

    @Nullable
    public String getName() {
        return name;
    }

    @NonNull
    public String getMac() {
        return mac;
    }

    public int getRssi() {
        return rssi;
    }

    @Nullable
    public BluetoothDevice getDevice() {
        return device;
    }

    @Nullable
    public byte[] getScanRecord() {
        return scanRecord;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(mac);
        dest.writeInt(rssi);
        dest.writeParcelable(device, flags);
        dest.writeByteArray(scanRecord);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BleDevice)) return false;
        BleDevice bleDevice = (BleDevice) o;
        return mac.equalsIgnoreCase(bleDevice.mac);
    }

    @Override
    public int hashCode() {
        return mac.toUpperCase().hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "BleDevice{name='" + name + "', mac='" + mac + "', rssi=" + rssi + '}';
    }
}
