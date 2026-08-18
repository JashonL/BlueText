package com.growatt.protocal.version6;

import androidx.annotation.NonNull;


import com.growatt.protocal.Param;
import com.growatt.protocal.utils.AESCBCUtil;
import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;

import java.util.ArrayList;
import java.util.List;

/**
 * 0X18命令，设置作用
 * 对采集器进行设置
 * 数据区=数据采集器序列号（10字节）+参数编号个数（2字节）+设置数据长度（2字节）+设置数据（（参数编号（2字节）+参数长度（2字节）+参数数据）*N）+AES加密补0区
 */
public class Protocol0X18 extends Protocol {

    /**
     * 设置一个参数
     */
    public static Protocol0X18 newInstance(int paramNo, String value) throws Exception {
        List<Param> paramList = new ArrayList<>();
        paramList.add(Param.newInstance(paramNo, value));
        return newInstance(paramList);
    }

    /**
     * 设置多个参数
     */
    public static Protocol0X18 newInstance(@NonNull List<Param> paramList) throws Exception {
        Protocol0X18 protocol = new Protocol0X18();
        //设置数据长度
        protocol.paramsLength = 0;
        for (Param param : paramList) {
            protocol.paramsLength += param.getLength();
        }
        protocol._paramsLength = ByteUtils.intTo2Byte(protocol.paramsLength);
        //设置数据
        protocol._params = new byte[protocol.paramsLength];
        int currentPos = 0;
        for (Param param : paramList) {
            int paramLength = param.getLength();
            System.arraycopy(param.getBytes(), 0, protocol._params, currentPos, paramLength);
            currentPos += paramLength;
        }

        //参数编号个数(既参数个数)
        protocol.paramNoCount = paramList.size();
        protocol._paramNoCount = ByteUtils.intTo2Byte(protocol.paramNoCount);

        //数据区长度（未包含补0）
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + ProtocolConstant.PARAMS_LENGTH + protocol._params.length;
     /*   if (length % 16 == 0) {
            protocol._aesEncryptZero = new byte[0];
        }else {
            protocol._aesEncryptZero = ByteUtils.createPKCS7Padding(length);
        }*/

        //数据长度=设备地址长度+功能码长度+数据区长度（不包含补0区）
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //未加密的数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._paramNoCount, protocol._paramsLength, protocol._params);
        protocol._dataAreaWithAES = AESCBCUtil.AESEncryption(protocol._dataArea);

        //算出总长度
        protocol.totalLength = ProtocolConstant.PROTOCOL_VERSION_LENGTH + ProtocolConstant.DATA_LENGTH + protocol.dataLength + ProtocolConstant.CRC_16_LENGTH;
        protocol._totalLength = ByteUtils.intTo2Byte(protocol.totalLength);

        //crc16检验内容
        byte[] crc16CalcContent = ByteUtils.join(protocol._totalLength, protocol._protocolVersion, protocol._dataLength, new byte[]{protocol._deviceAddress}, new byte[]{protocol.get_functionCode()}, protocol._dataAreaWithAES);
        protocol.crc16 = CRC16Util.calcCrc16(crc16CalcContent);
        protocol._crc16 = ByteUtils.intTo2Byte(protocol.crc16);

        return protocol;
    }

    /**
     * 数据区中内容：2个字节，编号个数
     */
    private byte[] _paramNoCount = new byte[2];
    private int paramNoCount;

    /**
     * 数据区中内容：2个字节，设置数据的长度
     */
    private byte[] _paramsLength;
    private int paramsLength;

    /**
     * 数据区中内容：设置数据（参数格式）
     */
    private byte[] _params;

    @Override
    public byte get_functionCode() {
        return 0X18;
    }

    /**
     * 是否设置成功
     */
    public static boolean isSetSuccess(byte[] response) throws Exception {
        if(CRC16Util.crc16Verify(response)){
            byte[] dataArea = Protocol.getDecodeDataArea(response);
            int statusCode = dataArea[ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH];
            return statusCode == ProtocolConstant.STATUS_CODE_SUCCESS;
        }
        return false;
    }

}
