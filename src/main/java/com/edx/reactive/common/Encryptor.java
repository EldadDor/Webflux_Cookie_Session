package com.edx.reactive.common;

public interface Encryptor {
    String encrypt(byte[] data);  // Returns Base64 encoded string

    byte[] decrypt(String data);  // Accepts Base64 encoded string
}
