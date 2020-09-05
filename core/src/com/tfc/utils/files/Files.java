package com.tfc.utils.files;

import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.utils.Logger;

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
			char[] chars = text.toCharArray();
			byte[] bytes = new byte[chars.length];
			for (int i = 0; i < chars.length; i++) bytes[i] = (byte) chars[i];
			writer.write(bytes);
			writer.close();
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	public static void createFile(String name, byte[] bytes) {
		String directory = ThreeDeeFirstPersonGame.dir + "\\" + name;
		try {
			File f = new File(directory);
			if (!f.exists()) {
				f.getParentFile().mkdirs();
				f.createNewFile();
			}
			FileOutputStream writer = new FileOutputStream(directory);
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
			for (int i = 0; i < bytes.length; i++) chars[i] = (char) bytes[i];
			stream.close();
			return new String(chars);
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
		return "";
	}
	
	public static byte[] readB(String name) {
		String directory = ThreeDeeFirstPersonGame.dir + "\\" + name;
		try {
			File f = new File(directory);
			FileInputStream stream = new FileInputStream(f);
			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);
			stream.close();
			return bytes;
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
		return new byte[0];
	}
}
