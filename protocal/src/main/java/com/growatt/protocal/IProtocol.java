package com.growatt.protocal;

import com.growatt.protocal.utils.ByteUtils;

/**
 * 数服协议基类
 * 报文头+功能码+数据区
 */
public interface IProtocol {

    /**
     * 报文头
     */
    byte[] get_Header();

    /**
     * 功能码（1字节）
     */
    byte get_functionCode();

    /**
     * 数据区
     */
    byte[] get_dataArea();

    /**
     * 发送的字节数组
     */
    byte[] getBytes();

    /**
     * 原始字节数组
     */
    byte[] getDecodeBytes();

    /**
     * 获取数服协议版本号
     */
    static int parseVersion(byte[] rawResponse){
        try{
            byte[] protocolVersion = new byte[]{rawResponse[2], rawResponse[3]};
            return ByteUtils.convert2BytesToUnsignedInt(protocolVersion);
        }catch (Exception ignored){
        }
        return 5;
    }

}
