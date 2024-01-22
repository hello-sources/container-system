package com.image.fastcdc4j.external.chunking;

/**
 * Available predefined algorithms to generate masks used by certain {@link Chunker}s.
 *
 * @Author litianwei
 * @Date 2024/1/22
 */
public enum MaskOption {
    /**
     * The mask layout used in the original FastCDC algorithm.
     */
    FAST_CDC,
    /**
     * The mask layout used in the  modified FastCDC algorithm by Nathan Fiedlers Rust implementation.
     */
    NLFIEDLER_RUST
}