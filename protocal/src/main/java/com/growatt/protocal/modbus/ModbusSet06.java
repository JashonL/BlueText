package com.growatt.protocal.modbus;


import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;

/**
 * 支持功能码为0X10连续寄存器地址批量设置
 */
public class ModbusSet06 extends Modbus {

    /**
     * @param _addr      寄存器地址
     * @param _setValues 设置的数据
     * @return
     */
    public static ModbusSet06 newInstance(int _addr, byte[] _setValues) {
        ModbusSet06 modbus = new ModbusSet06();
        modbus._addr = ByteUtils.intTo2Byte(_addr);
        modbus._values = ByteUtils.join(modbus._addr, _setValues);
        int crc16 = CRC16Util.calcCrc16(modbus.getBytesWithoutCrc());
        modbus._crc16 = ByteUtils.intTo2Byte(crc16);
        return modbus;
    }

    /**
     * 寄存器地址
     */
    private byte[] _addr;



    @Override
    byte get_FunctionCode() {
        return 0X06;
    }

}
