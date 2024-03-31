package com.image.fastcdc4j.internal.chunking;

import com.image.fastcdc4j.external.chunking.Chunk;
import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import com.image.fastcdc4j.internal.util.Util;
import com.image.fastcdc4j.internal.util.Validations;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
public final class IterativeStreamChunker implements Chunker {

    private final IterativeStreamChunkerCore core;

    private final String hashMethod;


    public IterativeStreamChunker(final IterativeStreamChunkerCore core, final String hashMethod) {
        Objects.requireNonNull(hashMethod);
        try {
            MessageDigest.getInstance(hashMethod);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The given hash method is not supported, was: " + hashMethod, e);
        }

        this.core = Objects.requireNonNull(core);
        this.hashMethod = hashMethod;
    }

    @Override
    public Iterable<Chunk> chunk(final InputStream stream, final long size) {
        Objects.requireNonNull(stream);
        Validations.requirePositiveNonZero(size, "Size");
        return () -> new ChunkerIterator(stream, size, core, hashMethod);
    }


    private static final class ChunkerIterator implements Iterator<Chunk> {

        private final IterativeStreamChunkerCore core;

        private final String hashMethod;

        private final long size;

        private final InputStream stream;

        private long currentOffset;


        private ChunkerIterator(final InputStream stream, final long size, final IterativeStreamChunkerCore core,
            final String hashMethod) {
            Objects.requireNonNull(hashMethod);
            try {
                MessageDigest.getInstance(hashMethod);
            } catch (final NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("The given hash method is not supported, was: " + hashMethod, e);
            }

            this.stream = Objects.requireNonNull(stream);
            this.size = Validations.requirePositiveNonZero(size, "Size");
            this.core = Objects.requireNonNull(core);
            this.hashMethod = hashMethod;
        }

        @Override
        public boolean hasNext() {
            return currentOffset < size;
        }

        @Override
        public Chunk next() {
            if (!hasNext()) {
                throw new NoSuchElementException("The data stream has ended, can not generate another chunk");
            }

            final byte[] data = core.readNextChunk(stream, size, currentOffset);

            final Chunk chunk = new SimpleChunk(data, currentOffset, Util.hash(hashMethod, data));

            currentOffset += data.length;
            return chunk;
        }
    }
}