package com.tfc.utils;

public class MathHelper {
	//I know the name of this because of Minecraft modding.
	public static float lerp(float pct, float start, float end) {
		return ((start*(pct))+(end*(1-pct)));
	}
}
