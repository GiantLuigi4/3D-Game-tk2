package com.tfc.world;

import com.tfc.blocks.BlockPos;

import java.util.Objects;

public class ChunkPos {
	public final BlockPos blockPos;
	public final int chunkX;
	public final int chunkY;
	public final int chunkZ;
	private static final int size=Chunk.size;
	
	public ChunkPos(int chunkX, int chunkY, int chunkZ) {
		this.chunkX = chunkX;
		this.chunkY = chunkY;
		this.chunkZ = chunkZ;
		this.blockPos = new BlockPos(chunkX*size,chunkY*size,chunkZ*size);
	}
	
	public ChunkPos(BlockPos blockPos) {
		int chunkX1;
		int chunkY1;
		int chunkZ1;
		int offX=(blockPos.x<0)?-1:0;
		int offY=(blockPos.y<0)?-1:0;
		int offZ=(blockPos.z<0)?-1:0;
		chunkX1=(blockPos.x-offX)/size;
		chunkY1=(blockPos.y-offY)/size;
		chunkZ1=(blockPos.z-offZ)/size;
		if (blockPos.x<0) {
			chunkX1-=1;
		}
		if (blockPos.y<0) {
			chunkY1-=1;
		}
		if (blockPos.z<0) {
			chunkZ1-=1;
		}
		this.chunkX=chunkX1;
		this.chunkY=chunkY1;
		this.chunkZ=chunkZ1;
		this.blockPos = new BlockPos(chunkX*size,chunkY*size,chunkZ*size);
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.toString().equals(obj.toString())&&obj.getClass().equals(this.getClass());
	}
	
	@Override
	public String toString() {
		return "ChunkPos{" +
				"chunkX=" + chunkX +
				", chunkY=" + chunkY +
				", chunkZ=" + chunkZ +
				'}';
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(chunkX, chunkY, chunkZ);
	}
}
