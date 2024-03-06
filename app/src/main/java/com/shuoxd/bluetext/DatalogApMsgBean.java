package com.shuoxd.bluetext;

public class DatalogApMsgBean {
    //MBAP报文头
    //报文编号	2字节	默认：00 01
    //协议标识	2字节	00 05
    //数据长度	2字节	根据数据内容定
    //从站地址	1字节	默认：01
    private byte[] mbap_num={0x00,0x01};
    private byte[] mbap_pro_id={0x00,0x05};
    private byte[] mbap_len;
    private byte mbap_slave_address=0x01;
    //功能码
    // 0x18	1字节	设置采集器参数
    // 0x19	1字节	获取采集器参数
    private byte fun_code;
    //数据区, 原始数据
    private byte[] data;
    //经过加密的数据
    private byte[] encryptedData;
    //校验区（CRC16）
    private byte[] crcData;

    public byte[] getMbap_num() {
        return mbap_num;
    }

    public void setMbap_num(byte[] mbap_num) {
        this.mbap_num = mbap_num;
    }

    public byte[] getMbap_pro_id() {
        return mbap_pro_id;
    }

    public void setMbap_pro_id(byte[] mbap_pro_id) {
        this.mbap_pro_id = mbap_pro_id;
    }

    public byte[] getMbap_len() {
        return mbap_len;
    }

    public void setMbap_len(byte[] mbap_len) {
        this.mbap_len = mbap_len;
    }

    public byte getMbap_slave_address() {
        return mbap_slave_address;
    }

    public void setMbap_slave_address(byte mbap_slave_address) {
        this.mbap_slave_address = mbap_slave_address;
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

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
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
        data[0]=getMbap_num()[0];
        data[1]=getMbap_num()[1];
        data[2]=getMbap_pro_id()[0];
        data[3]=getMbap_pro_id()[1];
        data[4]=getMbap_len()[0];
        data[5]=getMbap_len()[1];
        data[6]=getMbap_slave_address();
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
        int length=getEncryptedData().length+getCrcData().length;
        byte[]data=new byte[length];
        if (getEncryptedData() != null){
            System.arraycopy(getEncryptedData(),0,data,0,getEncryptedData().length);
        }
        if (getCrcData() != null){
            System.arraycopy(getCrcData(),0,data,getEncryptedData().length,getCrcData().length);
        }
        return data;
    }

}
