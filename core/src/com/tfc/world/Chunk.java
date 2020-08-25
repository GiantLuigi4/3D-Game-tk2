package com.tfc.world;

import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.utils.BiObject;


//This is mostly a copy from the original writing of this game, https://github.com/GiantLuigi4/3D-Game/blob/master/src/main/java/game/Chunk.java
public class Chunk {
	public static final int size = 32;
	
	private final BiObject<Block,BlockPos>[] blocks=new BiObject[size*size*size];
	public final World world;
	public final ChunkPos pos;
	
	public Chunk(World world, ChunkPos pos) {
		this.world = world;
		this.pos = pos;
	}
	
	public BiObject<Block,BlockPos>[] getBlocks() {
		return blocks.clone();
	}
	
	public Block getBlock(BlockPos pos) {
		BlockPos pos1=methodToName(pos);
		BiObject<Block,BlockPos> blockBlockPosBiObject = blocks[getIndexFromPos(pos1)];
		if (blockBlockPosBiObject != null) {
			return blockBlockPosBiObject.getObj1();
		} else {
			return null;
		}
	}
	
	private int getIndexFromPos(BlockPos pos) {
		return (pos.x)+(pos.y*size)+(pos.z*size*size);
	}
	
	public void setBlock(BlockPos pos, Block block) {
		BlockPos pos1=methodToName(pos);
		if (blocks[getIndexFromPos(pos1)]!=null) {
//			blocks[getIndexFromPos(pos1)].onRemove(world);
		}
		if (block!=null) {
			blocks[getIndexFromPos(pos1)]=new BiObject<>(block,pos);
		}
		if (block==null) blocks[getIndexFromPos(pos1)]=null;
//		if (block!=null) block.onPlace(world);
	}
	
	//Get a pos in the chunk from a pos in the world
	public static BlockPos methodToName(BlockPos pos1) {
		ChunkPos pos = new ChunkPos(pos1);
		int x = pos1.x-pos.blockPos.x;
		int y = pos1.y-pos.blockPos.y;
		int z = pos1.z-pos.blockPos.z;
		return new BlockPos(x,y,z);
	}
}
