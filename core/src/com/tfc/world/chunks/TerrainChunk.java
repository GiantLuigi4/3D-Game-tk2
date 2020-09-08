package com.tfc.world.chunks;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.files.compression.Numbers;
import com.tfc.model.Cube;
import com.tfc.utils.BiObject;
import com.tfc.utils.Location;
import com.tfc.utils.Logger;
import com.tfc.utils.files.Compression;
import com.tfc.world.TerrainTriangle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
	
	public static TerrainChunk read(InputStream stream, ChunkPos pos) {
		try {
			return loadChunkFromStream(stream, pos);
		} catch (Throwable err) {
			Logger.logErrFull(err);
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
		return loadChunkFromString(Numbers.decompress(Compression.decompress(new String(bytes)), true, true), pos);
	}
	
	public ModelInstance bake() {
		HashMap<Location, BiObject<Material, MeshBuilder>> builders = new HashMap<>();
		for (TerrainTriangle tri : terrain) {
			tri.createRenderable();
			if (builders.containsKey(tri.texture)) {
				MeshBuilder builder = builders.get(tri.texture).getObj2();
				tri.renderable.model.meshes.forEach(builder::addMesh);
			} else {
				builders.put(tri.texture, new BiObject<>(tri.renderable.model.materials.get(0), new MeshBuilder()));
				MeshBuilder builder = builders.get(tri.texture).getObj2();
				builder.begin(Cube.defaultAttribs);
				tri.renderable.model.meshes.forEach(builder::addMesh);
			}
		}
		ModelInstance instance = null;
		ModelBuilder modelBuilder = ThreeDeeFirstPersonGame.getInstance().modelBuilder;
		modelBuilder.begin();
		AtomicInteger integer = new AtomicInteger(0);
		builders.values().forEach(builder -> {
			Mesh mesh = builder.getObj2().end();
			modelBuilder.part(integer.toString(), mesh, GL20.GL_TRIANGLES, builder.getObj1());
			integer.getAndIncrement();
		});
		instance = new ModelInstance(modelBuilder.end());
		return instance;
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
