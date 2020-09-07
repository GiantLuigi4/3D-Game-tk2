package com.tfc.utils.awt;

import com.tfc.utils.BiObject;

public class AwtWrapper {
	
	private static final boolean isAwtPresent = detectAwt();
	private static final BiObject<Integer, Integer> defaultLoc = new BiObject<>(0, 0);
	
	private static boolean detectAwt() {
		try {
			Class.forName("java.awt.Robot");
			return true;
		} catch (Throwable err) {
			return false;
		}
	}
	
	public static void waitForIdle() {
		if (isAwtPresent) AwtAccessor.waitForIdle();
	}
	
	public static void mouseMove(int x, int y) {
		if (isAwtPresent) AwtAccessor.mouseMove(x, y);
	}
	
	public static void mouseOffset(int x, int y) {
		if (isAwtPresent) AwtAccessor.mouseOffset(x, y);
	}
	
	public static BiObject<Integer, Integer> getMouseLocation() {
		if (isAwtPresent) return AwtAccessor.getMouseLocation();
		return defaultLoc;
	}
}
