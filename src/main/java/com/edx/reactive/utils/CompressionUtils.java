package com.edx.reactive.utils;

import org.springframework.web.server.ServerErrorException;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class CompressionUtils {

	public static String decompressBase64(String compressedBase64) {
		try {
			byte[] compressedData = Base64.getDecoder().decode(compressedBase64);
			Inflater inflater = new Inflater();
			inflater.setInput(compressedData);

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
			byte[] buffer = new byte[1024];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
			inflater.end();

			return outputStream.toString("UTF-8");
		} catch (Exception e) {
			throw new ServerErrorException("Error decompressing data error" + e.getMessage(), e);
		}
	}

	public static String compressBase64(String data) {
		try {
			byte[] input = data.getBytes("UTF-8");
			Deflater deflater = new Deflater();
			deflater.setInput(input);
			deflater.finish();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
			byte[] buffer = new byte[1024];
			while (!deflater.finished()) {
				int count = deflater.deflate(buffer);
				outputStream.write(buffer, 0, count);
			}
			outputStream.close();
			byte[] output = outputStream.toByteArray();

			return Base64.getEncoder().encodeToString(output);
		} catch (Exception e) {
			throw new ServerErrorException("Error decompressing data error" + e.getMessage(), e);
		}
	}
}
