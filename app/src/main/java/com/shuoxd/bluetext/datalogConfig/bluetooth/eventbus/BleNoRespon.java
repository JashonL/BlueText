package com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus;

public class BleNoRespon {
    private byte[]datavalues;

    public BleNoRespon(byte[] dataValues) {
        this.datavalues=dataValues;
    }

    public byte[] getDatavalues() {
        return datavalues;
    }

    public void setDatavalues(byte[] datavalues) {
        this.datavalues = datavalues;
    }

}
