package com.image.fastcdc4j.internal.chunking;

import com.image.fastcdc4j.external.chunking.Chunker;
import com.image.fastcdc4j.external.chunking.MaskOption;
import com.image.fastcdc4j.internal.util.Util;
import com.image.fastcdc4j.internal.util.Validations;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for generating masks that are used by {@link Chunker}s.
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public final class MaskGenerator {

    private static final int MASK_SIZE_TOTAL_FAST_CDC = 48;


    private static long generateMaskFastCdc(final int effectiveBits, final long seed) {
        Validations.requirePositiveNonZero(effectiveBits, "Effective bits");

        // Shuffle a mask with 'effectiveBits' 1s and fill up the rest with '0'
        // The most significant bit has to be 1 always, hence we only shuffle the rest
        final List<Integer> maskBits = new ArrayList<>();
        int i = 0;
        while (i < effectiveBits - 1) {
            maskBits.add(1);
            i++;
        }
        while (i < MaskGenerator.MASK_SIZE_TOTAL_FAST_CDC - 1) {
            maskBits.add(0);
            i++;
        }
        Collections.shuffle(maskBits, new Random(seed));

        final String mask = Stream.concat(Stream.of(1), maskBits.stream())
            .map(Object::toString)
            .collect(Collectors.joining());

        return Long.parseLong(mask, 2);
    }


    private static long generateMaskNlfiedlerRust(final int effectiveBits) {
        Validations.requirePositiveNonZero(effectiveBits, "Effective bits");
        return Long.parseLong("1".repeat(effectiveBits), 2);
    }


    private static int getEffectiveBits(final int expectedChunkSize) {
        Validations.requirePositiveNonZero(expectedChunkSize, "Expected chunk size");
        return Util.log2(expectedChunkSize);
    }


    private final int expectedChunkSize;

    private final MaskOption maskOption;

    private final int normalizationLevel;

    private final long seed;


    public MaskGenerator(final MaskOption maskOption, final int normalizationLevel, final int expectedChunkSize,
        final long seed) {
        this.maskOption = Objects.requireNonNull(maskOption);
        this.normalizationLevel = Validations.requirePositive(normalizationLevel, "Normalization level");
        this.expectedChunkSize = Validations.requirePositiveNonZero(expectedChunkSize, "Expected chunk size");
        this.seed = seed;
    }


    public long generateLargeMask() {
        return generateMask(-normalizationLevel);
    }


    public long generateSmallMask() {
        return generateMask(normalizationLevel);
    }


    private long generateMask(final int effectiveBitOffset) {
        final int effectiveBits = MaskGenerator.getEffectiveBits(expectedChunkSize) + effectiveBitOffset;

        if (maskOption.toString().equals(MaskOption.FAST_CDC.toString())) {
            return MaskGenerator.generateMaskFastCdc(effectiveBits, seed);
        } else {
            return MaskGenerator.generateMaskNlfiedlerRust(effectiveBits);
        }

        // 原始一些错误的语句
        // return switch (maskOption) {
        //     case FAST_CDC -> MaskGenerator.generateMaskFastCdc(effectiveBits, seed);
        //     case NLFIEDLER_RUST -> MaskGenerator.generateMaskNlfiedlerRust(effectiveBits);
        // };
    }
}