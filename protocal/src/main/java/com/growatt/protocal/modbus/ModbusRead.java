package com.growatt.protocal.modbus;


import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;

/**
 * 支持0X03与0X04功能码读取
 * 例如start=0X01,count=3，那就是读取0X01,0X02,0X03共3个寄存器的值
 */
public class ModbusRead extends Modbus {

    /**
     * @param functionCode 功能码 0X03或者0X04
     * @param start        开始寄存器地址
     * @param count        寄存器个数
     * @return
     */
    public static ModbusRead newInstance(int functionCode, int start, int count) {
        ModbusRead modbusRead = new ModbusRead();
        modbusRead._functionCode = ByteUtils.intToByte(functionCode);
        modbusRead._start = ByteUtils.intTo2Byte(start);
        modbusRead._count = ByteUtils.intTo2Byte(count);
        modbusRead._values = ByteUtils.join(modbusRead._start, modbusRead._count);
        int crc16 = CRC16Util.calcCrc16(modbusRead.getBytesWithoutCrc());
        modbusRead._crc16 = ByteUtils.intTo2Byte(crc16);
        return modbusRead;
    }

    private byte _functionCode;

    /**
     * 开始寄存器
     */
    private byte[] _start;

    /**
     * 读取寄存器个数
     */
    private byte[] _count;

    @Override
    byte get_FunctionCode() {
        return _functionCode;
    }

}
