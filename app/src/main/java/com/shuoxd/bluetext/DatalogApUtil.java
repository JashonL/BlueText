package com.shuoxd.bluetext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import com.mylhyl.circledialog.callback.ConfigInput;
import com.mylhyl.circledialog.view.listener.OnInputClickListener;
import com.shuoxd.bluetext.datalogConfig.CircleDialogUtils;
import com.shuoxd.bluetext.datalogConfig.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DatalogApUtil {

    //获取采集器信息命令
    public static final byte DATALOG_GETDATA_0X19 = 0x19;
    public static final byte DATALOG_GETDATA_0X18 = 0x18;
    public static final byte DATALOG_GETDATA_0X26 = 0x26;


    public static int datalogConfigMode = Constant.CONFIG_STANDART;

    public static int getDatalogConfigMode() {
        return datalogConfigMode;
    }

    public static void setDatalogConfigMode(int localDebugMode) {
        DatalogApUtil.datalogConfigMode = localDebugMode;
    }


    // 转换字符编码
    public static Charset charset = Charset.forName("ISO-8859-1");

    // 解密采集器数据公共密钥 "Growatt"
    public static final String secretKey = "Growatt";

    /**
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @return
     */
    public static byte[] sendMsg(byte fun, String deviceId, int[] valus) throws Exception {
        //数服协议封装
        return numberServerPro(fun, deviceId, valus);
    }

    /**
     * 发送查询命令
     *
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @param fun      功能码
     * @return
     */
    public static byte[] numberServerPro(byte fun, String deviceId, int[] valus) throws Exception {
        DatalogApMsgBean msgBean = new DatalogApMsgBean();

        //报文头部分
        //报文头数据长度=设备地址(1字节)+功能码(1字节)+数据采集器(10字节)+参数编号个数(2字节)+参数编号(每个编号2字节)
        int datalen = 1 + 1 + 10 + 2 + valus.length * 2;
        byte[] mBytesBapLen = int2Byte(datalen);
        msgBean.setMbap_len(mBytesBapLen);

        //功能码
        msgBean.setFun_code(fun);

        //数据区
        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //参数编号个数
        int paramNum = valus.length;
        byte[] funNumByte = int2Byte(paramNum);
        //设置数据
        byte[] dataByte = new byte[valus.length * 2];
        for (int i = 0; i < valus.length; i++) {
            int valus1 = valus[i];
            byte[] bytes1 = int2Byte(valus1);
            System.arraycopy(bytes1, 0, dataByte, bytes1.length * i, bytes1.length);
        }

        //有效数据
        int alllen = serialBytes.length + funNumByte.length + dataByte.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(funNumByte, 0, allDataBytes, serialBytes.length, funNumByte.length);
        System.arraycopy(dataByte, 0, allDataBytes, serialBytes.length + funNumByte.length, dataByte.length);
        msgBean.setData(allDataBytes);

        //对数据加密
        byte[] encryptedData = getEnCode(msgBean.getBytes());
        msgBean.setEncryptedData(encryptedData);
        //获取crc效验
        int crc = CRC16.calcCrc16(encryptedData);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);
        return msgBean.getBytesCRC();
    }





    /**
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @return
     */
    public static byte[] sendMsg_blue19(byte fun, String deviceId, int[] valus) throws Exception {
        //数服协议封装
        return numberServerPro19(fun, deviceId, valus);
    }


    /**
     * 发送查询命令(0x19)------蓝牙
     *
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @param fun      功能码
     * @return
     */
    public static byte[] numberServerPro19(byte fun, String deviceId, int[] valus) throws Exception {
        DatalogBlueMsgBean msgBean=new DatalogBlueMsgBean();

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        //协议标识固定06
        //实际数据长度=设备地址+功能码+数据区内容（不包含补0的）
        msgBean.setDevice_addr(DatalogBlueMsgBean.DEVICE_CODE);
        msgBean.setFun_code(fun);
        //数据区
        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //参数编号个数
        int paramNum = valus.length;
        byte[] funNumByte = int2Byte(paramNum);
        //设置数据
        byte[] dataByte = new byte[valus.length * 2];
        for (int i = 0; i < valus.length; i++) {
            int valus1 = valus[i];
            byte[] bytes1 = int2Byte(valus1);
            System.arraycopy(bytes1, 0, dataByte, bytes1.length * i, bytes1.length);
        }
        //有效数据
        int alllen = serialBytes.length + funNumByte.length + dataByte.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(funNumByte, 0, allDataBytes, serialBytes.length, funNumByte.length);
        System.arraycopy(dataByte, 0, allDataBytes, serialBytes.length + funNumByte.length, dataByte.length);

        //对数据补0(16的倍数) 并进行AES加密
        byte[] encryptedData =  getMsgByAes(allDataBytes);
        msgBean.setData(encryptedData);

        //实际内容长度=设备地址+功能码+数据内容长度
        int realLen=1+1+alllen;
        byte[] realLenByte = int2Byte(realLen);
        msgBean.setRealdata_len(realLenByte);

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        int allLen=2+2+1+1+encryptedData.length+2;
        byte[] allLenByte = int2Byte(allLen);
        msgBean.setAll_len(allLenByte);

        //没有CRC的数据
        byte[] data = msgBean.getBytes();

        //获取crc效验
        int crc = CRC16.calcCrc16(data);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);

        return msgBean.getBytesCRC();

    }




    /**
     * @param deviceId 设备序列号
     * @param valus    设置参数的编号
     * @return
     */
    public static byte[] sendMsg_bt18(byte fun, String deviceId, List<DatalogAPSetParam> valus) throws Exception {
        //数服协议封装
        return numberServerPro18(fun, deviceId, valus);
    }


    /**
     * 发送查询命令(0x19)------蓝牙
     *
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @param fun      功能码
     * @return
     */
    public static byte[] numberServerPro18(byte fun, String deviceId, List<DatalogAPSetParam> valus) throws Exception {
        DatalogBlueMsgBean msgBean=new DatalogBlueMsgBean();

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        //协议标识固定06
        //实际数据长度=设备地址+功能码+数据区内容（不包含补0的）
        msgBean.setDevice_addr(DatalogBlueMsgBean.DEVICE_CODE);
        msgBean.setFun_code(fun);
        //数据区
        //要设置的数据
        byte[] bytes = parseBean2Byte(valus);
        int length = bytes.length;

        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //参数编号个数
        int size = valus.size();
        byte[] sizeByte = int2Byte(size);
        //设置数据长度
        byte[] lengthByte = int2Byte(length);

        //设置数据

        //有效数据
        int alllen = serialBytes.length + sizeByte.length + lengthByte.length + bytes.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(sizeByte, 0, allDataBytes, serialBytes.length, sizeByte.length);
        System.arraycopy(lengthByte, 0, allDataBytes, serialBytes.length + sizeByte.length, lengthByte.length);
        System.arraycopy(bytes, 0, allDataBytes, serialBytes.length + sizeByte.length + lengthByte.length, bytes.length);



        //对数据补0(16的倍数) 并进行AES加密
        byte[] encryptedData =  getMsgByAes(allDataBytes);
        msgBean.setData(encryptedData);

        //实际长度
        int realLen=1+1+alllen;
        byte[] realLenByte = int2Byte(realLen);
        msgBean.setRealdata_len(realLenByte);

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        int allLen=2+2+1+1+encryptedData.length+2;
        byte[] allLenByte = int2Byte(allLen);
        msgBean.setAll_len(allLenByte);

        //没有CRC的数据
        byte[] data = msgBean.getBytes();

        //获取crc效验
        int crc = CRC16.calcCrc16(data);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);
        return msgBean.getBytesCRC();

    }







    public static byte[] sendMsgByAes(byte[]bytes) throws Exception {
        byte[] bytes1 = getbyte16(bytes, 16);
        return AESCBCUtils.AESEncryption(bytes1);
    }


    public static byte[] getMsgByAes(byte[]bytes) throws Exception {
        byte[] bytes1 = getbyte16(bytes, 16);
        return AESCBCUtils.AESEncryption(bytes1);
    }





    public static byte[] msgDesCodeByAESCBC(byte []src) throws Exception {
        return AESCBCUtils.AESDecryption(src);
    }

    /**
     * 将数据补够bitnum的倍数
     *
     * @param bitNum
     * @return
     */
    public static byte[] getbyte16(byte[] data, int bitNum) {
        int length = data.length;
        int multiple = length / bitNum;

        //目标长度
        int tagetLent = (multiple+1)*bitNum;
        byte[] newData = new byte[tagetLent];
        if (length % bitNum == 0) {
            return data;
        } else {
            System.arraycopy(data, 0,  newData, 0, length);
        }
        return newData;
    }




    /**
     * @param deviceId 设备序列号
     * @param valus    设置参数的编号
     * @return
     */
    public static byte[] sendMsg(byte fun, String deviceId, List<DatalogAPSetParam> valus) throws Exception {
        //数服协议封装
        return numberServerPro(fun, deviceId, valus);
    }


    /**
     * 发送设置命令
     *
     * @param deviceId 设备序列号
     * @param valus    设置参数的值
     * @param fun      功能码
     * @return
     */
    public static byte[] numberServerPro(byte fun, String deviceId, List<DatalogAPSetParam> valus) throws Exception {
        //要设置的数据
        byte[] bytes = parseBean2Byte(valus);

        DatalogApMsgBean msgBean = new DatalogApMsgBean();


        //报文头数据长度=设备地址+功能码+数据采集器+参数编号个数+设置数据长度+设置数据
        int length = bytes.length;
        int datalen = 1 + 1 + 10 + 2 + 2 + length;
        byte[] mBytesBapLen = int2Byte(datalen);
        msgBean.setMbap_len(mBytesBapLen);

        //功能码
        msgBean.setFun_code(fun);

        //数据区
        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //参数编号个数
        int size = valus.size();
        byte[] sizeByte = int2Byte(size);
        //设置数据长度
        byte[] lengthByte = int2Byte(length);
        //设置数据

        //有效数据
        int alllen = serialBytes.length + sizeByte.length + lengthByte.length + bytes.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(sizeByte, 0, allDataBytes, serialBytes.length, sizeByte.length);
        System.arraycopy(lengthByte, 0, allDataBytes, serialBytes.length + sizeByte.length, lengthByte.length);
        System.arraycopy(bytes, 0, allDataBytes, serialBytes.length + sizeByte.length + lengthByte.length, bytes.length);
        msgBean.setData(allDataBytes);

        //对数据加密
        byte[] encryptedData = getEnCode(msgBean.getBytes());
        msgBean.setEncryptedData(encryptedData);
        //获取crc效验
        int crc = CRC16.calcCrc16(encryptedData);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);

        return msgBean.getBytesCRC();
    }





    /**
     * @param fun         功能码
     * @param deviceId    采集器序列号
     * @param totalLength 数据总长度
     * @param currNum     当前是第几个包
     * @param valus       数据包
     */

    public static byte[] updataDatalog(byte fun, String deviceId, int totalLength, int currNum, byte[] valus) throws Exception {
        DatalogApMsgBean msgBean = new DatalogApMsgBean();
        //报文头数据长度=设备地址+功能码+数据采集器+数据串长度+数据包(文件数据分包总数量+当前数据包编号+文件数据包数据)
        int length = 2 + 2 + valus.length;
        int datalen = 1 + 1 + 10 + 2 + length;
        byte[] mBytesBapLen = int2Byte(datalen);
        msgBean.setMbap_len(mBytesBapLen);
        //功能码
        msgBean.setFun_code(fun);

        //数据区
        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //数据串长度
        byte[] lengthByte = int2Byte(length);
        //分包总数量
        byte[] totalBytes = int2Byte(totalLength);
        //当前编号
        byte[] currNumBytes = int2Byte(currNum);

        int alllen = serialBytes.length + lengthByte.length + totalBytes.length + currNumBytes.length + valus.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(lengthByte, 0, allDataBytes, serialBytes.length, lengthByte.length);
        System.arraycopy(totalBytes, 0, allDataBytes, serialBytes.length + lengthByte.length, totalBytes.length);
        System.arraycopy(currNumBytes, 0, allDataBytes, serialBytes.length + lengthByte.length + totalBytes.length, currNumBytes.length);
        System.arraycopy(valus, 0, allDataBytes, serialBytes.length + lengthByte.length + totalBytes.length + currNumBytes.length, valus.length);
        msgBean.setData(allDataBytes);

        //对数据加密
        byte[] encryptedData = getEnCode(msgBean.getBytes());
        msgBean.setEncryptedData(encryptedData);
        //获取crc效验
        int crc = CRC16.calcCrc16(encryptedData);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);
        return msgBean.getBytesCRC();
    }




    /**
     * 发送查询命令(0x26)------蓝牙
     *
     * @param deviceId 设备序列号
     * @param valus    查询参数的编号
     * @param fun      功能码
     * @return
     */
    public static byte[] numberServerPro26(byte fun, String deviceId, int totalLength, int currNum, byte[] valus) throws Exception {
        DatalogBlueMsgBean msgBean=new DatalogBlueMsgBean();

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        //协议标识固定06
        //实际数据长度=设备地址+功能码+数据区内容（不包含补0的）
        msgBean.setDevice_addr(DatalogBlueMsgBean.DEVICE_CODE);
        msgBean.setFun_code(fun);
        //数据区
        //采集器序列号
        byte[] serialBytes = deviceId.getBytes();
        //数据串长度
        int length = 2 + 2 + valus.length;
        byte[] lengthByte = int2Byte(length);
        //分包总数量
        byte[] totalBytes = int2Byte(totalLength);
        //当前编号
        byte[] currNumBytes = int2Byte(currNum);

        int alllen = serialBytes.length + lengthByte.length + totalBytes.length + currNumBytes.length + valus.length;
        byte[] allDataBytes = new byte[alllen];
        System.arraycopy(serialBytes, 0, allDataBytes, 0, serialBytes.length);
        System.arraycopy(lengthByte, 0, allDataBytes, serialBytes.length, lengthByte.length);
        System.arraycopy(totalBytes, 0, allDataBytes, serialBytes.length + lengthByte.length, totalBytes.length);
        System.arraycopy(currNumBytes, 0, allDataBytes, serialBytes.length + lengthByte.length + totalBytes.length, currNumBytes.length);
        System.arraycopy(valus, 0, allDataBytes, serialBytes.length + lengthByte.length + totalBytes.length + currNumBytes.length, valus.length);


        //对数据补0(16的倍数) 并进行AES加密
        byte[] encryptedData =  getMsgByAes(allDataBytes);
        msgBean.setData(encryptedData);

        //实际长度
        int realLen=1+1+alllen;
        byte[] realLenByte = int2Byte(realLen);
        msgBean.setRealdata_len(realLenByte);

        //总长度=协议标识+实际数据长度+设备地址+功能码+数据区内容+AES加密补零区+CRC16码
        int allLen=2+2+1+1+encryptedData.length+2;
        byte[] allLenByte = int2Byte(allLen);
        msgBean.setAll_len(allLenByte);

        //没有CRC的数据
        byte[] data = msgBean.getBytes();

        //获取crc效验
        int crc = CRC16.calcCrc16(data);
        byte[] crcBytes = int2Byte(crc);
        msgBean.setCrcData(crcBytes);

        return msgBean.getBytesCRC();

    }





    private static byte[] parseBean2Byte(List<DatalogAPSetParam> valus) throws Exception {
        //1.将数据集合的转成数组集合
        List<byte[]> byteList = new ArrayList<>();
        //2.记录所有数据的byte长度
        int len = 0;
        //3.将集合中的数据转成byte数组，然后保存
        for (int i = 0; i < valus.size(); i++) {
            DatalogAPSetParam datalogAPSetParam = valus.get(i);
            String value = datalogAPSetParam.getValue();
            int paramnum = datalogAPSetParam.getParamnum();
            byte[] paramBytes = int2Byte(paramnum);
            byte[] valueBytes = value.getBytes();
            int length = valueBytes.length;
            byte[] lenBytes = int2Byte(length);
            byte[] bytes = new byte[paramBytes.length + valueBytes.length + lenBytes.length];
            len += paramBytes.length + valueBytes.length + lenBytes.length;
            System.arraycopy(paramBytes, 0, bytes, 0, paramBytes.length);
            System.arraycopy(lenBytes, 0, bytes, paramBytes.length, lenBytes.length);
            System.arraycopy(valueBytes, 0, bytes, paramBytes.length + lenBytes.length, valueBytes.length);
            byteList.add(bytes);
        }
        //4.用于返回封装好的数组
        byte[] allBeanBytes = new byte[len];
        //5.开始封装数据byte数组
        int len2 = 0;
        for (int i = 0; i < byteList.size(); i++) {
            byte[] bytes = byteList.get(i);
            System.arraycopy(bytes, 0, allBeanBytes, len2, bytes.length);
            len2 += bytes.length;
        }


        return allBeanBytes;
    }


    /**
     * 将int转成byte[2]
     *
     * @param a
     * @return
     */
    public static byte[] int2Byte(int a) {
        byte[] b = new byte[2];

        b[0] = (byte) (a >> 8);
        b[1] = (byte) (a);

        return b;
    }


    /**
     * 将byte[2]转成byte[2]
     *
     * @return
     */
    public static int byte2Int(byte[] b) {
        int value = 0;
        if (b.length > 1) {
//            value = (b[0] & 0xff << 8) | (b[1] & 0xff);
            value = (((0x000000ff & b[0]) << 8) & 0x0000ff00) | (0x000000ff & b[1]);
            return value;

        }
        return value;
    }


    /**
     * 获取加密byte[]
     *
     * @param command
     * @return
     */
    public static byte[] getEnCode(byte[] command) throws Exception {
        // 获取待加密数据长度
        int len = command.length;

        // 获取协议头
        byte[] prifixByte = Arrays.copyOfRange(command, 0, 8);
        // 获取数据
        byte[] dataByte = Arrays.copyOfRange(command, 8, len);
        // 加密
        dataByte = enCode(dataByte);
        // 组装返回
        System.arraycopy(prifixByte, 0, command, 0, prifixByte.length);
        System.arraycopy(dataByte, 0, command, prifixByte.length, dataByte.length);
        return command;
    }

    /**
     * 加密
     *
     * @param data
     * @return
     */
    public static byte[] enCode(byte[] data) throws Exception {
        byte[] secretKeyByte = secretKey.getBytes();
        // 1.初始化加密新byte[]
        byte[] backData = new byte[data.length];
        // 2.初始化索引
        int index = 0;
        // 3.加密
        for (int i = 0; i < data.length; i++) {
            if (i % (secretKeyByte.length) == 0) {
                index = 0;
            }
            backData[i] = (byte) (data[i] ^ secretKeyByte[index++]);
        }
        return backData;
    }


    /**
     * 解密
     *
     * @param data
     */
    public static byte[] desCode(byte[] data) throws Exception {
        // 1.初始化解密新byte[]
        byte[] backData = new byte[data.length];

        byte[] secretKeyByte = secretKey.getBytes();
        // 3.定义索引
        int index = 0;
        // 4.解密并赋值到新的byte[]
        for (int i = 0; i < data.length; i++) {
            if (i % secretKeyByte.length == 0) {
                index = 0;
            }
            backData[i] = (byte) (data[i] ^ secretKeyByte[index++]);
        }
        return backData;
    }


    /**
     * 解密
     *
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String desCode(String data) throws UnsupportedEncodingException {
        byte[] secretKeyByte = secretKey.getBytes();
        // 1.获取采集器数据byte[]及长度
        byte[] planintextByte = data.getBytes("ISO8859-1");
        int byteLen = planintextByte.length;
        // 2.初始化新的解密byte[]
        byte[] desCode = new byte[byteLen];
        // 3.定义索引
        int index = 0;
        // 4.解密并赋值到新的byte[]
        for (int i = 0; i < byteLen; i++) {
            if (i % secretKeyByte.length == 0) {
                index = 0;
            }
            desCode[i] = (byte) (planintextByte[i] ^ secretKeyByte[index++]);
        }
        // 5.解密后的byte[]转换为字符串
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(desCode.length);
            byteBuffer.put(desCode);
            byteBuffer.flip();
            data = String.valueOf(charset.newDecoder().decode(byteBuffer));
            return data;
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
        return data;
    }


    /**
     * 检测返回信息是否正确
     *
     * @param bytes
     * @return
     */
    public static boolean checkData(byte[] bytes) throws Exception {
        try {
            if (bytes == null) return false;
            int len = bytes.length;
            if (len > 6) {
                //返回数据长度
                int receiveLen = byte2Int(new byte[]{bytes[4], bytes[5]});
                if (receiveLen != len - 8) {
                    return false;
                }
                //crc校验
                //获取crc效验
                byte crcL = bytes[bytes.length - 1];
                byte crcH = bytes[bytes.length - 2];

                //获取CRC之外的数据
                byte[] originalByte = Arrays.copyOfRange(bytes, 0, bytes.length - 2);
                int crc = CRC16.calcCrc16(originalByte);
                byte[] crcBytes = int2Byte(crc);
                return crcBytes[0] == crcH && crcBytes[1] == crcL;

            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 弹出输入框
     *
     * @param num 参数编号
     */

    public static void showDialog(FragmentActivity activity, int num, TextView textView, String title) {

        //1.根据设置项设置标题和内容、弹框样式
        String inputext = textView.getText().toString();

        boolean isAllNumber = false;//是否智能输入数字

        if (num == DataLogApDataParseUtil.DATA_INTERVAL) {
            isAllNumber = true;
        }

        boolean finalIsAllNumber = isAllNumber;
        ConfigInput configInput = params -> {
            params.padding = new int[]{5, 5, 5, 5};
            params.strokeColor = ContextCompat.getColor(activity, R.color.title_bg_white);
            if (finalIsAllNumber) {
                params.inputType = InputType.TYPE_CLASS_NUMBER;
            }
        };

        //2.弹出弹框
        CircleDialogUtils.showCustomInputDialog(activity, title, "",
                inputext, "", false, Gravity.CENTER, activity.getString(R.string.all_ok), new OnInputClickListener() {

                    @Override
                    public boolean onClick(String text, View v) {
                        if (TextUtils.isEmpty(text)) text = "";
                        switch (num) {
                            case DataLogApDataParseUtil.LOCAL_IP:
                            case DataLogApDataParseUtil.REMOTE_IP:
                            case DataLogApDataParseUtil.DEFAULT_GATEWAY:
                            case DataLogApDataParseUtil.SUBNET_MASK:
                                if (!TextUtils.isEmpty(text)) {
                                    boolean isboolIp = SmartHomeUtil.isboolIp(text);
                                    if (!isboolIp) {
                                        Toast.makeText(activity,R.string.m177输入格式不正确,Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                }
                                break;


                        }
                        textView.setText(text);
                        return true;
                    }
                }, activity.getString(R.string.all_no), configInput, null);
    }

}
