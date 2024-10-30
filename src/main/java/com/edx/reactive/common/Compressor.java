package com.edx.reactive.common;

public interface Compressor {
    byte[] compress(byte[] data);

    byte[] decompress(byte[] data);
}
