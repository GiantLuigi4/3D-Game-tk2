package com.tfc.world;

import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;

public class World {
	public HashMap<ChunkPos,Chunk> chunks=new HashMap<>();
	public ArrayList<ChunkPos> needsRefresh=new ArrayList<>();

	public Chunk getChunk(BlockPos pos) {
		ChunkPos pos1=new ChunkPos(pos);
		if (!chunks.containsKey(pos1)) {
			chunks.put(pos1,new Chunk(this,pos1));
			System.out.println("missing chunk at pos:"+pos1);
		}
		return chunks.get(pos1);
	}
	public void setBlock(BlockPos pos,Block block) {
		Block bk=getChunk(pos).getBlock(pos);
//		if (bk!=null) bk.onRemove(this);
		if (!needsRefresh.contains(getChunk(pos).pos))
			needsRefresh.add(getChunk(pos).pos);
		getChunk(pos).setBlock(pos,block);
//		block.onPlace(this);
	}
	
	public void removeBlock(BlockPos pos) {
		Block bk=getChunk(pos).getBlock(pos);
//		if (bk!=null) bk.onRemove(this);
		if (!needsRefresh.contains(getChunk(pos).pos))
			needsRefresh.add(getChunk(pos).pos);
		getChunk(pos).setBlock(pos,null);
	}
	
	public Block getBlock(BlockPos pos) {
		return getChunk(pos).getBlock(pos);
	}
}
