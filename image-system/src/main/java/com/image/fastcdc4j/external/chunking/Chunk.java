package com.image.fastcdc4j.external.chunking;


import com.image.fastcdc4j.internal.chunking.SimpleChunkMetadata;


public interface Chunk {

    byte[] getData();


    byte[] getHash();


    String getHexHash();


    int getLength();


    long getOffset();


    default ChunkMetadata toChunkMetadata() {
        return new SimpleChunkMetadata(getOffset(), getLength(), getHash(), getHexHash());
    }
}