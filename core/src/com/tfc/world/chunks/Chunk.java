package com.tfc.world.chunks;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.model.Cube;
import com.tfc.utils.BiObject;
import com.tfc.world.World;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;


//This is mostly a copy from the original writing of this game, https://github.com/GiantLuigi4/3D-Game/blob/master/src/main/java/game/Chunk.java
public class Chunk {
	public static final int size = 16;
	
	private final BiObject<Block, BlockPos>[] blocks = new BiObject[size * size * size];
	public final World world;
	public final ChunkPos pos;
	
	public Chunk(World world, ChunkPos pos) {
		this.world = world;
		this.pos = pos;
	}
	
	public BiObject<Block, BlockPos>[] getBlocks() {
		return blocks.clone();
	}
	
	public Block getBlock(BlockPos pos) {
		BlockPos pos1 = methodToName(pos);
		int index = getIndexFromPos(pos1);
		if (index < blocks.length && index >= 0) {
			BiObject<Block, BlockPos> blockBlockPosBiObject = blocks[index];
			if (blockBlockPosBiObject != null) {
				return blockBlockPosBiObject.getObj1();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (BiObject<Block, BlockPos> block : this.getBlocks()) {
			if (block != null && block.getObj1() != null) {
				builder
						.append(block.getObj1().getName())
						.append(',').append(block.getObj2().x)
						.append(',').append(block.getObj2().y)
						.append(',').append(block.getObj2().z)
						.append('\n')
				;
			}
		}
		return builder.toString();
	}
	
	private int getIndexFromPos(BlockPos pos) {
		if (pos.x < 0 || pos.y < 0 || pos.z < 0 || pos.x > size || pos.y > size || pos.z > size) {
			return -1;
		}
		return (pos.x) + (pos.y * size) + (pos.z * size * size);
	}
	
	public void setBlock(BlockPos pos, Block block) {
		BlockPos pos1 = methodToName(pos);
		if (blocks[getIndexFromPos(pos1)] != null) {
//			blocks[getIndexFromPos(pos1)].onRemove(world);
		}
		if (block != null) {
			blocks[getIndexFromPos(pos1)] = new BiObject<>(block, pos);
		}
		if (block == null) blocks[getIndexFromPos(pos1)] = null;
//		if (block!=null) block.onPlace(world);
	}
	
	//Get a pos in the chunk from a pos in the world
	public static BlockPos methodToName(BlockPos pos1) {
		ChunkPos pos = new ChunkPos(pos1);
		int x = pos1.x - pos.blockPos.x;
		int y = pos1.y - pos.blockPos.y;
		int z = pos1.z - pos.blockPos.z;
		return new BlockPos(x, y, z);
	}
	
	public Model bake(BiObject<ModelBuilder, HashMap<BiObject<Integer, Material>, MeshBuilder>> meshData) {
		HashMap<BiObject<Integer, Material>, MeshBuilder> modelBuilders = meshData.getObj2();
		ModelBuilder modelBuilder = meshData.getObj1();
		AtomicInteger integer = new AtomicInteger(0);
		modelBuilders.forEach((mat, meshBuilder) -> {
			Mesh mesh = meshBuilder.end();
			modelBuilder.part(integer.toString(), mesh, mat.getObj1(), mat.getObj2());
			integer.getAndAdd(1);
		});
		return modelBuilder.end();
	}
	
	public BiObject<ModelBuilder, HashMap<BiObject<Integer, Material>, MeshBuilder>> createMesh() {
		HashMap<BiObject<Integer, Material>, MeshBuilder> modelBuilders = new HashMap<>();
		for (BiObject<Block, BlockPos> block : blocks) {
			if (block != null && block.getObj1() != null) {
				if (
						this.getBlock(block.getObj2().offset(1, 0, 0)) == null ||
								this.getBlock(block.getObj2().offset(-1, 0, 0)) == null ||
								this.getBlock(block.getObj2().offset(0, 1, 0)) == null ||
								this.getBlock(block.getObj2().offset(0, -1, 0)) == null ||
								this.getBlock(block.getObj2().offset(0, 0, 1)) == null ||
								this.getBlock(block.getObj2().offset(0, 0, -1)) == null
				) {
					block.getObj1().modelInstance.transform.setTranslation(block.getObj2().x, block.getObj2().y, block.getObj2().z);
					Array<MeshPart> parts = block.getObj1().modelInstance.model.meshParts;
					Array<Material> mats = block.getObj1().modelInstance.model.materials;
					for (int id = 0; id < parts.size; id++) {
						MeshPart mesh = parts.get(id);
						Material material = mats.get(id);
						BiObject<Integer, Material> obj = new BiObject<>(mesh.primitiveType, material);
						if (!modelBuilders.containsKey(obj)) {
							modelBuilders.put(obj, new MeshBuilder());
							modelBuilders.get(obj).begin(Cube.defaultAttribs, mesh.primitiveType);
						}
						Matrix4 matrix4 = new Matrix4();
						modelBuilders.get(obj).getVertexTransform(matrix4);
						matrix4.setTranslation(block.getObj2().x * 2, block.getObj2().y * 2, block.getObj2().z * 2);
						modelBuilders.get(obj).setVertexTransform(matrix4);
						modelBuilders.get(obj).addMesh(mesh);
					}
				}
			}
		}
		ModelBuilder modelBuilder = new ModelBuilder();
		modelBuilder.begin();
		return new BiObject<>(modelBuilder, modelBuilders);
	}
}
