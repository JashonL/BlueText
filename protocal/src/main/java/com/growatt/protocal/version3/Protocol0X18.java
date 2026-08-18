package com.growatt.protocal.version3;

import androidx.annotation.NonNull;


import com.growatt.protocal.Param;
import com.growatt.protocal.utils.ByteUtils;

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
    public static Protocol0X18 newInstance(int paramNo, String value) {
        List<Param> paramList = new ArrayList<>();
        paramList.add(Param.newInstance(paramNo, value));
        return newInstance(paramList);
    }

    /**
     * 设置多个参数
     */
    public static Protocol0X18 newInstance(@NonNull List<Param> paramList) {
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

        //数据区长度
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + ProtocolConstant.PARAMS_LENGTH + protocol._params.length;

        //数据长度=设备地址长度+功能码长度+数据区长度
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //未加密的数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._paramNoCount, protocol._paramsLength, protocol._params);

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
    public static boolean isSetSuccess(byte[] response) {
        byte[] dataArea = Protocol.getDataArea(response);
        int statusCode = dataArea[ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH];
        return statusCode == ProtocolConstant.STATUS_CODE_SUCCESS;
    }

}
