package com.shuoxd.bluetext;

import android.app.Application;

import com.shuoxd.bluetext.datalogConfig.bluetooth.BleService;

public class MyApplication extends Application {

    //为了实现每次使用该类时不创建新的对象而创建的静态对象
    private static MyApplication instance;

    private BleService gBleServer;


    //构造方法
    //实例化一次
    public  static MyApplication getInstance() {
        return instance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }



    public BleService getgBleServer() {
        return gBleServer;
    }

    public void setgBleServer(BleService gBleServer) {
        this.gBleServer = gBleServer;
    }

}
