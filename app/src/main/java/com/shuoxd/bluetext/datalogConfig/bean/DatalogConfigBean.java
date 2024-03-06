package com.shuoxd.bluetext.datalogConfig.bean;

public class DatalogConfigBean {
    //采集器序列号
    private String serialNumber;
    //wifi类型的名称
    private String wifiTypeName;
    //wifi的类型  11、16...
    private String typeNumber;
    //电站id
    private String plantId;
    //用户id
    private String userId;
    //是否已添加
    private String isHave;
    //服务器id
    private String serverId;
    //配网成功之后得操作   只配网、配网-添加
    private String configType;
    //配网完成之后返回哪里  电站详情、OSS..
    private String action;
    //是否是新采集器
    private String isNewDatalog;
    //配网模式
    private String configMode;


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getWifiTypeName() {
        return wifiTypeName;
    }

    public void setWifiTypeName(String wifiTypeName) {
        this.wifiTypeName = wifiTypeName;
    }

    public String getTypeNumber() {
        return typeNumber;
    }

    public void setTypeNumber(String typeNumber) {
        this.typeNumber = typeNumber;
    }


    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIsHave() {
        return isHave;
    }

    public void setIsHave(String isHave) {
        this.isHave = isHave;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIsNewDatalog() {
        return isNewDatalog;
    }

    public void setIsNewDatalog(String isNewDatalog) {
        this.isNewDatalog = isNewDatalog;
    }

    public String getConfigMode() {
        return configMode;
    }

    public void setConfigMode(String configMode) {
        this.configMode = configMode;
    }
}
