package com.shuoxd.bluetext.datalogConfig.bluetooth.bean;

import java.io.Serializable;

public class BleBean implements Serializable {


    private String status;

    private String bleName;

    private String address;

    private String type;

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
