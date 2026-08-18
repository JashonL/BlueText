package com.growatt.protocal.version3;



import com.growatt.protocal.modbus.Modbus;
import com.growatt.protocal.modbus.ModbusRead;
import com.growatt.protocal.modbus.ModbusSet;
import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 0X17命令，透传命令
 * 对逆变器进行读取或者设置
 * 数据区=数据采集器序列号（10字节）+透传数据区的数据长度（2字节）+透传数据区（Modbus协议内容）
 */
public class Protocol0X17 extends Protocol {

    /**
     * 03功能码：holding
     * 读取一个寄存器值
     */
    public static Protocol0X17 newInstanceFor0X03(int registerAddress) {
        return newInstanceFor0X03(registerAddress, 1);
    }

    /**
     * 04功能码：input
     * 读取一个寄存器值
     */
    public static Protocol0X17 newInstanceFor0X04(int registerAddress) {
        return newInstanceFor0X04(registerAddress, 1);
    }

    /**
     * 03功能码：holding
     * 读取多个连续地址寄存器的值
     */
    public static Protocol0X17 newInstanceFor0X03(int registerAddress, int count) {
        return newInstanceForRead(0X03, registerAddress, count);
    }

    /**
     * 04功能码：input
     * 读取多个连续地址寄存器的值
     */
    public static Protocol0X17 newInstanceFor0X04(int registerAddress, int count) {
        return newInstanceForRead(0X04, registerAddress, count);
    }

    /**
     * @param functionCode         功能码：0X03或者0X04
     * @param startRegisterAddress 开始寄存器地址
     * @param count                数据读取个数
     */
    public static Protocol0X17 newInstanceForRead(int functionCode, int startRegisterAddress, int count) {
        ModbusRead modbusRead = ModbusRead.newInstance(functionCode, startRegisterAddress, count);
        return newInstance(modbusRead.getBytes());
    }

    /**
     * 单个设置与批量设置连续寄存器地址的值
     *
     * @param startRegisterAddress 开始寄存器地址
     * @param _setValues           设置的数据
     */
    public static Protocol0X17 newInstanceForSet(int startRegisterAddress, byte[] _setValues) {
        ModbusSet modbusSet = ModbusSet.newInstance(startRegisterAddress, _setValues);
        return newInstance(modbusSet.getBytes());
    }

    /**
     * @param _modbusData 透传数据区数据，即modbus数据
     */
    private static Protocol0X17 newInstance(byte[] _modbusData) {
        Protocol0X17 protocol = new Protocol0X17();
        protocol._modbusData = _modbusData;
        protocol._modbusDataLength = ByteUtils.intTo2Byte(protocol._modbusData.length);

        //数据区长度
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + MODBUS_DATA_LENGTH + protocol._modbusData.length;

        //数据长度=设备地址长度+功能码长度+数据区长度
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //未加密的数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._modbusDataLength, protocol._modbusData);

        return protocol;
    }

    /**
     * 数据区中内容：2个字节，透传数据区的数据长度
     */
    private byte[] _modbusDataLength;
    private static final int MODBUS_DATA_LENGTH = 2;

    /**
     * 数据区中内容：透传数据区数据
     */
    private byte[] _modbusData;

    @Override
    public byte get_functionCode() {
        return 0X17;
    }

    /**
     * 返回来寄存器值，每个寄存器的值占2个字节
     */
    public static List<byte[]> getValues(byte[] rawResponse) {
        List<byte[]> params = new ArrayList<>();
        byte[] _modbusData = getModbusData(rawResponse);
        if (_modbusData != null) {
            //字节数目
            int byteCount = ByteUtils.byteToUnsignedInt(_modbusData[Modbus.ADDRESS_LENGTH + Modbus.FUNCTION_CODE_LENGTH]);
            //寄存器值
            byte[] _values = new byte[byteCount];
            System.arraycopy(_modbusData, Modbus.ADDRESS_LENGTH + Modbus.FUNCTION_CODE_LENGTH + 1, _values, 0, byteCount);
            int currentPosition = 0;
            while (currentPosition < byteCount) {
                params.add(new byte[]{_values[currentPosition], _values[currentPosition + 1]});
                currentPosition += 2;
            }
        }
        return params;
    }

    /**
     * @return modbus数据, null代表错误响应，否则是正确响应
     */
    public static byte[] getModbusData(byte[] rawResponse) {
        byte[] dataArea = Protocol.getDataArea(rawResponse);
        byte[] _modbusDataLength = new byte[MODBUS_DATA_LENGTH];
        System.arraycopy(dataArea, ProtocolConstant.DATA_LOGGING_SN_LENGTH, _modbusDataLength, 0, _modbusDataLength.length);
        int modbusDataLength = ByteUtils.convert2BytesToUnsignedInt(_modbusDataLength);
        //地址(1个字节)+功能码或者错误码(1个字节)+字节数目(1个字节)+数据+crc(2个字节)
        byte[] _modbusData = new byte[modbusDataLength];
        System.arraycopy(dataArea, ProtocolConstant.DATA_LOGGING_SN_LENGTH + MODBUS_DATA_LENGTH, _modbusData, 0, modbusDataLength);
        if (CRC16Util.crc16VerifyForModbus(_modbusData)) {
            boolean isSuccess = !(ByteUtils.byteToUnsignedInt(_modbusData[Modbus.ADDRESS_LENGTH]) > 0X80);
            if (isSuccess) {
                return _modbusData;
            }
        }
        return null;
    }

    public static boolean isSetSuccess(byte[] rawResponse) {
        return getModbusData(rawResponse) != null;
    }

}
