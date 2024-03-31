package com.image.fastcdc4j.external.chunking;

/**
 * @Author litianwei
 * @Date 2024/1/22
 */
public interface ChunkMetadata {

    byte[] getHash();


    String getHexHash();


    int getLength();


    long getOffset();
}
