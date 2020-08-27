package com.tfc.registry;

import com.badlogic.gdx.graphics.Texture;
import com.tfc.utils.Location;

import java.util.HashMap;

public class Textures {
	private static final HashMap<Location, Texture> textures = new HashMap<>();
	
	public static RegistryObject<Texture> register(Location name, Texture texture) {
		return new RegistryObject<>(textures.put(name, texture));
	}
	
	public static Texture get(Location name) {
		return textures.get(name);
	}
	
	public static void close() {
		textures.forEach((location, texture) -> {
			texture.dispose();
		});
	}
}
