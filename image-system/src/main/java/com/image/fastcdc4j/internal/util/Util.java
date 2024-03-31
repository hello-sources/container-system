package com.image.fastcdc4j.internal.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Util
 * Collection of various utility methods of no particular topic.
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
public enum Util {
    ;

    private static final double FLOATING_DELTA = 1.0e-12;

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);


    @SuppressWarnings("MagicNumber")
    public static String bytesToHex(final byte[] bytes) {
        Objects.requireNonNull(bytes);
        // See https://stackoverflow.com/a/9855338/2411243
        //noinspection MultiplyOrDivideByPowerOfTwo
        final byte[] hexChars = new byte[bytes.length * 2];
        //noinspection ArrayLengthInLoopCondition
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            //noinspection MultiplyOrDivideByPowerOfTwo
            hexChars[j * 2] = Util.HEX_ARRAY[v >>> 4];
            //noinspection MultiplyOrDivideByPowerOfTwo
            hexChars[j * 2 + 1] = Util.HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }


    public static byte[] hash(final String method, final byte[] data) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(data);
        try {
            return MessageDigest.getInstance(method)
                .digest(data);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash method must be supported", e);
        }
    }


    public static int log2(final int x) {
        Validations.requirePositiveNonZero(x, "Value");
        // Safe binary-only conversion without floating points
        return Integer.bitCount(Integer.highestOneBit(x) - 1);
    }
}