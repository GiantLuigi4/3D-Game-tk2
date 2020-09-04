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
	
	public static byte[] readBytes(GZIPInputStream stream) throws IOException {
		byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		return bytes;
	}
}
