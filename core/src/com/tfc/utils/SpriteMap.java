package com.tfc.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.tfc.registry.Textures;

import java.util.HashMap;

public class SpriteMap {
	private static final HashMap<Location, Sprite> spriteHashMap = new HashMap<>();
	
	private SpriteMap() {
	}
	
	public static Sprite getOrCreate(Location texture) {
		if (spriteHashMap.containsKey(texture)) {
			return spriteHashMap.get(texture);
		} else {
			spriteHashMap.put(texture, new Sprite(Textures.get(texture)));
			return spriteHashMap.get(texture);
		}
	}
}
