package com.tfc.registry;

import com.tfc.blocks.Block;
import com.tfc.utils.Location;

import java.util.HashMap;
import java.util.Set;

public class Blocks {
	private static final HashMap<Location, Block> blocks = new HashMap<>();
	
	public static RegistryObject<Block> register(Block block) {
		return new RegistryObject<>(blocks.put(block.getName(), block));
	}
	
	public static Block get(Location name) {
		return blocks.get(name);
	}
	
	public static Block getByID(int id) {
		return (Block) blocks.values().toArray()[id];
	}
	
	public static int count() {
		return blocks.size();
	}
	
	public static Set<Location> allBlocks() {
		return blocks.keySet();
	}
}
