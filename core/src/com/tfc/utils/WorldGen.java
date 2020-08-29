package com.tfc.utils;

import com.badlogic.gdx.math.Vector3;
import com.tfc.world.TerrainTriangle;
import net.rgsw.ptg.noise.Noise2D;

public class WorldGen {
	public static BiObject<TerrainTriangle, TerrainTriangle> getTerrainSquare(int x, int z, Noise2D noise, float scale, Location mat1, Location mat2) {
		float yPosTerrain1 = ((float) (noise.generate(x / scale, z / scale) * 32) + 16);
		float yPosTerrain2 = ((float) (noise.generate((x + 1) / scale, z / scale) * 32) + 16);
		float yPosTerrain3 = ((float) (noise.generate((x + 1) / scale, (z + 1) / scale) * 32) + 16);
		float yPosTerrain4 = ((float) (noise.generate(x / scale, (z + 1) / scale) * 32) + 16);
		float x0z0 = yPosTerrain1;
		float x1z0 = yPosTerrain2;
		float x1z1 = yPosTerrain3;
		float x0z1 = yPosTerrain4;
		TerrainTriangle tri1 = (new TerrainTriangle(
				new Vector3((x + 1) * 2, x1z1, (z + 1) * 2),
				new Vector3((x + 1) * 2, x1z0, z * 2),
				new Vector3(x * 2, x0z0, z * 2),
				mat1
		));
		TerrainTriangle tri2 = (new TerrainTriangle(
				new Vector3((x) * 2, x0z0, z * 2),
				new Vector3((x) * 2, x0z1, (z + 1) * 2),
				new Vector3((x + 1) * 2, x1z1, (z + 1) * 2),
				mat2
		));
		return new BiObject<>(tri1, tri2);
	}
}
