package com.image.fastcdc4j.internal.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Validations
 * Utility class providing validation methods.
 *
 * @Author litianwei
 * @Date 2024/1/22
 **/
public final class Validations {

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public static long requirePositive(final long value, final String valueName) {
        Objects.requireNonNull(valueName);
        Validations.require(value >= 0, valueName + " must be positive.");
        return value;
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public static long requirePositiveNonZero(final long value, final String valueName) {
        Objects.requireNonNull(valueName);
        Validations.require(value > 0, valueName + " must be positive and not zero.");
        return value;
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public static int requirePositive(final int value, final String valueName) {
        Objects.requireNonNull(valueName);
        Validations.require(value >= 0, valueName + " must be positive.");
        return value;
    }

    @SuppressWarnings("OverloadedMethodsWithSameNumberOfParameters")
    public static int requirePositiveNonZero(final int value, final String valueName) {
        Objects.requireNonNull(valueName);
        Validations.require(value > 0, valueName + " must be positive and not zero.");
        return value;
    }

    public static <E extends RuntimeException> void requireNotThrow(final Class<E> expectedException,
        final Runnable runnable, final String message) {
        Objects.requireNonNull(expectedException);
        Objects.requireNonNull(runnable);
        Objects.requireNonNull(message);
        try {
            runnable.run();
        } catch (final RuntimeException e) {
            if (expectedException.isInstance(e)) {
                throw new IllegalArgumentException(message, e);
            }
        }
    }

    public static <T> T require(final Predicate<? super T> predicate, final T object, final String message) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(message);
        Validations.require(predicate.test(object), message);
        return object;
    }

    @SuppressWarnings("BooleanParameter")
    public static void require(final boolean predicate, final String message) {
        Objects.requireNonNull(message);
        Validations.require(predicate, IllegalArgumentException::new, message);
    }

    public static <T, E extends RuntimeException> T require(final Predicate<? super T> predicate, final T object,
        final Function<? super String, E> exceptionSupplier, final String message) {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(exceptionSupplier);
        Objects.requireNonNull(message);
        Validations.require(predicate.test(object), exceptionSupplier, message);
        return object;
    }

    @SuppressWarnings({ "BooleanParameter", "WeakerAccess" })
    public static <E extends RuntimeException> void require(final boolean predicate,
        final Function<? super String, E> exceptionSupplier, final String message) {
        Objects.requireNonNull(exceptionSupplier);
        Objects.requireNonNull(message);
        if (predicate) {
            return;
        }
        throw exceptionSupplier.apply(message);
    }

    private Validations() {
        throw new UnsupportedOperationException("Utility class, no implementation");
    }
}