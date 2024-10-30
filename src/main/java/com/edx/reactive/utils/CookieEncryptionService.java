package com.edx.reactive.utils;

import com.edx.reactive.common.Compressor;
import com.edx.reactive.common.Encryptor;
import org.springframework.stereotype.Service;

@Service
public class CookieEncryptionService {
    private final Encryptor encryptor;
    private final Compressor compressor;

    public CookieEncryptionService(Encryptor encryptor, Compressor compressor) {
        this.encryptor = encryptor;
        this.compressor = compressor;
    }

    public String encryptAndCompress(String data) {
        try {
            byte[] compressed = compressor.compress(data.getBytes());
            return encryptor.encrypt(compressed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt and compress data", e);
        }
    }

    public String decompressAndDecrypt(String data) {
        try {
            byte[] decrypted = encryptor.decrypt(data);
            byte[] decompressed = compressor.decompress(decrypted);
            return new String(decompressed);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt and decompress data", e);
        }
    }
}
