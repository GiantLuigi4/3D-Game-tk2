package com.tfc.world.chunks;

import com.tfc.utils.Compression;
import com.tfc.utils.Logger;
import com.tfc.world.TerrainTriangle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class TerrainChunk {
	public final ChunkPos pos;
	private final ArrayList<TerrainTriangle> terrain = new ArrayList<>();
	
	public TerrainChunk(ChunkPos pos) {
		this.pos = pos;
	}
	
	public static TerrainChunk read(File file, String posLoad, ChunkPos pos) {
		if (file.getName().endsWith(".zip")) {
			try {
				ZipFile file1 = new ZipFile(file);
				ZipEntry entry = file1.getEntry(posLoad + ".data");
				InputStream stream = file1.getInputStream(entry);
				file1.close();
				return loadChunkFromStream(stream, pos);
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		} else {
			for (File chunk : file.listFiles()) {
				if (file.getName().startsWith(posLoad)) {
					try {
						FileInputStream input = new FileInputStream(chunk);
						return loadChunkFromStream(input, pos);
					} catch (Throwable err) {
						Logger.logErrFull(err);
					}
				}
			}
		}
		return null;
	}
	
	public static TerrainChunk read(ZipFile file, ZipEntry entry, ChunkPos pos) {
		if (file.getName().endsWith(".zip")) {
			try {
				InputStream stream = file.getInputStream(entry);
				return loadChunkFromStream(stream, pos);
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		}
		return null;
	}
	
	private static TerrainChunk loadChunkFromStream(InputStream stream, ChunkPos pos) throws IOException {
		byte[] bytes = new byte[stream.available()];
		stream.read(bytes);
		char[] chars = new char[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			chars[i] = (char) bytes[i];
		}
		stream.close();
		return loadChunkFromString(Compression.decompress(Compression.reQuadruple(Compression.makeLegible(new String(bytes)))), pos);
	}
	
	private static TerrainChunk loadChunkFromString(String terrainData, ChunkPos pos) {
		TerrainChunk terrain = new TerrainChunk(pos);
		for (String s : terrainData.split("\n")) {
			try {
				terrain.add(TerrainTriangle.fromString(s));
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
		}
		return terrain;
	}
	
	public void forEach(Consumer<TerrainTriangle> triangleConsumer) {
		if (terrain != null) terrain.forEach(triangleConsumer);
	}
	
	public String toString() {
		StringBuilder terrainS = new StringBuilder();
		for (TerrainTriangle tri : terrain) {
			terrainS.append(tri.toString()).append("\n");
		}
		return Compression.makeIllegible(Compression.deQuadruple(Compression.compress(terrainS.toString())));
	}
	
	public void add(TerrainTriangle tri) {
		terrain.add(tri);
	}
}
