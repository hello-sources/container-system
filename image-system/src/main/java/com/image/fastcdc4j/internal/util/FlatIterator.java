package com.image.fastcdc4j.internal.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public final class FlatIterator<X, Y> implements Iterator<Y> {

    private final Iterator<? extends X> outerIterator;

    private final Function<? super X, ? extends Iterator<Y>> provider;

    private Iterator<Y> currentInnerIter;


    public FlatIterator(final Iterator<? extends X> outerIterator,
        final Function<? super X, ? extends Iterator<Y>> provider) {
        this.outerIterator = Objects.requireNonNull(outerIterator);
        this.provider = Objects.requireNonNull(provider);
    }

    @Override
    public boolean hasNext() {
        while (true) {
            final boolean hasNext = currentInnerIter != null && currentInnerIter.hasNext();
            if (hasNext) {
                return true;
            }

            // Not set yet or exhausted
            if (!outerIterator.hasNext()) {
                return false;
            }

            currentInnerIter = provider.apply(outerIterator.next());
        }
    }

    @Override
    public Y next() {
        // hasNext also prepares currentInnerIter
        if (!hasNext()) {
            throw new NoSuchElementException("Attempting to get the next element but the iterator is out of elements");
        }

        return currentInnerIter.next();
    }
}