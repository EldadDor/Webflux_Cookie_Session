package com.edx.reactive.utils;

import com.edx.reactive.common.Compressor;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ZstdCompressor implements Compressor {
    private static final int COMPRESSION_LEVEL = 3;

    @Override
    public byte[] compress(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZstdOutputStream zstdOutputStream = new ZstdOutputStream(baos, COMPRESSION_LEVEL)) {

            zstdOutputStream.write(data);
            zstdOutputStream.close(); // Important to close before getting the bytes
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Compression failed", e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ZstdInputStream zstdInputStream = new ZstdInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = zstdInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Decompression failed", e);
        }
    }
}
