package com.image.fastcdc4j.internal.chunking;

import  com.image.fastcdc4j.external.chunking.IterativeStreamChunkerCore;
import  com.image.fastcdc4j.internal.util.Validations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * @Author litianwei
 * @Date 2024/1/22
 */
public final class NlfiedlerRustChunkerCore implements IterativeStreamChunkerCore {

    private final int expectedSize;

    private final long[] gear;

    private final long maskLarge;

    private final long maskSmall;

    private final int maxSize;

    private final int minSize;


    @SuppressWarnings("ConstructorWithTooManyParameters")
    public NlfiedlerRustChunkerCore(final int expectedSize, final int minSize, final int maxSize, final long[] gear,
        final long maskSmall, final long maskLarge) {
        Validations.require(minSize <= expectedSize, "Min size must be less equals expected size");
        Validations.require(maxSize >= expectedSize, "Max size must be greater equals expected size");
        Objects.requireNonNull(gear);
        //noinspection MagicNumber
        Validations.require(gear.length == 256,
            "Gear must have a length of 256, one hash per byte value, was: " + gear.length);

        this.expectedSize = Validations.requirePositive(expectedSize, "Expected size");
        this.minSize = Validations.requirePositive(minSize, "Min size");
        this.maxSize = Validations.requirePositive(maxSize, "Max size");
        this.gear = gear.clone();
        this.maskSmall = maskSmall;
        this.maskLarge = maskLarge;
    }

    @Override
    public byte[] readNextChunk(final InputStream stream, final long size, final long currentOffset) {
        Objects.requireNonNull(stream);
        Validations.requirePositiveNonZero(size, "Size");
        Validations.requirePositive(currentOffset, "Current offset");
        Validations.require(currentOffset < size, "Current offset must be less than size");

        try (final ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream()) {
            int normalSize = expectedSize;
            //noinspection StandardVariableNames
            long n = size - currentOffset;
            if (n <= 0) {
                throw new IllegalArgumentException(
                    "Attempting to read the next chunk but out of available bytes, as indicated by size");
            }
            if (n <= minSize) {
                return stream.readNBytes((int) n);
            }
            if (n >= maxSize) {
                n = maxSize;
            } else if (n <= normalSize) {
                normalSize = (int) n;
            }

            long fingerprint = 0;
            int i = minSize;
            dataBuffer.write(stream.readNBytes(i));

            //noinspection ForLoopWithMissingComponent
            for (; i < normalSize; i++) {
                final int data = stream.read();
                if (data == -1) {
                    throw new IllegalStateException(
                        "Attempting to read a byte from the stream but the stream has ended");
                }
                dataBuffer.write(data);
                fingerprint = (fingerprint >> 1) + gear[data];
                if ((fingerprint & maskSmall) == 0) {
                    return dataBuffer.toByteArray();
                }
            }
            //noinspection ForLoopWithMissingComponent
            for (; i < n; i++) {
                final int data = stream.read();
                if (data == -1) {
                    throw new IllegalStateException(
                        "Attempting to read a byte from the stream but the stream has ended");
                }
                dataBuffer.write(data);
                fingerprint = (fingerprint >> 1) + gear[data];
                if ((fingerprint & maskLarge) == 0) {
                    return dataBuffer.toByteArray();
                }
            }

            return dataBuffer.toByteArray();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}