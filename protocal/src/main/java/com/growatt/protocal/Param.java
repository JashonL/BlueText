package com.growatt.protocal;


import com.growatt.protocal.utils.ByteUtils;

/**
 * 参数格式
 */
public class Param {

    /**
     * 参数编号常量
     */
    public static class ParamNoConstant {
        //密钥
        public static final int PARAM_NO_SECRET_KEY = 54;
        //默认密钥
        public static final String PARAM_DEFAULT_NO_SECRET_KEY_VALUE = "growatt_iot_device_common_key_01";

        //查询WiFi状态，"0"-成功，其它失败
        public static final int PARAM_NO_WIFI_STATUS = 55;
        //设置WiFi名称
        public static final int PARAM_NO_WIFI_NAME = 56;
        //设置WiFi密码
        public static final int PARAM_NO_WIFI_PASSWORD = 57;
        //查询采集器与服务器通讯状态，"16"、"3"、"4"代表成功，其它失败
        public static final int PARAM_NO_SERVER_STATUS = 60;
        //获取周围的WIFI列表
        public static final int PARAM_NO_WIFI_LIST = 75;

        //添加RS485设备
        public static final int ADD_RS485 = 118;
        //RS485-1参数
        public static final int RS485_1_PORT = 1101;
        //RS485-2参数
        public static final int RS485_2_PORT = 1102;
        //RS485-3参数
        public static final int RS485_3_PORT = 1103;

        //采集器软件版本号
        public static final int FIRMWARE_VERSION = 21;

        //查询升级进度
        public static final int UPGRADE_PROGRESS = 101;
    }

    /**
     * 参数长度占2个字节
     */
    public static final int PARAM_LENGTH = 2;


    /**
     * 2个字节，参数编号
     */
    private byte[] _paramNo;

    /**
     * 参数编号值
     */
    private int paramNo;

    /**
     * 2个字节，参数长度
     */
    private byte[] _paramLength;

    /**
     * 参数长度值
     */
    private int paramLength;

    /**
     * 参数数据
     */
    private byte[] _param;

    private Param() {
    }

    public static Param newInstance(int paramNo, String paramStr) {
        Param param = new Param();
        param.paramNo = paramNo;
        param._paramNo = ByteUtils.intTo2Byte(paramNo);
        param._param = ByteUtils.stringToBytes(paramStr);
        param.paramLength = param._param.length;
        param._paramLength = ByteUtils.intTo2Byte(param._param.length);
        return param;
    }

    public static Param newInstance(byte[] _paramNo, int paramNo, byte[] _paramLength, int paramLength, byte[] _param) {
        return new Param(_paramNo, paramNo, _paramLength, paramLength, _param);
    }

    private Param(byte[] _paramNo, int paramNo, byte[] _paramLength, int paramLength, byte[] _param) {
        this._paramNo = _paramNo;
        this.paramNo = paramNo;
        this._paramLength = _paramLength;
        this.paramLength = paramLength;
        this._param = _param;
    }

    public byte[] getBytes() {
        return ByteUtils.join(_paramNo, _paramLength, _param);
    }

    /**
     * 参数长度（参数编号长度+参数长度长度+参数值长度）
     */
    public int getLength() {
        return _paramNo.length + _paramLength.length + _param.length;
    }

    public int getParamNo() {
        return paramNo;
    }

    public String readParamValueText() {
        return ByteUtils.bytesToString(_param);
    }

}
