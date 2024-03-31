package com.image.fastcdc4j.external.chunking;

import com.image.fastcdc4j.internal.util.Validations;
import com.image.fastcdc4j.internal.chunking.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;


@SuppressWarnings("ClassWithTooManyFields")
public final class ChunkerBuilder {

    @SuppressWarnings("MultiplyOrDivideByPowerOfTwo")
    private static final int DEFAULT_EXPECTED_CHUNK_SIZE = 8 * 1_024;

    private static final String DEFAULT_HASH_METHOD = "SHA-1";

    private static final long DEFAULT_MASK_GENERATION_SEED = 941_568_351L;

    private static final double DEFAULT_MAX_SIZE_FACTOR = 8;

    private static final double DEFAULT_MIN_SIZE_FACTOR = 0.25;

    private static final int DEFAULT_NORMALIZATION_LEVEL = 2;

    private Chunker chunker;

    private IterativeStreamChunkerCore chunkerCore;

    private ChunkerOption chunkerOption = ChunkerOption.FAST_CDC;

    private int expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;

    private String hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;

    private long[] hashTable;

    private HashTableOption hashTableOption = HashTableOption.RTPAL;

    private long maskGenerationSeed = ChunkerBuilder.DEFAULT_MASK_GENERATION_SEED;

    private Long maskLarge;

    private MaskOption maskOption = MaskOption.FAST_CDC;

    private Long maskSmall;

    private double maximalChunkSizeFactor = ChunkerBuilder.DEFAULT_MAX_SIZE_FACTOR;

    private double minimalChunkSizeFactor = ChunkerBuilder.DEFAULT_MIN_SIZE_FACTOR;

    private int normalizationLevel = ChunkerBuilder.DEFAULT_NORMALIZATION_LEVEL;


    public Chunker build() {
        // TODO Maybe add Adler and Rabin CDC alternatives
        if (chunker != null) {
            return chunker;
        }

        // 原始一些出bug的代码
        // final long[] hashTableToUse = hashTable != null ? hashTable : switch (hashTableOption) {
        //     case RTPAL -> HashTables.getRtpal();
        //     case NLFIEDLER_RUST -> HashTables.getNlfiedlerRust();
        // };

        final long[] hashTableToUse;
        if (hashTable != null) hashTableToUse = hashTable;
        else {
            if (hashTableOption.toString().equals(HashTableOption.RTPAL.toString())) {
                hashTableToUse = HashTables.getRtpal();
            } else {
                hashTableToUse = HashTables.getNlfiedlerRust();
            }
        }

        final MaskGenerator maskGenerator =
            new MaskGenerator(maskOption, normalizationLevel, expectedChunkSize, maskGenerationSeed);
        final long maskSmallToUse = maskSmall != null ? maskSmall : maskGenerator.generateSmallMask();
        final long maskLargeToUse = maskLarge != null ? maskLarge : maskGenerator.generateLargeMask();

        //noinspection NumericCastThatLosesPrecision
        final int minimalChunkSize = (int) (expectedChunkSize * minimalChunkSizeFactor);
        //noinspection NumericCastThatLosesPrecision
        final int maximalChunkSize = (int) (expectedChunkSize * maximalChunkSizeFactor);

        // 原先出现bug的一些代码
        // final IterativeStreamChunkerCore coreToUse = chunkerCore != null ? chunkerCore : switch (chunkerOption) {
        //     case FAST_CDC -> new FastCdcChunkerCore(expectedChunkSize, minimalChunkSize, maximalChunkSize,
        //         hashTableToUse, maskSmallToUse, maskLargeToUse);
        //     case NLFIEDLER_RUST -> new NlfiedlerRustChunkerCore(expectedChunkSize, minimalChunkSize, maximalChunkSize,
        //         hashTableToUse, maskSmallToUse, maskLargeToUse);
        //     case FIXED_SIZE_CHUNKING -> new FixedSizeChunkerCore(expectedChunkSize);
        // };

        final IterativeStreamChunkerCore coreToUse;
        if (chunkerCore != null) coreToUse = chunkerCore;
        else {
            if (chunkerOption.toString().equals(ChunkerOption.FAST_CDC.toString())) {
                coreToUse = new FastCdcChunkerCore(expectedChunkSize, minimalChunkSize, maximalChunkSize,
                    hashTableToUse, maskSmallToUse, maskLargeToUse);
            } else if (chunkerOption.toString().equals(ChunkerOption.NLFIEDLER_RUST.toString())) {
                coreToUse = new NlfiedlerRustChunkerCore(expectedChunkSize, minimalChunkSize, maximalChunkSize,
                    hashTableToUse, maskSmallToUse, maskLargeToUse);
            } else {
                coreToUse = new FixedSizeChunkerCore(expectedChunkSize);
            }
        }
        return new IterativeStreamChunker(coreToUse, hashMethod);
    }


