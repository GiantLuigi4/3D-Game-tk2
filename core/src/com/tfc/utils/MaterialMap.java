package com.tfc.utils;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.tfc.registry.Textures;

import java.util.HashMap;

public class MaterialMap {
	private static final HashMap<Location, Material> materialHashMap = new HashMap<>();
	
	private MaterialMap() {
	}
	
	public static Material getOrCreate(Location texture) {
		if (materialHashMap.containsKey(texture)) {
			return materialHashMap.get(texture);
		} else {
			materialHashMap.put(texture, new Material(TextureAttribute.createDiffuse(Textures.get(texture))));
			return materialHashMap.get(texture);
		}
	}
}
