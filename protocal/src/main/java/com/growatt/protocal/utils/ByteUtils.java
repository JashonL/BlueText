package com.growatt.protocal.utils;

import java.util.Arrays;

public class ByteUtils {
    /**
     * byte数组转16进制字符串
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv).append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * 16进制字符串转byte数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        if (hexString.length() == 1) {
            hexString = "0" + hexString;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * 16进制转byte
     */
    public static byte hexStringToByte(String hexString) {
        return hexStringToBytes(hexString)[0];
    }

    /**
     * char转byte
     */
    public static byte charToByte(char c) {
        return intToByte("0123456789ABCDEF".indexOf(c));
    }

    public static byte intToByte(int i) {
        return (byte) i;
    }

    /**
     * int转长度为2的字节数组
     */
    public static byte[] intTo2Byte(int i) {
        byte[] b = new byte[2];

        b[0] = (byte) (i >> 8);
        b[1] = (byte) (i);

        return b;
    }

    /**
     * int转长度为4的字节数组
     */
    public static byte[] intTo4Byte(int i) {
        byte[] bytes = new byte[4];
        // 拆分 32 位：每次右移 8 位，取低 8 位（& 0xFF 防止符号位扩展）
        bytes[0] = (byte) ((i >> 24) & 0xFF); // 最高位字节（第1字节）
        bytes[1] = (byte) ((i >> 16) & 0xFF); // 次高位字节（第2字节）
        bytes[2] = (byte) ((i >> 8) & 0xFF);  // 次低位字节（第3字节）
        bytes[3] = (byte) (i & 0xFF);         // 最低位字节（第4字节）
        return bytes;
    }

    /**
     * int转16进制字符串,得出来的字符串长度为2或者4，不够前面补0
     * 示例：
     * 11->0b
     * 19->13
     * 1600->0640
     */
    public static String intToHexString(int i) {
        String hexString = Integer.toHexString(i);//值不为空，并且值的长度大于0
        if (hexString.length() <= 4) {
            if (hexString.length() % 2 != 0) {
                return "0" + hexString;
            }
        } else {
            return "00";
        }
        return hexString;
    }

    /**
     * byte数组转String
     */
    public static String bytesToString(byte[] bytes) {

        if (bytes == null) return "";

        StringBuilder strBuilder = new StringBuilder();
        for (byte aByte : bytes) {
            if (aByte != 0) {
                strBuilder.append((char) aByte);
            } else {
                break;
            }

        }
        return strBuilder.toString();
    }

    /**
     * byte转无符号int
     */
    public static int byteToUnsignedInt(byte b) {
        return b & 0xff;
    }

    /**
     * 长度为2byte数组转无符号int
     */
    public static int convert2BytesToUnsignedInt(byte[] bs) {
        if (bs == null || bs.length != 2) {
            return 0;
        }
        return (bs[0] & 0xFF) << 8 | (bs[1] & 0xFF);
    }

    /**
     * 长度为4byte数组转无符号long
     */
    public static long convert4BytesToUnsignedLong(byte[] bs) {
        if (bs == null || bs.length != 4) {
            return 0;
        }
        return (long) (bs[0] & 0xFF) << 24 | (bs[1] & 0xFF) << 16 | (bs[2] & 0xFF) << 8 | (bs[3] & 0xFF);
    }

    /**
     * 长度为4byte数组转有符号int
     */
    public static int convert4BytesToSignedInt(byte[] bs) {
        if (bs == null || bs.length != 4) {
            return 0;
        }
        int b0 = bs[0] & 0xFF; // 最高位字节（第24-31位）
        int b1 = bs[1] & 0xFF; // 次高位字节（第16-23位）
        int b2 = bs[2] & 0xFF; // 次低位字节（第8-15位）
        int b3 = bs[3] & 0xFF; // 最低位字节（第0-7位）
        return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
    }

    /**
     * 2个字节转有符号int
     */
    public static int convert2BytesToSignedInt(byte[] bs) {
        if (bs == null || bs.length != 2) {
            return 0;
        }
        return (bs[0] << 8) | (bs[1] & 0xFF);
    }

    /**
     * 2字节数组中截取其中一部分转成无符号int
     * 包头包尾
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    public static int convert2BytesToUnSignedIntSub(byte[] bs, int start, int end) {
        if (bs == null || bs.length != 2) {
            return 0;
        }
        boolean[] bitArray = bytesToBitArray(bs);
        //需要反转，因为机器返回来的bit位0是从右边算
        bitArray = reverse(bitArray);
        int sum = 0;
        for (int i = start; i < end + 1; i++) {
            int bitValue = bitArray[i] ? 1 : 0;
            sum += bitValue * Math.pow(2, i - start);
        }

        return sum;
    }

    /**
     * 计算出给定byte中的每一位,并以一个布尔数组返回. true表示为1,false表示为0
     */
    public static boolean[] byteToBitArray(byte b) {
        boolean[] buff = new boolean[8];
        int index = 0;
        for (int i = 7; i >= 0; i--) {
            buff[index++] = ((b >>> i) & 1) == 1;
        }
        return buff;
    }

    /**
     * byte数组转为bit数组
     */
    public static boolean[] bytesToBitArray(byte[] bs) {
        if (bs == null || bs.length == 0) {
            return new boolean[]{};
        }
        boolean[] bitArray = new boolean[bs.length * 8];
        int destPos = 0;
        for (byte b : bs) {
            boolean[] src = byteToBitArray(b);
            System.arraycopy(src, 0, bitArray, destPos, 8);
            destPos += 8;
        }
        return bitArray;
    }

    /**
     * 数组反转
     */
    public static boolean[] reverse(boolean[] arrays) {
        int length = arrays.length;
        boolean[] newArrays = new boolean[arrays.length];
        int position = length;
        for (boolean array : arrays) {
            newArrays[position - 1] = array;
            position = position - 1;
        }
        return newArrays;
    }

    /**
     * 字节数组拼接
     */
    public static byte[] join(byte[]... byteArrays) {
        int totalLength = 0;
        for (byte[] bytes : byteArrays) {
            if (bytes != null) {
                totalLength += bytes.length;
            }
        }
        byte[] joinBytes = new byte[totalLength];
        int currentPos = 0;
        for (byte[] bytes : byteArrays) {
            if (bytes != null) {
                int bytesLength = bytes.length;
                if (bytesLength > 0) {
                    System.arraycopy(bytes, 0, joinBytes, currentPos, bytesLength);
                    currentPos += bytesLength;
                }
            }
        }
        return joinBytes;
    }

    public static byte[] stringToBytes(String value) {
        return value.getBytes();
    }

    public static byte[] createPKCS7Padding(int dataLength) {
        int paddingSize = 16 - (dataLength % 16);
        byte[] padding = new byte[paddingSize];
        Arrays.fill(padding, (byte) paddingSize);
        return padding;
    }

}
