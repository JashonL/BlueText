package com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus;

public class BleEvent {

    private byte[]datavalues;

    public BleEvent(byte[] dataValues) {
        this.datavalues=dataValues;
    }

    public byte[] getDatavalues() {
        return datavalues;
    }

    public void setDatavalues(byte[] datavalues) {
        this.datavalues = datavalues;
    }
}