    public ChunkerBuilder fastCdc() {
        chunkerOption = ChunkerOption.FAST_CDC;
        hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
        expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
        minimalChunkSizeFactor = ChunkerBuilder.DEFAULT_MIN_SIZE_FACTOR;
        maximalChunkSizeFactor = ChunkerBuilder.DEFAULT_MAX_SIZE_FACTOR;
        hashTableOption = HashTableOption.RTPAL;
        normalizationLevel = 2;
        maskOption = MaskOption.FAST_CDC;
        maskGenerationSeed = ChunkerBuilder.DEFAULT_MASK_GENERATION_SEED;
        return this;
    }

    public ChunkerBuilder fsc() {
        chunkerOption = ChunkerOption.FIXED_SIZE_CHUNKING;
        hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
        expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
        minimalChunkSizeFactor = ChunkerBuilder.DEFAULT_MIN_SIZE_FACTOR;
        maximalChunkSizeFactor = ChunkerBuilder.DEFAULT_MAX_SIZE_FACTOR;
        return this;
    }


    public ChunkerBuilder nlFiedlerRust() {
        chunkerOption = ChunkerOption.NLFIEDLER_RUST;
        hashMethod = ChunkerBuilder.DEFAULT_HASH_METHOD;
        expectedChunkSize = ChunkerBuilder.DEFAULT_EXPECTED_CHUNK_SIZE;
        minimalChunkSizeFactor = ChunkerBuilder.DEFAULT_MIN_SIZE_FACTOR;
        maximalChunkSizeFactor = ChunkerBuilder.DEFAULT_MAX_SIZE_FACTOR;
        hashTableOption = HashTableOption.NLFIEDLER_RUST;
        normalizationLevel = 1;
        maskOption = MaskOption.NLFIEDLER_RUST;
        return this;
    }


    public ChunkerBuilder setChunker(final Chunker chunker) {
        this.chunker = Objects.requireNonNull(chunker);
        return this;
    }


    public ChunkerBuilder setChunkerCore(final IterativeStreamChunkerCore chunkerCore) {
        this.chunkerCore = Objects.requireNonNull(chunkerCore);
        return this;
    }


    public ChunkerBuilder setChunkerOption(final ChunkerOption chunkerOption) {
        this.chunkerOption = Objects.requireNonNull(chunkerOption);
        return this;
    }


    public ChunkerBuilder setExpectedChunkSize(final int expectedChunkSize) {
        this.expectedChunkSize = Validations.requirePositiveNonZero(expectedChunkSize, "Expected chunk size");
        return this;
    }


    public ChunkerBuilder setHashMethod(final String hashMethod) {
        Objects.requireNonNull(hashMethod);
        try {
            MessageDigest.getInstance(hashMethod);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("The given hash method is not supported, was: " + hashMethod, e);
        }
        this.hashMethod = hashMethod;
        return this;
    }


    public ChunkerBuilder setHashTable(final long[] hashTable) {
        Objects.requireNonNull(hashTable);
        //noinspection MagicNumber
        Validations.require(hashTable.length == 256,
            "Hash table must have a length of 256, one hash per byte value, was: " + hashTable.length);
        this.hashTable = hashTable.clone();
        return this;
    }


    public ChunkerBuilder setHashTableOption(final HashTableOption hashTableOption) {
        this.hashTableOption = Objects.requireNonNull(hashTableOption);
        return this;
    }


    public ChunkerBuilder setMaskGenerationSeed(final long maskGenerationSeed) {
        this.maskGenerationSeed = maskGenerationSeed;
        return this;
    }


    public ChunkerBuilder setMaskLarge(final long maskLarge) {
        this.maskLarge = maskLarge;
        return this;
    }


    public ChunkerBuilder setMaskOption(final MaskOption maskOption) {
        this.maskOption = Objects.requireNonNull(maskOption);
        return this;
    }


    public ChunkerBuilder setMaskSmall(final long maskSmall) {
        this.maskSmall = maskSmall;
        return this;
    }


    public ChunkerBuilder setMaximalChunkSizeFactor(final double maximalChunkSizeFactor) {
        Validations.require(maximalChunkSizeFactor >= 1.0, "Maximal chunk size factor must be greater equals 1.0");
        this.maximalChunkSizeFactor = maximalChunkSizeFactor;
        return this;
    }


    public ChunkerBuilder setMinimalChunkSizeFactor(final double minimalChunkSizeFactor) {
        Validations.require(minimalChunkSizeFactor <= 1.0, "Minimal chunk size factor must be smaller equals 1.0");
        this.minimalChunkSizeFactor = minimalChunkSizeFactor;
        return this;
    }


    public ChunkerBuilder setNormalizationLevel(final int normalizationLevel) {
        this.normalizationLevel = Validations.requirePositive(normalizationLevel, "Normalization level");
        return this;
    }
}