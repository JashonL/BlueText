package com.shuoxd.bluetext.datalogConfig.bluetooth.eventbus;

public class NotFoundEvent {

    private boolean  isConnet;

    public NotFoundEvent(boolean isConnet) {
        this.isConnet = isConnet;
    }

    public boolean isConnet() {
        return isConnet;
    }

    public void setConnet(boolean connet) {
        isConnet = connet;
    }
}
