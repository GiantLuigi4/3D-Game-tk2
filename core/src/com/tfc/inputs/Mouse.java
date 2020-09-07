package com.tfc.inputs;

import com.tfc.ThreeDeeFirstPersonGame;

public class Mouse {
	public static int getMouseX() {
		return ThreeDeeFirstPersonGame.getInstance().getMouseX();
	}
	
	public static int getMouseY() {
		return ThreeDeeFirstPersonGame.getInstance().getMouseY();
	}
	
	public static boolean isPressed(int button) {
		switch (button) {
			case 0:
				return ThreeDeeFirstPersonGame.getInstance().isLeftDown();
			case 2:
				return ThreeDeeFirstPersonGame.getInstance().isRightDown();
			default:
				return false;
		}
	}
	
	public static void release(int button) {
		switch (button) {
			case 0:
				ThreeDeeFirstPersonGame.getInstance().setRightDown(false);
				return;
			case 2:
				ThreeDeeFirstPersonGame.getInstance().setLeftDown(false);
				return;
			default:
				break;
		}
	}
}
