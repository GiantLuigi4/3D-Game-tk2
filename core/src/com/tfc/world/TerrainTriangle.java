package com.tfc.world;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.tfc.model.Triangle;
import com.tfc.utils.Location;

import java.util.ArrayList;

public class TerrainTriangle {
	final Vector3 v1, v2, v3;
	public final ModelInstance renderable;
	public final Location texture;
	public final Vector3 min;
	
	public TerrainTriangle(Vector3 v1, Vector3 v2, Vector3 v3, Location texture) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		min = new Vector3(
				Math.min(v1.x, Math.min(v2.x, v3.x)),
				Math.min(v1.y, Math.min(v2.y, v3.y)),
				Math.min(v1.z, Math.min(v2.z, v3.z))
		);
		this.renderable = Triangle.createTriangle(
				new Vector3(v1).sub(0, 3, 0),
				new Vector3(v2).sub(0, 3, 0),
				new Vector3(v3).sub(0, 3, 0),
				(texture)
		);
		this.texture = texture;
	}
	
	public static TerrainTriangle fromString(String string) {
		String text = string;
		String num1 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		String num2 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		String num3 = text.substring(0, text.indexOf(":"));
		text = text.substring(text.indexOf(":") + 1);
		Vector3 v1 = new Vector3(
				Float.parseFloat(num1.replace(",", "")),
				Float.parseFloat(num2.replace(",", "")),
				Float.parseFloat(num3.replace(":", ""))
		);
		num1 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		num2 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		num3 = text.substring(0, text.indexOf(":"));
		text = text.substring(text.indexOf(":") + 1);
		Vector3 v2 = new Vector3(
				Float.parseFloat(num1.replace(",", "")),
				Float.parseFloat(num2.replace(",", "")),
				Float.parseFloat(num3.replace(":", ""))
		);
		num1 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		num2 = text.substring(0, text.indexOf(","));
		text = text.substring(text.indexOf(",") + 1);
		num3 = text.substring(0, text.indexOf(":"));
		text = text.substring(text.indexOf(":") + 1);
		Vector3 v3 = new Vector3(
				Float.parseFloat(num1.replace(",", "")),
				Float.parseFloat(num2.replace(",", "")),
				Float.parseFloat(num3.replace(",", ""))
		);
		return new TerrainTriangle(v1, v2, v3, new Location(text));
	}
	
	public String toString() {
		return (v1 + ":" + v2 + ":" + v3 + ":" + texture.toString()).replace("(", "").replace(")", "");
	}
	
	public void draw(ModelBatch batch, Environment environment, Vector3 offset) {
		renderable.transform.setTranslation(offset);
		renderable.transform.translate(min);
		batch.render(renderable, environment);
	}
	
	public boolean collides(Vector3 pos) {
		return new BoundingBox(new Vector3(
				Math.min(v1.x, Math.min(v2.x, v3.x)),
				Math.min(v1.y, Math.min(v2.y, v3.y)) - 1,
				Math.min(v1.z, Math.min(v2.z, v3.z))
		), new Vector3(
				Math.max(v1.x, Math.max(v2.x, v3.x)),
				Math.max(v1.y, Math.max(v2.y, v3.y)) + 1,
				Math.max(v1.z, Math.max(v2.z, v3.z))
		)).contains(pos);
	}
	
	public float getCollisionPosY(Vector3 pos) {
		ArrayList<Vector3> poses = new ArrayList<>();
		float precision = 16f;
		for (int i = 0; i < precision; i++) {
			poses.add(new Vector3(v1).lerp(new Vector3(v2), i / precision));
			poses.add(new Vector3(v1).lerp(new Vector3(v3), i / precision));
			poses.add(new Vector3(v2).lerp(new Vector3(v3), i / precision));
		}
		float closestDist = 9999;
		Vector3 closestPoint = null;
		Vector3 closestPointDisregardingXZ = null;
		for (Vector3 point1 : poses) {
			for (Vector3 point2 : poses) {
				if (!point1.equals(point2)) {
					for (float f = 0; f <= 1; f += 0.01f) {
						Vector3 lerped = new Vector3(point1).lerp(new Vector3(point2), f);
						if (lerped.y > pos.y) {
							float dist = lerped.dst(new Vector3(pos.x, lerped.y, pos.z));
							if (dist < closestDist) {
								if (
										Math.abs(pos.x - lerped.x) <= 0.01f &&
												Math.abs(pos.z - lerped.z) <= 0.01f
								) {
									closestPoint = lerped;
									closestDist = dist;
								} else {
									closestPointDisregardingXZ = closestPoint;
								}
							}
						}
					}
				}
			}
		}
//		Vector3 pos1 = new Vector3(pos);
//		pos1.set(v1.x,v1.y,pos1.z);
//		Vector3 pos2 = new Vector3(pos);
//		pos2.set(pos2.x,v2.y,v2.z);
//		Vector3 pos3 = pos1.lerp(v1,pos1.dst(v1)*0.5f);
//		Vector3 pos4 = pos2.lerp(v2,pos2.dst(v2)*0.5f);
//		pos3 = pos3.lerp(pos4,0.5f);
//		if (closestPoint == null|| pos3.y>closestPoint.y) {
//			return pos3.y;
//		}
		if (closestPoint == null) {
			if (closestPointDisregardingXZ == null) {
				return pos.y - 1;
			} else {
				return closestPointDisregardingXZ.y;
			}
		}
		return closestPoint.y;
	}
	
	enum collisionType {
		FLOOR(0, 1, 0),
		NONE(0, 0, 0);
		
		int x, y, z;
		
		collisionType(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
