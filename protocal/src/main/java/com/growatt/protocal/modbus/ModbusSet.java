package com.growatt.protocal.modbus;



/**
 * 支持功能码为0X10连续寄存器地址批量设置
 */
public class ModbusSet extends Modbus {

    /**
     * @param start      开始寄存器地址
     * @param _setValues 设置的数据
     * @return
     */
    public static ModbusSet newInstance(int start, byte[] _setValues) {
        ModbusSet modbus = new ModbusSet();
        modbus._start = ByteUtils.intTo2Byte(start);
        modbus._count = ByteUtils.intTo2Byte(_setValues.length / 2);
        modbus._byteCount = ByteUtils.intToByte(_setValues.length);
        modbus._values = ByteUtils.join(modbus._start, modbus._count, new byte[]{modbus._byteCount}, _setValues);
        int crc16 = CRC16Util.calcCrc16(modbus.getBytesWithoutCrc());
        modbus._crc16 = ByteUtils.intTo2Byte(crc16);
        return modbus;
    }

    /**
     * 开始寄存器
     */
    private byte[] _start;

    /**
     * 设置寄存器个数
     */
    private byte[] _count;

    /**
     * 设置数据的字节数量
     */
    private byte _byteCount;

    @Override
    byte get_FunctionCode() {
        return 0X10;
    }

}
