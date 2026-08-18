package com.growatt.protocal.version6;


import com.growatt.protocal.utils.AESCBCUtil;
import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;

/**
 * 0X26命令，下发文件进行升级
 * <p>
 * 数据区=数据采集器序列号（10字节）+数据串长度（2字节）+数据串内容（文件数据分包总数量（2字节）+文件数据分包总数量（2字节）+文件数据包数据）+AES加密补0区
 */
public class Protocol0X26 extends Protocol {

    /**
     * @param fileTotalCount   文件数据分包总数量,实际升级内容的分包，不包含20字节校验内容
     * @param currentPackageNo 当前数据包编号，从1开始，编号为0是20字节校验内容
     * @param _fileData        当前数据包内容
     */
    public static Protocol0X26 newInstance(int fileTotalCount, int currentPackageNo, byte[] _fileData) throws Exception {
        Protocol0X26 protocol = new Protocol0X26();
        protocol._fileTotalCount = ByteUtils.intTo2Byte(fileTotalCount - 1);
        protocol._currentPackageNo = ByteUtils.intTo2Byte(currentPackageNo);
        protocol._fileData = _fileData;
        int fileDataSectionLength = FILE_TOTAL_COUNT + CURRENT_PACKAGE_NO + _fileData.length;
        protocol._fileDataSectionLength = ByteUtils.intTo2Byte(fileDataSectionLength);

        //数据区长度（未包含补0）
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + FILE_DATA_SECTION_LENGTH + fileDataSectionLength;
      /*  if (length % 16 == 0) {
            protocol._aesEncryptZero = new byte[0];
        }else {
            protocol._aesEncryptZero = ByteUtils.createPKCS7Padding(length);
        }*/

        //数据长度=设备地址长度+功能码长度+数据区长度（不包含补0区）
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //未加密的数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._fileDataSectionLength, protocol._fileTotalCount, protocol._currentPackageNo, protocol._fileData);
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
     * 数据串长度
     */
    private byte[] _fileDataSectionLength;
    private final static int FILE_DATA_SECTION_LENGTH = 2;

    /**
     * 数据串内容：文件数据分包总数量,实际升级内容的分包，不包含20字节校验内容
     */
    private byte[] _fileTotalCount;
    private final static int FILE_TOTAL_COUNT = 2;

    /**
     * 数据串内容：当前数据包编号，从1开始，编号为0是20字节校验内容
     */
    private byte[] _currentPackageNo;
    private final static int CURRENT_PACKAGE_NO = 2;

    /**
     * 数据串内容：当前数据包内容
     */
    private byte[] _fileData;


    @Override
    public byte get_functionCode() {
        return 0X26;
    }

    /**
     * 获取结果
     */
    public static Result parse(byte[] response) throws Exception {
        byte[] dataArea = Protocol.getDecodeDataArea(response);
        byte[] result = new byte[5];
        System.arraycopy(dataArea, ProtocolConstant.DATA_LOGGING_SN_LENGTH, result, 0, 2 + 2 + 1);
        int fileTotalCount = ByteUtils.convert2BytesToUnsignedInt(new byte[]{result[0], result[1]});
        int currentPackageNo = ByteUtils.convert2BytesToUnsignedInt(new byte[]{result[2], result[3]});
        int status = ByteUtils.byteToUnsignedInt(result[4]);
        return new Result(fileTotalCount, currentPackageNo, status);
    }

    public final static class Result {
        //文件数据分包总数量
        private int fileTotalCount;
        //当前数据包编号
        private int currentPackageNo;
        /**
         * 当前数据包接收状态码
         * 0-成功，发送下一包
         * 1-接收异常，再次发送当前包
         * 2-整体校验错误，重发文件第一包
         * 3-其它错误，重发文件第一包
         * 4-接收异常，发送本次命令返回来的包编号数据
         */
        private int status;

        public Result(int fileTotalCount, int currentPackageNo, int status) {
            this.fileTotalCount = fileTotalCount;
            this.currentPackageNo = currentPackageNo;
            this.status = status;
        }

        public int getFileTotalCount() {
            return fileTotalCount;
        }

        public int getCurrentPackageNo() {
            return currentPackageNo;
        }

        public int getStatus() {
            return status;
        }
    }

}
