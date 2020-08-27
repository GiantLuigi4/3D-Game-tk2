package com.tfc.blocks;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.tfc.utils.Location;

public class Block {
	private final Location name;
	public final ModelInstance modelInstance;
	
	public Block(Location name, ModelInstance modelInstance) {
		this.name = name;
		this.modelInstance = modelInstance;
	}
	
	public Location getName() {
		return name;
	}
	
	public BoundingBox getCollisionBox(BlockPos pos) {
		float scale = 2f;
		return new BoundingBox(new Vector3(pos.x * scale, pos.y * scale, pos.z * scale), new Vector3(pos.x * scale + scale, pos.y * scale + scale, pos.z * scale + scale));
	}
}
