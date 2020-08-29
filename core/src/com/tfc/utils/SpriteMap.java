package com.tfc.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.tfc.registry.Textures;

import java.util.HashMap;

public class SpriteMap {
	private final HashMap<Location, Sprite> spriteHashMap = new HashMap<>();
	
	public Sprite getOrCreate(Location texture) {
		return new Sprite(Textures.get(texture));
	}
}
