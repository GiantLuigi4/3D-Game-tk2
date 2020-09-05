package com.tfc.utils.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZip {
	public static byte[] gZip(byte[] bytes) throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		GZIPOutputStream stream2 = new GZIPOutputStream(stream1);
		stream2.write(bytes);
		stream2.finish();
		byte[] bytes1 = stream1.toByteArray();
		stream2.close();
		return bytes1;
	}
	
	public static byte[] gZip(String text) throws IOException {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		GZIPOutputStream stream2 = new GZIPOutputStream(stream1);
		char[] chars = text.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) bytes[i] = (byte) chars[i];
		stream2.write(bytes);
		stream2.finish();
		byte[] bytes1 = stream1.toByteArray();
		stream2.close();
		return bytes1;
	}
	
	public static byte[] readBytes(GZIPInputStream stream) throws IOException {
		byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		stream.close();
		return bytes;
	}
	
	public static String readString(GZIPInputStream stream) throws IOException {
		byte[] bytes = new byte[1024];
		StringBuilder builder = new StringBuilder();
		//https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
		int len = 0;
		while ((len = stream.read(bytes)) != -1)
			for (int i = 0; i < len; i++)
				builder.append((char) bytes[i]);
		stream.read(bytes);
		stream.close();
		return builder.toString();
	}
}
