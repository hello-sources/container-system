package com.image.imageCDC.service;

import com.image.imageCDC.service.impl.ImageCDCServiceImpl.CDCFileDescriptor;

import java.io.InputStream;

public interface ImageCDCService {

    /***********************    一些工具方法    ***************************/

    /**
     * 将rawdata转换为十六进制字符串
     **/
    String rawdataToHex(byte[] rawdata);

    /**
     * 处理数据块的写入
     **/
    int doWriteChunk(byte[] checksum, byte[] buf);

    /**
     * 从InputStream读取数据
     **/
    Long readn(InputStream in, byte[] buffer, long n);

    /***********************    Rabin滚动校验和    ***************************/

    /**
     * 实现 Rabin 校验和算法
     **/
    Long rabinChecksum(byte[] buf);

    /**
     * 实现 Rabin 滚动校验和算法
     **/
    Long rabinRollingChecksum(long csum, int len, byte c1, byte c2);

    /**
     * 初始化 Rabin 校验和算法
     **/
    void rabinInit(int len);

    /***********************    基于Rabin指纹CDC相关接口    ***************************/

    int fileChunkCDC(InputStream inputStream, CDCFileDescriptor fileDescriptor);

    int filenameChunkCDC(String filename, CDCFileDescriptor fileDescriptor);

    void cdcInit();

    void cdcWork(String filename);



}
