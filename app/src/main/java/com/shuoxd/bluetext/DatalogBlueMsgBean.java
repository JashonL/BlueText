package com.shuoxd.bluetext;


/**
 * 用于发送蓝牙数据的实体类
 * <p>
 * <p>
 * 总长度+协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补0区+CRC16校验码
 */
public class DatalogBlueMsgBean {

    public static byte DEVICE_CODE = 0X01;

    //*****************CRC校验内容*******************
    //总长度2字节
    private byte[] all_len;
    //----------总长度内容-------------------------
    //协议标识 2字节
    private byte[] bap_pro_id = {0x00, 0x06};
    //实际数据长度 2字节
    private byte[] realdata_len;
    //=========实际数据内容================
    //设备地址 1字节  先写0 设备返回再保存
    private byte device_addr;
    //功能码  1字节
    private byte fun_code;
    //数据内容包括补0  进行AES加密
    private byte[] data;
    //=========实际数据内容================
    //----------总长度内容-----------------------
    //*******************CRC校验内容***********************
    //校验区（CRC16） 2字节
    private byte[] crcData;


    public byte[] getAll_len() {
        return all_len;
    }

    public void setAll_len(byte[] all_len) {
        this.all_len = all_len;
    }

    public byte[] getBap_pro_id() {
        return bap_pro_id;
    }

    public void setBap_pro_id(byte[] bap_pro_id) {
        this.bap_pro_id = bap_pro_id;
    }

    public byte[] getRealdata_len() {
        return realdata_len;
    }

    public void setRealdata_len(byte[] realdata_len) {
        this.realdata_len = realdata_len;
    }

    public byte getDevice_addr() {
        return device_addr;
    }

    public void setDevice_addr(byte device_addr) {
        this.device_addr = device_addr;
    }

    public byte getFun_code() {
        return fun_code;
    }

    public void setFun_code(byte fun_code) {
        this.fun_code = fun_code;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getCrcData() {
        return crcData;
    }

    public void setCrcData(byte[] crcData) {
        this.crcData = crcData;
    }



    public byte[] getBytes(){
        int length=8+getData().length;
        byte[]data=new byte[length];
        data[0]=getAll_len()[0];
        data[1]=getAll_len()[1];
        data[2]=getBap_pro_id()[0];
        data[3]=getBap_pro_id()[1];
        data[4]=getRealdata_len()[0];
        data[5]=getRealdata_len()[1];
        data[6]=getDevice_addr();
        data[7]=getFun_code();
        if (getData() != null){
            System.arraycopy(getData(),0,data,8,getData().length);
        }
/*
        if (getCrcData() != null){
            System.arraycopy(getCrcData(),0,data,7+getData().length,getCrcData().length);
        }*/
        return data;
    }

    public byte[] getBytesCRC(){
        int length=getBytes().length+getCrcData().length;
        byte[]data=new byte[length];
        if (getBytes() != null){
            System.arraycopy(getBytes(),0,data,0,getBytes().length);
        }
        if (getCrcData() != null){
            System.arraycopy(getCrcData(),0,data,getBytes().length,getCrcData().length);
        }
        return data;
    }


}
