package com.shuoxd.bluetext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WifiUtils {
    private static WifiUtils utils = null;
    private WifiManager wifiManager;
    private WifiInfo lastWifiInfo;
    private NetworkInfo lastNetworkInfo;
    private WifiConfiguration lastWifiConfiguration;
    private List<AccessPoint> lastAccessPoints = new ArrayList<>();

    private Context context;

    public WifiUtils(Context context){
        this.context=context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //获取权限
        if (wifiManager != null) {
            wifiManager.setWifiEnabled(true);
            lastNetworkInfo = getActiveNetworkInfo();
            lastWifiInfo = wifiManager.getConnectionInfo();
        }

    }

    public static WifiUtils getInstance(Context context){
        if (utils == null){
            synchronized (WifiUtils.class){
                if (utils == null){
                    utils = new WifiUtils(context);
                }
            }
        }
        return utils;
    }





    public List<AccessPoint> updateAccessPoints() {
        List<AccessPoint> accessPoints = new ArrayList<>();
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (lastWifiInfo != null && lastWifiInfo.getNetworkId() != AccessPoint.INVALID_NETWORK_ID) {
            lastWifiConfiguration = getWifiConfigurationForNetworkId(lastWifiInfo.getNetworkId());
        }
        if (scanResults != null) {

            for (ScanResult scanResult : scanResults) {
                if (TextUtils.isEmpty(scanResult.SSID)) {
                    continue;
                }
                AccessPoint accessPoint = new AccessPoint(context, scanResult);
                if (accessPoints.contains(accessPoint)) {
                    continue;
                }

                @SuppressLint("MissingPermission") List<WifiConfiguration> wifiConfigurations = wifiManager.getConfiguredNetworks();
                if (wifiConfigurations != null) {
                    for (WifiConfiguration config : wifiConfigurations) {
                        if (accessPoint.getQuotedSSID().equals(config.SSID)) {
                            accessPoint.setWifiConfiguration(config);
                        }
                    }
                }
                if (lastWifiInfo != null && lastNetworkInfo != null) {
                    accessPoint.update(lastWifiConfiguration, lastWifiInfo, lastNetworkInfo);
                }
                int frequency = accessPoint.frequency;
                if ((frequency > 4900 && frequency < 5900) || accessPoint.ssid.toUpperCase().endsWith("5G")) {
                    continue;
                }
                accessPoints.add(accessPoint);

            }
        }
        Collections.sort(accessPoints);
        lastAccessPoints = accessPoints;
        return lastAccessPoints;
    }




    /**
     * 根据 NetworkId 获取 WifiConfiguration 信息
     *
     * @param networkId 需要获取 WifiConfiguration 信息的 networkId
     * @return 指定 networkId 的 WifiConfiguration 信息
     */
    private WifiConfiguration getWifiConfigurationForNetworkId(int networkId) {
        @SuppressLint("MissingPermission") final List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                if (lastWifiInfo != null && networkId == config.networkId) {
                    return config;
                }
            }
        }
        return null;
    }


    /**
     * 获取当前网络信息
     */
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            return cm.getActiveNetworkInfo();
        }
        return null;
    }





    /**
     * wifi是否打开
     * @return
     */
    public boolean isWifiEnable(){
        boolean isEnable = false;
        if (wifiManager != null){
            if (wifiManager.isWifiEnabled()){
                isEnable = true;
            }
        }
        return isEnable;
    }

    /**
     * 打开WiFi
     */
    public void openWifi(){
        if (wifiManager != null && !isWifiEnable()){
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * 关闭WiFi
     */
    public void closeWifi(){
        if (wifiManager != null && isWifiEnable()){
            wifiManager.setWifiEnabled(false);
        }
    }

    /**
     * 获取WiFi列表
     * @return
     */
    public List<ScanResult> getWifiList(){
        List<ScanResult> resultList = new ArrayList<>();
        if (wifiManager != null && isWifiEnable()){
            resultList.addAll(wifiManager.getScanResults());
        }
        return resultList;
    }

    /*   *//**
     * 有密码连接
     * @param ssid
     * @param pws
     *//*
    public boolean connectWifiPws(String ssid, String pws){
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, pws, true));
        boolean b = wifiManager.enableNetwork(netId, true);
        return b;
    }*/

    /*   *//**
     * 无密码连接
     * @param ssid
     *//*
    public void connectWifiNoPws(String ssid){
        wifiManager.disableNetwork(wifiManager.getConnectionInfo().getNetworkId());
        int netId = wifiManager.addNetwork(getWifiConfig(ssid, "", false));
        wifiManager.enableNetwork(netId, true);
    }*/

    /*   *//**
     * wifi设置
     * @param ssid
     * @param pws
     * @param isHasPws
     *//*
    private WifiConfiguration getWifiConfig(String ssid, String pws, boolean isHasPws){
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + ssid + "\"";

        WifiConfiguration tempConfig = isExist(ssid);
        if(tempConfig != null) {
            wifiManager.removeNetwork(tempConfig.networkId);
        }
        if (isHasPws){
            config.preSharedKey = "\""+pws+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }else {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        return config;
    }*/

    /*    *//**
     * 得到配置好的网络连接
     * @param ssid
     * @return
     *//*
    private WifiConfiguration isExist(String ssid) {
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equals("\""+ssid+"\"")) {
                return config;
            }
        }
        return null;
    }*/
}
