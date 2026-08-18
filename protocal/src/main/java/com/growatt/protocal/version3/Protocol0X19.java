package com.growatt.protocal.version3;



import com.growatt.protocal.Param;
import com.growatt.protocal.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 0X19命令，读取作用
 * 对采集器参数进行读取
 * 数据区=数据采集器序列号（10字节）+参数编号个数（2字节）+读取参数编号列表（参数编号（2字节）*N）+AES加密补0区
 */
public class Protocol0X19 extends Protocol {

    /**
     * 读取一个编号数据
     */
    public static Protocol0X19 newInstance(int paramNo) {
        return newInstance(new int[]{paramNo});
    }

    /**
     * 读取多个编号数据
     */
    public static Protocol0X19 newInstance(int[] paramNos) {
        Protocol0X19 protocol = new Protocol0X19();
        //参数编号个数(既参数个数)
        protocol.paramNoCount = paramNos.length;
        protocol._paramNoCount = ByteUtils.intTo2Byte(protocol.paramNoCount);
        protocol._paramNos = new byte[protocol.paramNoCount * 2];
        int currentPos = 0;
        for (int i = 0; i < paramNos.length; i++) {
            byte[] _paramNo = ByteUtils.intTo2Byte(paramNos[i]);
            System.arraycopy(_paramNo, 0, protocol._paramNos, currentPos, _paramNo.length);
            currentPos = (i + 1) * 2;
        }

        //数据区长度
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + protocol._paramNos.length;

        //数据长度=设备地址长度+功能码长度+数据区长度
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._paramNoCount, protocol._paramNos);

        return protocol;
    }

    /**
     * 数据区中内容：2个字节，编号个数
     */
    private byte[] _paramNoCount;
    private int paramNoCount;

    /**
     * 数据区中内容：读取编号列表（参数格式）
     */
    private byte[] _paramNos;

    @Override
    public byte get_functionCode() {
        return 0X19;
    }

    public static String readOneParamText(byte[] response) {
        String value = "";
        List<Param> params = readParams(response);
        if (params.size() == 1) {
            value = params.get(0).readParamValueText();
        }
        return value;
    }

    public static List<Param> readParams(byte[] response) {
        List<Param> params = new ArrayList<>();
        byte[] dataArea = Protocol.getDataArea(response);
        int statusCode = dataArea[ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH];
        if (statusCode == ProtocolConstant.STATUS_CODE_SUCCESS) {
            //数据长度
            int start = ProtocolConstant.COMMUNICATION_NO_LENGTH + ProtocolConstant.PROTOCOL_VERSION_LENGTH;
            byte[] _dataLength = new byte[ProtocolConstant.DATA_LENGTH];
            System.arraycopy(response, start, _dataLength, 0, ProtocolConstant.DATA_LENGTH);
            int dataLength = ByteUtils.convert2BytesToUnsignedInt(_dataLength);
            //有效数据
            int paramsLength = getParamsLength(dataLength);
            byte[] _params = new byte[paramsLength];
            System.arraycopy(dataArea, getDataAreaSrcPos(), _params, 0, paramsLength);
            //遍历取出有效数据
            int readPosition = 0;
            while (readPosition < paramsLength) {
                //参数编号
                byte[] _paramNo = new byte[ProtocolConstant.PARAM_NO_COUNT_LENGTH];
                System.arraycopy(_params, readPosition, _paramNo, 0, _paramNo.length);
                int paramNo = ByteUtils.convert2BytesToUnsignedInt(_paramNo);
                //参数长度
                readPosition += _paramNo.length;
                byte[] _paramLength = new byte[Param.PARAM_LENGTH];
                System.arraycopy(_params, readPosition, _paramLength, 0, ProtocolConstant.PARAM_NO_COUNT_LENGTH);
                int paramLength = ByteUtils.convert2BytesToUnsignedInt(_paramLength);
                //参数数据
                readPosition += _paramLength.length;
                byte[] _param = new byte[paramLength];
                System.arraycopy(_params, readPosition, _param, 0, paramLength);
                readPosition += paramLength;
                params.add(Param.newInstance(_paramNo, paramNo, _paramLength, paramLength, _param));
            }
        }
        return params;
    }

    /**
     * @param dataLength 实际数据长度
     * @return 有效数据长度
     */
    private static int getParamsLength(int dataLength) {
        return dataLength - ProtocolConstant.DEVICE_ADDRESS_LENGTH - ProtocolConstant.FUNCTION_CODE_LENGTH - ProtocolConstant.DATA_LOGGING_SN_LENGTH - ProtocolConstant.PARAM_NO_COUNT_LENGTH - ProtocolConstant.STATUS_CODE_LENGTH;
    }

    /**
     * 获取有效数据在response中的起始位置
     */
    private static int getDataAreaSrcPos() {
        return ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + ProtocolConstant.STATUS_CODE_LENGTH;
    }
}
