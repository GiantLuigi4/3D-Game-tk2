package com.tfc.model;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.tfc.utils.Location;
import com.tfc.utils.MaterialMap;

public class Triangle {
	//https://stackoverflow.com/questions/18448018/3d-triangle-in-libgdx
	public static ModelInstance createTriangle(Vector3 vert1, Vector3 vert2, Vector3 vert3) {
		Mesh mesh = new Mesh(true, 3, 3,
				new VertexAttribute(VertexAttributes.Usage.Position, 3, "vPosition"));
		
		mesh.setVertices(new float[]{vert1.x, vert1.y, vert1.z, vert2.x, vert2.y, vert2.z, vert3.x, vert3.y, vert3.z});
		mesh.setIndices(new short[]{0, 1, 2});
		
		Model model = new Model();
		model.meshes.add(mesh);
		
		return new ModelInstance(model);
	}
	
	public static ModelInstance createTriangle(Vector3 vert1, Vector3 vert2, Vector3 vert3, Location texture) {
		Material mat = MaterialMap.getOrCreate(texture);
		ModelBuilder builder = new ModelBuilder();
//		ModelBuilder builder = ThreeDeeFirstPersonGame.getInstance().modelBuilder;
		builder.begin();
		MeshPartBuilder builder1 = builder.part("tri", GL20.GL_TRIANGLES, Cube.defaultAttribs, mat);
		builder1.setColor(Color.RED);
		VertexInfo v1 = new VertexInfo().setPos(vert1).setNor(vert1).setCol(null).setUV(1.0f, 0.0f);
		VertexInfo v2 = new VertexInfo().setPos(vert2).setNor(vert2).setCol(null).setUV(0.0f, 0.0f);
		VertexInfo v3 = new VertexInfo().setPos(vert3).setNor(vert3).setCol(null).setUV(0.0f, 1.0f);
		builder1.triangle(v1, v2, v3);
		builder.part(builder1.getMeshPart(), mat);
		return new ModelInstance(builder.end());
	}
}
