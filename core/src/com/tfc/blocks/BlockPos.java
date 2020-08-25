package com.tfc.blocks;

import java.util.Objects;

public class BlockPos {
	public final int x;
	public final int y;
	public final int z;
	
	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y, z);
	}
}
