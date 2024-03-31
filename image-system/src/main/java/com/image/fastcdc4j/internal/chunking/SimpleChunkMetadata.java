package com.image.fastcdc4j.internal.chunking;

import com.image.fastcdc4j.external.chunking.ChunkMetadata;
import com.image.fastcdc4j.internal.util.Validations;
import com.image.fastcdc4j.external.chunking.Chunker;

import java.util.Objects;

/**
 * Implementation of a simple chunk metadata, wrapping given data.
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public final class SimpleChunkMetadata implements ChunkMetadata {

    private final byte[] hash;

    private final String hexHash;

    private final int length;

    private final long offset;


    public SimpleChunkMetadata(final long offset, final int length, final byte[] hash, final String hexHash) {
        Objects.requireNonNull(hash);
        Validations.require(hash.length > 0, "Hash must not be empty");
        Objects.requireNonNull(hexHash);
        Validations.require(!hexHash.isEmpty(), "Hex hash must not be empty");
        this.offset = Validations.requirePositive(offset, "Offset");
        this.length = Validations.requirePositiveNonZero(length, "Length");
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.hash = hash;
        this.hexHash = hexHash;
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
        return length;
    }

    @Override
    public long getOffset() {
        return offset;
    }
}