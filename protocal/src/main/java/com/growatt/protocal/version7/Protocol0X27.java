package com.growatt.protocal.version7;


import com.growatt.protocal.LocalManager;
import com.growatt.protocal.utils.AESCBCUtil;
import com.growatt.protocal.utils.ByteUtils;
import com.growatt.protocal.utils.CRC16Util;
import com.growatt.protocal.version6.Protocol;

/**
 * 0X27命令，下发固件文件升级命令
 * 数据区=数据采集器序列号（10字节）+数据段长度（2字节）+数据段内容（文件属性数据或者文件块数据）+AES加密补0区
 */
public class Protocol0X27 extends Protocol {

    //文件类型
    public final static byte SCRIPT_FILE = 0x01;//脚本文件
    public final static byte FIRMWARE_FILE = 0x02;//固件文件

    public static Protocol0X27 newInstance(IDataSegment dataSegment, boolean isEncrypt) throws Exception {
        Protocol0X27 protocol = new Protocol0X27();
        protocol._protocolVersion = new byte[]{0x00, 0x07};
        protocol._functionCode = dataSegment.getFunctionCode();
        protocol._dataSegment = dataSegment.getBytes();
        protocol._dataSegmentLength = ByteUtils.intTo2Byte(protocol._dataSegment.length);
        protocol.isEncrypt = isEncrypt;


        //数据区长度（不包含补0）
        int length = ProtocolConstant.DATA_LOGGING_SN_LENGTH + protocol.DATA_SEGMENT_LENGTH_LENGTH + protocol._dataSegment.length;

        //数据长度=设备地址长度+功能码长度+数据区长度（不包含补0区）
        protocol.dataLength = ProtocolConstant.DEVICE_ADDRESS_LENGTH + ProtocolConstant.FUNCTION_CODE_LENGTH + length;
        protocol._dataLength = ByteUtils.intTo2Byte(protocol.dataLength);

        //未加密的数据区数据
        protocol._dataArea = ByteUtils.join(protocol._dataloggingSn, protocol._dataSegmentLength, protocol._dataSegment);
        if (protocol.isEncrypt) {
          /*  if (length % 16 == 0) {
                protocol._aesEncryptZero = new byte[0];
            } else {
                protocol._aesEncryptZero = ByteUtils.createPKCS7Padding(length);
            }*/
            //加密，需要AES补0
            protocol._dataAreaWithAES = AESCBCUtil.AESEncryption(ByteUtils.join(protocol._dataArea));
        }

        //算出总长度
        protocol.totalLength = ProtocolConstant.PROTOCOL_VERSION_LENGTH + ProtocolConstant.DATA_LENGTH + protocol.dataLength + ProtocolConstant.CRC_16_LENGTH;
        if (protocol.isEncrypt) {
            protocol.totalLength = protocol.totalLength;
        }
        protocol._totalLength = ByteUtils.intTo2Byte(protocol.totalLength);

        //crc16检验内容
        byte[] crc16CalcContent = ByteUtils.join(protocol._totalLength, protocol._protocolVersion, protocol._dataLength, new byte[]{protocol._deviceAddress}, new byte[]{protocol.get_functionCode()});
        if (protocol.isEncrypt) {
            crc16CalcContent = ByteUtils.join(crc16CalcContent, protocol._dataAreaWithAES);
        } else {
            crc16CalcContent = ByteUtils.join(crc16CalcContent, protocol._dataArea);
        }
        protocol.crc16 = CRC16Util.calcCrc16(crc16CalcContent);
        protocol._crc16 = ByteUtils.intTo2Byte(protocol.crc16);

        protocol.isEncrypt = isEncrypt;

        return protocol;
    }

    /**
     * 数据区：数据段长度
     */
    private byte[] _dataSegmentLength;
    private final int DATA_SEGMENT_LENGTH_LENGTH = 2;

    /**
     * 数据区：数据段
     */
    private byte[] _dataSegment;

    private byte _functionCode;

    @Override
    public byte get_functionCode() {
        return _functionCode;
    }

    //文件属性数据是否设置成功，区分加密与不加密的解析
    public static boolean isSetFileAttributeSuccess(byte[] response, boolean isEncrypt) throws Exception {
        if (CRC16Util.crc16Verify(response)) {
            byte[] dataArea;
            if (isEncrypt) {
                dataArea = Protocol.getDecodeDataArea(response);
            } else {
                dataArea = Protocol.getDataArea(response);
            }
            int statusCode = dataArea[ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + FileAttribute.UPGRADE_FILE_TYPE_LENGTH + FileAttribute.FILE_DATA_TYPE_LENGTH + FileAttribute.FILE_INDEX_LENGTH];
            return statusCode == 0;
        }
        return false;
    }

