package com.growatt.protocal.version3;


import com.growatt.protocal.IProtocol;
import com.growatt.protocal.utils.ByteUtils;

/**
 * 数服协议基础类，由这几部分组成
 * 报文头+功能码+数据区
 * 不同的功能码只有数据区的数据不一样
 * 这个版本的协议无AES加密，无CRC校验
 */
public abstract class Protocol implements IProtocol {

    /**
     * 协议常量
     */
    public static class ProtocolConstant {

        /**
         * 通讯编号长度
         */
        public final static int COMMUNICATION_NO_LENGTH= 2;

        /**
         * 协议版本（协议标识）长度
         */
        public final static int PROTOCOL_VERSION_LENGTH = 2;

        /**
         * 实际数据长度
         */
        public final static int DATA_LENGTH = 2;

        /**
         * 设备地址长度
         */
        public final static int DEVICE_ADDRESS_LENGTH = 1;

        /**
         * 报文头长度
         */
        public final static int HEADER_LENGTH = COMMUNICATION_NO_LENGTH + PROTOCOL_VERSION_LENGTH + DATA_LENGTH + DEVICE_ADDRESS_LENGTH;

        /**
         * 功能码长度
         */
        public final static int FUNCTION_CODE_LENGTH = 1;

        /**
         * 数据采集器序列号长度
         */
        public final static int DATA_LOGGING_SN_LENGTH = 10;

        /**
         * 参数编号个数长度,0X18与0X19命令专用
         */
        public final static int PARAM_NO_COUNT_LENGTH = 2;

        /**
         * 设置数据的长度,0X18命令专用
         */
        public static final int PARAMS_LENGTH = 2;

        /**
         * 状态码的长度
         */
        public static final int STATUS_CODE_LENGTH = 1;

        /**
         * 成功
         */
        public final static int STATUS_CODE_SUCCESS = 0x00;

        /**
         * 失败-数据不合法
         */
        public final static int STATUS_CODE_ILLEGAL = 0x01;
    }

    /**
     * 报文头中内容：2个字节，通讯编号
     */
    protected byte[] _communicationNo = new byte[]{0x00, 0x01};

    /**
     * 报文头中内容：2个字节，协议标识，既版本号
     */
    protected byte[] _protocolVersion = new byte[]{0x00, 0x03};

    /**
     * 报文头中内容：2个字节，实际数据长度
     */
    protected byte[] _dataLength;
    protected int dataLength;

    /**
     * 报文头中内容：1个字节，设备地址
     */
    protected byte _deviceAddress = 0x01;

    /**
     * 数据区中内容：10个字节，数据采集器序列号，随便传，字节数组长度为10即可
     */
    protected byte[] _dataloggingSn = "0000000000".getBytes();

    /**
     * 未加密的数据区
     */
    protected byte[] _dataArea;

    /**
     * 报文头=数据总长度（2字节）+协议标识（2字节）+数据长度（2字节）+设备地址（1字节）
     */
    @Override
    public byte[] get_Header() {
        return ByteUtils.join(_communicationNo, _protocolVersion, _dataLength, new byte[]{_deviceAddress});
    }

    /**
     * 数据区（未加密）
     */
    @Override
    public byte[] get_dataArea() {
        return _dataArea;
    }


    @Override
    public byte[] getBytes() {
        return ByteUtils.join(get_Header(), new byte[]{get_functionCode()}, get_dataArea());
    }

    /**
     * 原始字节数组
     */
    @Override
    public byte[] getDecodeBytes() {
        return getBytes();
    }

    /**
     * 从response中取出数据区数据
     */
    public static byte[] getDataArea(byte[] response) {
        int startPosition = ProtocolConstant.HEADER_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH;
        int dataAreaLengthInt = response.length - (ProtocolConstant.HEADER_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH);
        byte[] dataArea = new byte[dataAreaLengthInt];
        System.arraycopy(response, startPosition, dataArea, 0, dataAreaLengthInt);
        return dataArea;
    }

}
