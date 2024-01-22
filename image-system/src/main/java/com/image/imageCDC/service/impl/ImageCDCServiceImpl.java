// package com.image.imageCDC.service.impl;
//
// import com.image.imageCDC.service.ImageCDCService;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Service;
//
// import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.nio.file.Paths;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
//
// /**
//  * ImageCDCServiceImpl
//  *
//  * @Author litianwei
//  * @Date 2024/1/12
//  **/
// @Slf4j
// @Service
// public class ImageCDCServiceImpl implements ImageCDCService {
//
//     private static final long POLY = 0xbfe6b8a5bf378d83L;
//     private static final long[] T = new long[256];
//     private static final long[] U = new long[256];
//     private static int shift;
//
//     // 关于CDC初始化的一些值
//     public static final int BLOCK_SIZE = 1024 * 1024; // 1 MB
//     public static final int BLOCK_MIN_SIZE = 1024 * 256; // 256 KB
//     public static final int BLOCK_MAX_SIZE = 1024 * 1024 * 4; // 4 MB
//     public static final int BLOCK_WIN_SIZE = 48;
//     public static final int NAME_MAX_SIZE = 4096;
//     public static final int BREAK_VALUE = 0x0013; // or 0x0513
//     public static final int READ_SIZE = 1024 * 4; // 4 KB
//     public static String byteToHex(int b) {
//         return (b >= 10) ? Character.toString((char)('a' + b - 10)) : Character.toString((char)('0' + b));
//     }
//
//     // 定义块大小范围，平均块大小，最大块大小，最小块大小
//     public static final int CDC_AVERAGE_BLOCK_SIZE = 1 << 23; // 8MB
//     public static final int CDC_MIN_BLOCK_SIZE = 6 * (1 << 20); // 6MB
//     public static final int CDC_MAX_BLOCK_SIZE = 10 * (1 << 20); // 10MB
//
//
//     // LongWrapper 类是一个简单的自定义辅助类，用于模拟C语言中通过指针传递值的效果
//     // 在Java中，基本数据类型（如 long）是按值传递的，而不是按引用传递。LongWrapper辅助类是为了在函数中修改长整型的值并保持这些更改
//     private static class LongWrapper {
//         long value;
//
//         public LongWrapper() {
//             this.value = 0;
//         }
//
//         public LongWrapper(long value) {
//             this.value = value;
//         }
//     }
//
//
//     // 初始化CDC分块结构
//     public static final int CHECKSUM_LENGTH = 20;
//
//     // 处理写入一个数据块
//     public static class WriteblockFunc {
//         private String outputDirectory; // 块文件的输出目录
//
//         int writeBlock(CDCDescriptor chunkDescriptor, byte[] checksum) {
//             String fileName = checksumToHexString(checksum) + ".blk";
//             String filePath = Paths.get(outputDirectory, fileName).toString();
//
//             try (FileOutputStream fos = new FileOutputStream(filePath)) {
//                 fos.write(chunkDescriptor.blockBuffer, 0, (int)chunkDescriptor.length);
//                 return 0; // 写入成功
//             } catch (IOException e) {
//                 System.err.println("Error writing block to file: " + e.getMessage());
//                 return -1; // 写入失败
//             }
//         }
//
//         private String checksumToHexString(byte[] checksum) {
//             StringBuilder hexString = new StringBuilder();
//             for (byte b : checksum) {
//                 String hex = Integer.toHexString(0xff & b);
//                 if (hex.length() == 1) {
//                     hexString.append('0');
//                 }
//                 hexString.append(hex);
//             }
//             return hexString.toString();
//         }
//     }
//
//     // CDC数据块特征，和处理方式
//     public static class CDCFileDescriptor {
//         public long blockMinSize; //最小块大小
//         public long blockMaxSize; //最大块大小
//         public long blockSize;    //块实际大小
//         public long fileSize;     //当前CDC处理了文件的大小
//
//         public long blockNumber;   //当前块数量
//         public byte[] blkSha1s;    //每个块对应的SHA1值,用数组保存
//         public int maxBlockNumber;    //最多的块数量
//         public byte[] fileSum = new byte[CHECKSUM_LENGTH];   //整个文件的SHA1值
//
//         public WriteblockFunc writeBlock;   //函数指针，用来写块
//     }
//
//     // 单个CDC数据块的描述信息
//     public static class CDCDescriptor {
//         public long offset;
//         public long length;
//         public byte[] checksum = new byte[CHECKSUM_LENGTH];
//         public byte[] blockBuffer;
//         public int result;
//     }
//
//
//
//
//
//
//     @Override
//     public String rawdataToHex(byte[] rawdata) {
//         StringBuilder hexStr = new StringBuilder();
//         for (byte b : rawdata) {
//             hexStr.append(String.format("%02x", b));
//         }
//         return hexStr.toString();
//     }
//
//     @Override
//     public int doWriteChunk(byte[] checksum, byte[] buf) {
//         String chksumStr = rawdataToHex(checksum);
//         File file = new File(chksumStr);
//
//         if (file.exists()) {
//             return 0;
//         }
//
//         try (FileOutputStream fos = new FileOutputStream(file)) {
//             fos.write(buf);
//         } catch (IOException e) {
//             System.out.println("Failed to write chunk " + chksumStr + ".");
//             return -1;
//         }
//         return 0;
//     }
//
//     @Override
//     public Long readn(InputStream in, byte[] buffer, long n) {
//         int nleft = (int) n;
//         int nread;
//         int totalRead = 0;
//         try {
//             while (nleft > 0) {
//                 nread = in.read(buffer, totalRead, nleft);
//                 if (nread < 0) {
//                     break; // EOF
//                 }
//                 nleft -= nread;
//                 totalRead += nread;
//             }
//         } catch (Exception exception) {
//             exception.printStackTrace();
//         }
//         return Long.valueOf(totalRead);
//     }
//
//     @Override
//     public Long rabinChecksum(byte[] buf) {
//         long sum = 0;
//         for (int i = 0; i < buf.length; ++i) {
//             sum = rabinRollingChecksum(sum, buf.length, (byte)0, buf[i]);
//         }
//         return sum;
//     }
//
//     @Override
//     public Long rabinRollingChecksum(long csum, int len, byte c1, byte c2) {
//         return append8(csum ^ U[Byte.toUnsignedInt(c1)], c2);
//     }
//
//     @Override
//     public void rabinInit(int len) {
//         calcT(POLY);
//         calcU(len);
//     }
//
//     private static int fls64(long v) {
//         if (v < 0) {
//             return 64;
//         }
//         int result = 0;
//         while (v != 0) {
//             v >>>= 1;
//             result++;
//         }
//         return result;
//     }
//
//     private static long polymod(long nh, long nl, long d) {
//         int k = fls64(d) - 1;
//         d <<= 63 - k;
//
//         if (nh != 0) {
//             if ((nh & Long.MIN_VALUE) != 0) {
//                 nh ^= d;
//             }
//             for (int i = 62; i >= 0; i--) {
//                 if ((nh & (1L << i)) != 0) {
//                     nh ^= d >> (63 - i);
//                     nl ^= d << (i + 1);
//                 }
//             }
//         }
//         for (int i = 63; i >= k; i--) {
//             if ((nl & (1L << i)) != 0) {
//                 nl ^= d >> (63 - i);
//             }
//         }
//         return nl;
//     }
//
//     private static void polymult(LongWrapper ph, LongWrapper pl, long x, long y) {
//         long h = 0, l = 0;
//         if ((x & 1) != 0) {
//             l = y;
//         }
//         for (int i = 1; i < 64; i++) {
//             if ((x & (1L << i)) != 0) {
//                 h ^= y >>> (64 - i);
//                 l ^= y << i;
//             }
//         }
//         ph.value = h;
//         pl.value = l;
//     }
//
//     private static long polymmult(long x, long y, long d) {
//         LongWrapper h = new LongWrapper();
//         LongWrapper l = new LongWrapper();
//         polymult(h, l, x, y);
//         return polymod(h.value, l.value, d);
//     }
//
//     private static long append8(long p, byte m) {
//         return ((p << 8) | (m & 0xFFL)) ^ T[(int)(p >>> shift)];
//     }
//
//     private static void calcT(long poly) {
//         int xshift = fls64(poly) - 1;
//         shift = xshift - 8;
//         long T1 = polymod(0, 1L << xshift, poly);
//         for (int j = 0; j < 256; j++) {
//             T[j] = polymmult(j, T1, poly) | ((long)j << xshift);
//         }
//     }
//
//     private static void calcU(int size) {
//         long sizeshift = 1;
//         for (int i = 1; i < size; i++) {
//             sizeshift = append8(sizeshift, (byte)0);
//         }
//         for (int i = 0; i < 256; i++) {
//             U[i] = polymmult(i, sizeshift, POLY);
//         }
//     }
//
//
//     /***********************    基于Rabin指纹CDC实现方法   ***************************/
//
//     public static int writeChunk(CDCDescriptor chunk, byte[] checksum) {
//         // 在Java中，可以使用MessageDigest来计算SHA1校验和
//         try {
//             MessageDigest md = MessageDigest.getInstance("SHA-1");
//             md.update(chunk.blockBuffer, 0, (int)chunk.length);
//             System.arraycopy(md.digest(), 0, checksum, 0, checksum.length);
//
//             // 需要实现 doWriteChunk 方法
//             return WriteblockFunc.doWriteChunk(checksum, chunk.blockBuffer, chunk.length);
//         } catch (NoSuchAlgorithmException e) {
//             e.printStackTrace();
//             return -1;
//         }
//     }
//
//     public static void initCdcFileDescriptor(long fileSize, CDCFileDescriptor fileDesc) {
//         // 设置文件大小、块计数和其他参数
//         fileDesc.fileSize = fileSize;
//         fileDesc.blockNumber = 0;
//
//         // 确保块大小有有效值
//         if (fileDesc.blockMinSize <= 0)
//             fileDesc.blockMinSize = BLOCK_MIN_SIZE;
//         if (fileDesc.blockMaxSize <= 0)
//             fileDesc.blockMaxSize = BLOCK_MAX_SIZE;
//         if (fileDesc.blockSize <= 0)
//             fileDesc.blockSize = BLOCK_SIZE;
//
//         // 预计算最大块数量
//         int maxBlockNumber = (int) ((fileSize + fileDesc.blockMinSize - 1) / fileDesc.blockMinSize);
//         fileDesc.blkSha1s = new byte[maxBlockNumber * CHECKSUM_LENGTH];
//         fileDesc.maxBlockNumber = maxBlockNumber;
//
//         // 设置写块函数
//         fileDesc.writeBlock = ImageCDCServiceImpl::writeChunk;
//     }
//
//
//     @Override
//     public int fileChunkCDC(InputStream inputStream, CDCFileDescriptor fileDesc) {
//         try {
//             // 初始化
//             MessageDigest mdFile = MessageDigest.getInstance("SHA-1");
//             byte[] buffer = new byte[BLOCK_MAX_SIZE];
//             int bytesRead;
//             long totalBytesRead = 0;
//             int fingerprint = 0;
//             int offset = 0;
//
//             while ((bytesRead = inputStream.read(buffer, offset, buffer.length - offset)) != -1) {
//                 totalBytesRead += bytesRead;
//                 offset += bytesRead;
//
//                 // 检查文件块，计算校验和等
//                 // TODO: 实现CDC逻辑
//
//                 if (offset >= fileDesc.blockMinSize) {
//                     // 处理块
//                     byte[] chunkChecksum = new byte[20];
//                     mdFile.update(buffer, 0, offset);
//                     chunkChecksum = mdFile.digest();
//
//                     // 写块
//                     int result = WriteblockFunc.writeBlock(chunkChecksum, buffer, offset);
//                     if (result < 0) {
//                         return -1; // 处理失败
//                     }
//                     offset = 0;
//                 }
//             }
//
//             if (offset > 0) {
//                 // 处理最后一个块
//                 byte[] chunkChecksum = new byte[20];
//                 mdFile.update(buffer, 0, offset);
//                 chunkChecksum = mdFile.digest();
//
//                 // 写块
//                 int result = WriteblockFunc::writeBlock(chunkChecksum, buffer, offset);
//                 if (result < 0) {
//                     return -1; // 处理失败
//                 }
//             }
//
//             // 设置文件整体的校验和
//             fileDesc.fileSum = mdFile.digest();
//
//             return 0; // 成功完成
//         } catch (NoSuchAlgorithmException e) {
//             System.err.println("SHA-1 algorithm not found: " + e.getMessage());
//             return -1;
//         } catch (IOException e) {
//             System.err.println("I/O error: " + e.getMessage());
//             return -1;
//         }
//     }
//
//     @Override
//     public int filenameChunkCDC(String filename, CDCFileDescriptor fileDesc) {
//         try {
//             FileInputStream fis = new FileInputStream(filename);
//             // 将文件输入流传递给 fileChunkCDC 方法
//             return fileChunkCDC(fis, fileDesc);
//         } catch (FileNotFoundException e) {
//             System.err.println("File not found: " + e.getMessage());
//             return -1;
//         } catch (IOException e) {
//             System.err.println("I/O error: " + e.getMessage());
//             return -1;
//         }
//     }
//
//     @Override
//     public void cdcInit() {
//
//
//
//     }
//
//     @Override
//     public void cdcWork(String filename) {
//         CDCFileDescriptor cdc = new CDCFileDescriptor();
//
//         // 设置块的参数
//         cdc.blockSize = CDC_AVERAGE_BLOCK_SIZE;
//         cdc.blockMinSize = CDC_MIN_BLOCK_SIZE;
//         cdc.blockMaxSize = CDC_MAX_BLOCK_SIZE;
//
//         // 设置写块函数
//         cdc.writeBlock = WriteblockFunc::writeChunk;
//
//         // 开始处理文件
//         filenameChunkCDC(filename, cdc);
//     }
//
//
// }