    //文件块数据是否下发成功，区分加密与不加密的解析
    public static FilePackageResponse isSendFilePackageSuccess(byte[] response, boolean isEncrypt) throws Exception {
        if (CRC16Util.crc16Verify(response)) {
            byte[] dataArea;
            if (isEncrypt) {
                dataArea = Protocol.getDecodeDataArea(response);
            } else {
                dataArea = Protocol.getDataArea(response);
            }
            LocalManager.log(ByteUtils.bytesToHexString(dataArea));
            int statusCode = dataArea[ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + FilePackage.UPGRADE_FILE_TYPE_LENGTH + FilePackage.FILE_DATA_TYPE_LENGTH + FilePackage.FILE_INDEX_LENGTH + FilePackage.PACKAGE_INDEX_LENGTH];
            //解析文件索引
            int fileIndexStartPosition = ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + FilePackage.UPGRADE_FILE_TYPE_LENGTH + FilePackage.FILE_DATA_TYPE_LENGTH;
            byte[] _fileIndex = new byte[FilePackage.FILE_INDEX_LENGTH];
            System.arraycopy(dataArea, fileIndexStartPosition, _fileIndex, 0, _fileIndex.length);
            LocalManager.log("文件索引="+ByteUtils.bytesToHexString(_fileIndex));
            //解析分段索引
            int packageIndexStartPosition = ProtocolConstant.DATA_LOGGING_SN_LENGTH + ProtocolConstant.PARAM_NO_COUNT_LENGTH + FilePackage.UPGRADE_FILE_TYPE_LENGTH + FilePackage.FILE_DATA_TYPE_LENGTH + FilePackage.FILE_INDEX_LENGTH;
            byte[] _packageIndex = new byte[FilePackage.PACKAGE_INDEX_LENGTH];
            System.arraycopy(dataArea, packageIndexStartPosition, _packageIndex, 0, _packageIndex.length);
            LocalManager.log("包索引="+ByteUtils.bytesToHexString(_packageIndex));
            return new FilePackageResponse(ByteUtils.convert2BytesToUnsignedInt(_fileIndex), ByteUtils.convert4BytesToSignedInt(_packageIndex), statusCode);
        }
        return null;
    }

    /**
     * 文件属性数据内容（升级文件类型（1字节，固定为0x01）+文件数据类型（1字节，固定为0x01）+文件索引（2个字节，从0递增）+文件大小（4字节）+分段大小（2字节）+CRC32校验码（从固件包解压出来配置文件获取，4字节））
     */
    public final static class FileAttribute implements IDataSegment {

        //文件分段大小固定为450一包
        public final static int SEGMENT_LENGTH = 450;

        //升级文件类型,0x01是脚本文件，0x02是固件文件
        private byte[] _upgradeFileType;
        private final static int UPGRADE_FILE_TYPE_LENGTH = 1;

        //文件数据类型
        private final byte[] _fileDataType = new byte[]{0x01};
        private final static int FILE_DATA_TYPE_LENGTH = 1;

        //文件索引
        private byte[] _fileIndex;
        private final static int FILE_INDEX_LENGTH = 2;

        //文件大小
        private byte[] _fileLength;
        private final static int FILE_LENGTH_LENGTH = 4;

        //分段大小
        private final byte[] _segmentLength = ByteUtils.intTo2Byte(SEGMENT_LENGTH);
        private final static int SEGMENT_LENGTH_LENGTH = 2;

        //CRC32校验码
        private byte[] _crc32;
        private final static int CRC32_LENGTH = 4;

        /**
         * @param upgradeFileType 升级文件类型,0x01是脚本文件，0x02是固件文件
         * @param fileIndex       文件索引，从0递增，文件的标识
         * @param fileLength      文件大小
         * @param crc32           十六进制的字符串
         */
        public static FileAttribute newInstance(byte upgradeFileType, int fileIndex, int fileLength, byte[] crc32) {
            FileAttribute fileAttribute = new FileAttribute();
            fileAttribute._upgradeFileType = new byte[]{upgradeFileType};
            fileAttribute._fileIndex = ByteUtils.intTo2Byte(fileIndex);
            fileAttribute._fileLength = ByteUtils.intTo4Byte(fileLength);
            fileAttribute._crc32 = crc32;
            return fileAttribute;
        }

