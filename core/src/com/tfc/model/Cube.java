package com.tfc.model;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.utils.Location;
import com.tfc.utils.MaterialMap;

public class Cube {
	public static final int defaultAttribs =
			VertexAttributes.Usage.Position |
					VertexAttributes.Usage.Normal |
					VertexAttributes.Usage.TextureCoordinates |
					VertexAttributes.Usage.BiNormal |
					VertexAttributes.Usage.Tangent;
	
	public static ModelInstance createModel(int att, Texture texture) {
		Model mdl = (ThreeDeeFirstPersonGame.getInstance().modelBuilder.createBox(2f, 2f, 2f, new Material(TextureAttribute.createDiffuse(texture)), att));
		return new ModelInstance(mdl, 0, 0, 0);
	}
	
	public static ModelInstance createModel(Location texture) {
		Model mdl = (
				ThreeDeeFirstPersonGame.getInstance().modelBuilder.createBox
						(
								2f, 2f, 2f,
								MaterialMap.getOrCreate(texture),
								defaultAttribs
						)
		);
		return new ModelInstance(mdl, 0, 0, 0);
	}
	
	public static ModelInstance createTransparentModel(Location texture) {
		Model mdl = (
				ThreeDeeFirstPersonGame.getInstance().modelBuilder.createBox
						(
								2f, 2f, 2f,
								MaterialMap.getOrCreate(texture),
								defaultAttribs
						)
		);
		mdl.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
		return new ModelInstance(mdl, 0, 0, 0);
	}
}
