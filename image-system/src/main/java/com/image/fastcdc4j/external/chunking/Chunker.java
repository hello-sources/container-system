package com.image.fastcdc4j.external.chunking;

import com.image.fastcdc4j.internal.util.FlatIterator;
import com.image.fastcdc4j.internal.util.Validations;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;


@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface Chunker {

    Iterable<Chunk> chunk(InputStream stream, long size);


    default Iterable<Chunk> chunk(final Stream<? extends Path> paths) {
        Objects.requireNonNull(paths);
        return () -> new FlatIterator<>(paths.filter(Files::isRegularFile)
            .iterator(), path -> chunk(path).iterator());
    }


    default Iterable<Chunk> chunk(final byte[] data) {
        Objects.requireNonNull(data);
        Validations.require(data.length > 0, "Data must not be empty");
        return chunk(new ByteArrayInputStream(data), data.length);
    }


    default Iterable<Chunk> chunk(final Path path) {
        Objects.requireNonNull(path);
        try {
            if (Files.isDirectory(path)) {
                return chunk(Files.walk(path));
            }
            if (Files.isRegularFile(path)) {
                return chunk(new BufferedInputStream(Files.newInputStream(path)), Files.size(path));
            }
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        throw new IllegalArgumentException("Only existing regular files or directories are supported");
    }
}