        @Override
        public byte[] getBytes() {
            return ByteUtils.join(_upgradeFileType, _fileDataType, _fileIndex, _fileLength, _segmentLength, _crc32);
        }

        @Override
        public byte getFunctionCode() {
            return 0x27;
        }
    }

    /**
     * 文件块数据内容（升级文件类型（1字节，固定为0x02）+文件数据类型（1字节，固定为0x02）+文件索引（2个字节，从0递增）+分段索引（2个字节，从0递增）+文件块长度N（2字节）+文件块数据（N字节）+CRC16校验码
     */
    public final static class FilePackage implements IDataSegment {

        //升级文件类型,0x01是脚本文件，0x02是固件文件
        private byte[] _upgradeFileType;
        private final static int UPGRADE_FILE_TYPE_LENGTH = 1;

        //文件数据类型
        private final byte[] _fileDataType = new byte[]{0x02};
        private final static int FILE_DATA_TYPE_LENGTH = 1;

        //文件索引
        private byte[] _fileIndex;
        private final static int FILE_INDEX_LENGTH = 2;

        //分段索引
        private byte[] _packageIndex;
        private final static int PACKAGE_INDEX_LENGTH = 4;

        //文件块长度
        private byte[] _filePackageLength;
        private final static int FILE_PACKAGE_LENGTH_LENGTH = 2;

        //文件块数据
        private byte[] _filePackages;

        //CRC32校验码
        private byte[] _crc16;
        private final static int CRC16_LENGTH = 2;

        private boolean isLastPackage;

        /**
         * @param upgradeFileType 升级文件类型,0x01是脚本文件，0x02是固件文件
         * @param fileIndex       文件索引，从0递增，文件的标识
         * @param packageIndex    分段索引，从0递增，每一包的标识
         * @param filePackages    文件块数据
         */
        public static FilePackage newInstance(byte upgradeFileType, int fileIndex, int packageIndex, byte[] filePackages, boolean isLastPackage) {
            FilePackage filePackage = new FilePackage();
            filePackage._upgradeFileType = new byte[]{upgradeFileType};
            filePackage._fileIndex = ByteUtils.intTo2Byte(fileIndex);
            filePackage._packageIndex = ByteUtils.intTo4Byte(packageIndex);
            filePackage._filePackageLength = ByteUtils.intTo2Byte(filePackages.length);
            filePackage._filePackages = filePackages;

            //crc16检验内容
            int crc16Int = CRC16Util.calcCrc16(filePackages);
            filePackage._crc16 = ByteUtils.intTo2Byte(crc16Int);

            filePackage.isLastPackage = isLastPackage;
            return filePackage;
        }

        @Override
        public byte[] getBytes() {
            return ByteUtils.join(_upgradeFileType, _fileDataType, _fileIndex, _packageIndex, _filePackageLength, _filePackages, _crc16);
        }

        @Override
        public byte getFunctionCode() {
            if (isLastPackage) {
                return 0X27;
            }
            return ByteUtils.intToByte(0xA7);
        }

    }

    public interface IDataSegment {
        byte[] getBytes();

        byte getFunctionCode();
    }

    public static class FilePackageResponse {
        /**
         * 出现异常包的文件索引
         */
        private int fileIndex;
        /**
         * 出现异常包的分段索引
         */
        private int packageIndex;
        /**
         * 0-成功
         * 1-接收异常，从指定分段索引重发
         * 2-整体检验错误-重新下发文件
         * 3-其他错误（采集器准备失败）-重新下发文件
         */
        private int statusCode;

        public FilePackageResponse(int fileIndex, int packageIndex, int statusCode) {
            this.fileIndex = fileIndex;
            this.packageIndex = packageIndex;
            this.statusCode = statusCode;
        }

        public int getPackageIndex() {
            return packageIndex;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isSendSuccess() {
            return statusCode == 0;
        }

        public int getFileIndex() {
            return fileIndex;
        }

        @Override
        public String toString() {
            return "FilePackageResponse{" +
                    "fileIndex=" + fileIndex +
                    ", packageIndex=" + packageIndex +
                    ", statusCode=" + statusCode +
                    '}';
        }
    }

}
