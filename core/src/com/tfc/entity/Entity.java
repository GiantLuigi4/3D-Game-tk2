package com.tfc.entity;

import com.badlogic.gdx.math.Vector3;

public class Entity {
	public Vector3 pos = new Vector3(0,0,0);
	public Vector3 velocity = new Vector3();
	public boolean onGround = false;
	
	public void tick() {
		pos.add(velocity);
		velocity.y-=0.025;
		velocity.x*=onGround?0.25f:0.3f;
		velocity.z*=onGround?0.25f:0.3f;
	}
}
