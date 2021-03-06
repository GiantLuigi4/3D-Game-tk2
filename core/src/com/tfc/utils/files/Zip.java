package com.tfc.utils.files;

import com.tfc.utils.BiObject;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Zip {
	public static ZipOutStream createZipFile(File output) throws FileNotFoundException {
		FileOutputStream stream1 = new FileOutputStream(output);
		ZipOutputStream stream2 = new ZipOutputStream(stream1);
		return new ZipOutStream(stream2, null);
	}
	
	public static ZipOutStream createZipFile() {
		ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
		ZipOutputStream stream2 = new ZipOutputStream(stream1);
		return new ZipOutStream(stream2, stream1);
	}
	
	public static ZipOutStream addFile(ZipOutStream file, String name, String text) throws IOException {
		file.getObj1().putNextEntry(new ZipEntry(name));
		char[] chars = text.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) bytes[i] = (byte) chars[i];
		file.getObj1().write(bytes);
		file.getObj1().closeEntry();
		return file;
	}
	
	public static ZipOutStream addFile(ZipOutStream file, String name, char[] chars) throws IOException {
		file.getObj1().putNextEntry(new ZipEntry(name));
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) bytes[i] = (byte) chars[i];
		file.getObj1().write(bytes);
		file.getObj1().closeEntry();
		return file;
	}
	
	public static ZipOutStream addFile(ZipOutStream file, String name, byte[] bytes) throws IOException {
		file.getObj1().putNextEntry(new ZipEntry(name));
		file.getObj1().write(bytes);
		file.getObj1().closeEntry();
		return file;
	}
	
	public static byte[] finish(ZipOutStream file) throws IOException {
		file.getObj1().finish();
		byte[] bytes = new byte[0];
		if (file.getObj2() != null) bytes = file.getObj2().toByteArray();
		file.getObj1().close();
		return bytes;
	}
	
	public static String read(ZipFile file, String entry) throws IOException {
		InputStream stream = file.getInputStream(file.getEntry(entry));
		byte[] bytes = new byte[stream.available()];
		char[] chars = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) chars[i] = (char) bytes[i];
		return new String(chars);
	}
	
	public static class ZipOutStream extends BiObject<ZipOutputStream, ByteArrayOutputStream> {
		public ZipOutStream(ZipOutputStream obj1, ByteArrayOutputStream obj2) {
			super(obj1, obj2);
		}
		
		public ZipOutStream addFile(String name, String text) throws IOException {
			return Zip.addFile(this, name, text);
		}
		
		public ZipOutStream addFile(String name, char[] text) throws IOException {
			return Zip.addFile(this, name, text);
		}
		
		public ZipOutStream addFile(String name, byte[] text) throws IOException {
			return Zip.addFile(this, name, text);
		}
		
		public byte[] finish() throws IOException {
			return Zip.finish(this);
		}
	}
}
