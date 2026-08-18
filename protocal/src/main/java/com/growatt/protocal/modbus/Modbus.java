package com.growatt.protocal.modbus;



/**
 * Modbus协议基类
 * 地址域(长度为1字节) + 功能码(长度为1字节) + 数据（长度为N字节） + 差错校验(长度为2字节)
 */
public abstract class Modbus {

    /**
     * 寄存器地址常量
     */
    public static final class AddressConstant {
        /**
         * 绑定逆变器到NOAH电源
         */
        public static final int ADDRESS_BIND_INVERTER_TO_NOAH = 300;
    }

    //地址
    protected byte _address = 0x01;

    public static final int ADDRESS_LENGTH = 1;
    public static final int FUNCTION_CODE_LENGTH = 1;

    //数据
    protected byte[] _values;

    protected byte[] _crc16;

    /**
     * 功能码
     */
    abstract byte get_FunctionCode();

    /**
     * _crc16需要倒序放进去
     */
    public byte[] getBytes() {
        return ByteUtils.join(new byte[]{_address, get_FunctionCode()}, _values, new byte[]{_crc16[1], _crc16[0]});
    }

    public byte[] getBytesWithoutCrc() {
        return ByteUtils.join(new byte[]{_address, get_FunctionCode()}, _values);
    }
}
