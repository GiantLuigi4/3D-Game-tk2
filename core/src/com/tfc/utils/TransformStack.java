package com.tfc.utils;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TransformStack {
	private final SpriteBatch batch;
	
	public TransformStack(SpriteBatch batch) {
		this.batch = batch;
	}
	
	public void push() {
	}
	
	public void pop() {
	}
	
	public void scale(float x,float y,float z) {
		batch.getTransformMatrix().scale(x,y,z);
	}
}
