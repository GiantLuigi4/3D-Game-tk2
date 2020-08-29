package com.tfc.world;

import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.registry.Blocks;
import com.tfc.utils.*;
import com.tfc.world.chunks.Chunk;
import com.tfc.world.chunks.ChunkPos;
import com.tfc.world.chunks.TerrainChunk;
import net.rgsw.ptg.noise.Noise2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class World {
	public HashMap<ChunkPos, Chunk> chunks = new HashMap<>();
	public HashMap<ChunkPos, TerrainChunk> terrainChunks = new HashMap<>();
	public ArrayList<ChunkPos> needsRefresh = new ArrayList<>();
	
	public boolean hasChunk(BlockPos pos) {
		ChunkPos pos1 = new ChunkPos(pos);
		return chunks.containsKey(pos1);
	}
	
	public Chunk getChunk(BlockPos pos) {
		ChunkPos pos1 = new ChunkPos(pos);
		if (!chunks.containsKey(pos1)) {
			chunks.put(pos1, new Chunk(this, pos1));
//			System.out.println("missing chunk at pos:"+pos1);
		}
		return chunks.get(pos1);
	}
	
	public void setBlock(BlockPos pos, Block block) {
		Block bk = getChunk(pos).getBlock(pos);
//		if (bk!=null) bk.onRemove(this);
		if (!needsRefresh.contains(getChunk(pos).pos))
			needsRefresh.add(getChunk(pos).pos);
		getChunk(pos).setBlock(pos, block);
//		block.onPlace(this);
	}
	
	public void loadTerrainChunks(File file) {
		try {
			ZipFile file1 = new ZipFile(file);
			file1.stream().forEach(zipEntry -> {
				String pos = zipEntry.getName().replace(".data", "");
				String[] nums = pos.split(",");
				ChunkPos pos1 = new ChunkPos(
						Integer.parseInt(nums[0]),
						Integer.parseInt(nums[1]),
						Integer.parseInt(nums[2])
				);
				TerrainChunk chunk = TerrainChunk.read(file1, zipEntry, pos1);
				this.terrainChunks.put(pos1, chunk);
			});
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	public void loadAll(File file) {
		if (file.getName().endsWith(".zip")) {
			try {
				ZipFile file1 = new ZipFile(file);
				Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file1.entries();
				while (entries.hasMoreElements()) {
					InputStream stream = file1.getInputStream(entries.nextElement());
					loadChunkFromStream(stream);
				}
				file1.close();
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		} else {
			for (File chunk : file.listFiles()) {
				try {
					FileInputStream input = new FileInputStream(chunk);
					loadChunkFromStream(input);
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
			}
		}
	}
	
	private void loadChunkFromStream(InputStream input) throws IOException {
		StringBuilder builder = new StringBuilder();
		byte[] bytes = new byte[input.available()];
		input.read(bytes);
		for (byte b : bytes) {
			builder.append((char) b);
		}
		input.close();
		String saveData = Compression.reQuadruple(Compression.decompress(Compression.makeLegible(builder.toString())));
		
		loadChunkFromString(saveData);
	}
	
	private void loadChunkFromString(String saveData) {
		for (String s : saveData.split("\n")) {
			String[] strings = s.split(",");
			try {
				Block block = Blocks.get(new Location(strings[0].replace(",", "")));
				BlockPos pos = new BlockPos(
						Integer.parseInt(strings[1].replace(",", "")),
						Integer.parseInt(strings[2].replace(",", "")),
						Integer.parseInt(strings[3].replace(",", ""))
				);
				this.setBlock(pos, block);
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		}
	}
	
	public void addTerrainTriangle(TerrainTriangle triangle) {
		ChunkPos pos = new ChunkPos(new BlockPos(
				(int) Math.min(triangle.v1.x, Math.min(triangle.v2.x, triangle.v3.x)),
				(int) Math.min(triangle.v1.y, Math.min(triangle.v2.y, triangle.v3.y)),
				(int) Math.min(triangle.v1.z, Math.min(triangle.v2.z, triangle.v3.z))
		));
		if (terrainChunks.containsKey(pos)) {
			TerrainChunk chunk = terrainChunks.get(pos);
			chunk.add(triangle);
		} else {
			TerrainChunk chunk = new TerrainChunk(pos);
			chunk.add(triangle);
			terrainChunks.put(pos, chunk);
		}
	}
	
	public void loadChunk(File file, String posLoad) {
		if (file.getName().endsWith(".zip")) {
			try {
				ZipFile file1 = new ZipFile(file);
				ZipEntry entry = file1.getEntry(posLoad + ".data");
				InputStream stream = file1.getInputStream(entry);
				loadChunkFromStream(stream);
				file1.close();
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		} else {
			for (File chunk : file.listFiles()) {
				if (file.getName().startsWith(posLoad)) {
					try {
						FileInputStream input = new FileInputStream(chunk);
						loadChunkFromStream(input);
					} catch (Throwable err) {
						Logger.logErrFull(err);
					}
				}
			}
		}
	}
	
	public void removeBlock(BlockPos pos) {
		Block bk = getChunk(pos).getBlock(pos);
//		if (bk!=null) bk.onRemove(this);
		if (!needsRefresh.contains(getChunk(pos).pos))
			needsRefresh.add(getChunk(pos).pos);
		getChunk(pos).setBlock(pos, null);
	}
	
	public Block getBlock(BlockPos pos) {
		return getChunk(pos).getBlock(pos);
	}
	
	public void generate(int chunkX, int chunkZ, Noise2D noise) {
		int genX = chunkX * (Chunk.size / 2);
		int genZ = chunkZ * (Chunk.size / 2);
		for (int x = 0; x < (Chunk.size / 2); x++) {
			for (int z = 0; z < (Chunk.size / 2); z++) {
				BiObject<TerrainTriangle, TerrainTriangle> triangles = WorldGen.getTerrainSquare(genX + x, genZ + z, noise, 1f, new Location(ThreeDeeFirstPersonGame.namespace, "grass"), new Location(ThreeDeeFirstPersonGame.namespace, "grass"));
				this.addTerrainTriangle(triangles.getObj1());
				this.addTerrainTriangle(triangles.getObj2());
			}
		}
	}
}
