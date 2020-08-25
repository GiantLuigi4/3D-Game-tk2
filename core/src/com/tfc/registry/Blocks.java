package com.tfc.registry;

import com.tfc.blocks.Block;
import com.tfc.utils.Location;

import java.util.HashMap;

public class Blocks {
	private static final HashMap<Location, Block> blocks = new HashMap<>();
	
	public static RegistryObject<Block> register(Block block) {
		return new RegistryObject<>(blocks.put(block.getName(),block));
	}
	
	public static Block get(Location name) {
		return blocks.get(name);
	}
}
