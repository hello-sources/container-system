package com.image.fastcdc4j.internal.chunking;

import com.image.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import com.image.fastcdc4j.internal.util.Validations;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
public final class FixedSizeChunkerCore implements IterativeStreamChunkerCore {

    private final int chunkSize;


    public FixedSizeChunkerCore(final int chunkSize) {
        this.chunkSize = Validations.requirePositiveNonZero(chunkSize, "Chunk size");
    }

    @Override
    public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
        Objects.requireNonNull(stream);
        Validations.requirePositiveNonZero(size, "Size");
        Validations.requirePositive(currentOffset, "Current offset");
        Validations.require(currentOffset < size, "Current offset must be less than size");

        // Read up to CHUNK_SIZE many bytes
        //noinspection NumericCastThatLosesPrecision
        final int length = currentOffset + chunkSize <= size ? chunkSize : (int) (size - currentOffset);

        try {
            return stream.readNBytes(length);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}