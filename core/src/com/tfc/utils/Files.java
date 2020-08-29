package com.tfc.utils;

import com.tfc.ThreeDeeFirstPersonGame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Files {
	public static void createFile(String name, String text) {
		String directory = ThreeDeeFirstPersonGame.dir + "\\" + name;
		try {
			File f = new File(directory);
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			FileOutputStream writer = new FileOutputStream(directory);
			byte[] bytes = new byte[text.length()];
			char[] chars = text.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				bytes[i] = (byte) chars[i];
			}
			writer.write(bytes);
			writer.close();
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	public static String read(String name) {
		String directory = ThreeDeeFirstPersonGame.dir + "\\" + name;
		try {
			File f = new File(directory);
			FileInputStream stream = new FileInputStream(f);
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			char[] chars = new char[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				chars[i] = (char) bytes[i];
			}
			stream.close();
			return new String(chars);
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
		return "";
	}
}
