package com.image.fastcdc4j.external.chunking;

import java.io.InputStream;

/**
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
@FunctionalInterface
public interface IterativeStreamChunkerCore {

    byte[] readNextChunk(InputStream stream, long size, long currentOffset);
}