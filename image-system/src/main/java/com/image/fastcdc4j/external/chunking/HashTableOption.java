package com.image.fastcdc4j.external.chunking;

/**
 * Available predefined hash tables used by chunker algorithms.
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public enum HashTableOption {
    /**
     * Table used by RTPal.
     */
    RTPAL,
    /**
     * Table used by the modified FastCDC algorithm by Nathan Fiedlers Rust implementation.
     */
    NLFIEDLER_RUST
}