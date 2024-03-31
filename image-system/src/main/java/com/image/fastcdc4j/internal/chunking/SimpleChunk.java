package com.image.fastcdc4j.internal.chunking;


import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.internal.util.Util;
import com.image.fastcdc4j.internal.util.Validations;
import com.image.fastcdc4j.external.chunking.Chunker;

import java.util.Objects;

/**
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public final class SimpleChunk implements Chunk {

    private final byte[] data;

    private final byte[] hash;

    private final String hexHash;

    private final long offset;


    public SimpleChunk(final byte[] data, final long offset, final byte[] hash) {
        Objects.requireNonNull(data);
        Validations.require(data.length > 0, "Data must not be empty");
        Objects.requireNonNull(hash);
        Validations.require(hash.length > 0, "Hash must not be empty");
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.data = data;
        this.offset = Validations.requirePositive(offset, "Offset");
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.hash = hash;
        hexHash = Util.bytesToHex(hash);
    }

    @Override
    public byte[] getData() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return data;
    }

    @Override
    public byte[] getHash() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return hash;
    }

    @Override
    public String getHexHash() {
        return hexHash;
    }

    @Override
    public int getLength() {
        return data.length;
    }

    @Override
    public long getOffset() {
        return offset;
    }
}