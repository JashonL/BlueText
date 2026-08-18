package com.growatt.protocal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * 升级工具，分包等
 */
public class UpgradeUtil {

    // 默认分包大小：450字节
    public static final int DEFAULT_PACKET_SIZE = 450;
    // 默认编码：UTF-8
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * 通用字符串分包逻辑（支持指定分包大小和编码）
     * @param originalStr   原始字符串
     */
    public static byte[][] strToBytes(String originalStr, int maxPacketSize) {
        // 1. 参数校验
        if (originalStr == null || originalStr.isEmpty()) {
            return null; // 空字符串返回空数组
        }

        byte[] originalBytes = originalStr.getBytes(DEFAULT_CHARSET);
        if (originalBytes.length == 0) {
            return null;
        }

        List<byte[]> packets = new ArrayList<>();
        int totalLength = originalBytes.length;
        int startIndex = 0;

        while (startIndex < totalLength) {
            int endIndex = Math.min(startIndex + maxPacketSize, totalLength);
            byte[] packet = new byte[endIndex - startIndex];
            System.arraycopy(originalBytes, startIndex, packet, 0, packet.length);
            packets.add(packet);
            startIndex = endIndex;
        }
        return packets.toArray(new byte[0][]);
    }

    /**
     * 通用文件分包逻辑（支持指定分包大小）
     *
     * @param filePath   文件绝对/相对路径
     * @param packetSize 每个包的字节大小（必须>0）
     * @return 分包后的byte[][]
     */
    public static byte[][] fileToBytes(String filePath, int packetSize) throws IOException {
        // 1. 参数校验
        if (filePath == null || filePath.trim().isEmpty()) {
            //"文件路径不能为空"
            return null;
        }
        if (packetSize <= 0) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        if (!file.canRead()) {
            return null;
        }

        // 2. 空文件直接返回空数组
        if (file.length() == 0) {
            return null;
        }

        // 3. 逐块读取文件并分包
        List<byte[]> packets = new ArrayList<>();
        // 缓冲区：大小等于分包大小，避免频繁创建数组
        byte[] buffer = new byte[packetSize];
        try (FileInputStream fis = new FileInputStream(file)) {
            int readLen; // 每次实际读取的字节数
            // 循环读取：readLen=-1表示文件读取完毕
            while ((readLen = fis.read(buffer)) != -1) {
                // 若读取的字节数等于分包大小，直接复制缓冲区；否则截取有效部分
                byte[] packet = readLen == packetSize
                        ? buffer.clone()
                        : new byte[readLen];
                if (readLen != packetSize) {
                    System.arraycopy(buffer, 0, packet, 0, readLen);
                }
                packets.add(packet);
            }
        }

        // 4. List<byte[]> 转换为 byte[][]
        return packets.toArray(new byte[0][]);
    }

    public static byte[] crc32(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        long value = crc32.getValue();
        byte[] src = new byte[4];
        src[0] = (byte) ((value >> 24) & 0xFF);
        src[1] = (byte) ((value >> 16) & 0xFF);
        src[2] = (byte) ((value >> 8) & 0xFF);
        src[3] = (byte) (value & 0xFF);
        return src;
    }
}
