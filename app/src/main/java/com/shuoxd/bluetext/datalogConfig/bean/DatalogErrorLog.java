package com.shuoxd.bluetext.datalogConfig.bean;

import androidx.annotation.NonNull;


public class DatalogErrorLog  {

    //采集器序列号
    private String sn;
    //校验码
    private String code;
    //wifi名称
    private String ssid;
    //时长
    private String timer;
    //配网方式
    private String configMode;
    //从哪里配网
    private String from;
    //错误
    private String errorMsg;
    //对应错误码
    private String errorCode;
    //错误名称中文
    private String errorNameZn;
    //错误名称英文
    private String errorNameEn;
    //日期
    private String date;
    //手机类型
    private String phoneType;
    //应用版本
    private String version;


    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getTimer() {
        return timer;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    public String getConfigMode() {
        return configMode;
    }

    public void setConfigMode(String configMode) {
        this.configMode = configMode;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getPhoneType() {
        return phoneType;
    }

    public void setPhoneType(String phoneType) {
        this.phoneType = phoneType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorNameZn() {
        return errorNameZn;
    }

    public void setErrorNameZn(String errorNameZn) {
        this.errorNameZn = errorNameZn;
    }

    public String getErrorNameEn() {
        return errorNameEn;
    }

    public void setErrorNameEn(String errorNameEn) {
        this.errorNameEn = errorNameEn;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @NonNull
    @Override
    public String toString() {
        return "DatalogConfigErrorLog:" + "sn:" + sn+"\ntimer"+timer+"\nconfigMode"+configMode+
                "\nfrom"+from+"\ndate"+date+"\nphoneType"+phoneType+"\nversion"+version;
    }
}
