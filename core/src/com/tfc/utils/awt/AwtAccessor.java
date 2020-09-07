package com.tfc.utils.awt;

import com.tfc.utils.BiObject;

import java.awt.*;

/**
 * Minecraft modding taught me that if I use a class to access another class if the second class is present, I can prevent class loading issues
 */
public class AwtAccessor {
	private static final Robot r = genRobot();
	
	private static Robot genRobot() {
		try {
			return new Robot();
		} catch (Throwable err) {
			throw new RuntimeException(err);
		}
	}
	
	protected static void waitForIdle() {
		r.waitForIdle();
	}
	
	protected static void mouseMove(int x, int y) {
		r.mouseMove(x, y);
	}
	
	protected static void mouseOffset(int x, int y) {
		Point location = MouseInfo.getPointerInfo().getLocation();
		r.mouseMove(location.x + x, location.y + y);
	}
	
	protected static BiObject<Integer, Integer> getMouseLocation() {
		Point location = MouseInfo.getPointerInfo().getLocation();
		return new BiObject<>(location.x, location.y);
	}
}
