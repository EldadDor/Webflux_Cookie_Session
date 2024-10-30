package com.edx.reactive.config;

import com.edx.reactive.common.Compressor;
import com.edx.reactive.common.Encryptor;
import com.edx.reactive.utils.AesEncryptor;
import com.edx.reactive.utils.ZstdCompressor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class CookieConfiguration {
    @Value("${cookie.encryption.key}")
    private String secretKey;

    @Bean
    public Compressor compressor() {
        return new ZstdCompressor();
    }

    @Bean
    public Encryptor encryptor() {
        return new AesEncryptor(secretKey);
    }
}
