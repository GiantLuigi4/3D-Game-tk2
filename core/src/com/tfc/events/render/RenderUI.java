package com.tfc.events.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tfc.events.EventBase;

public class RenderUI extends EventBase {
	private SpriteBatch batch;
	
	public void post(SpriteBatch batch) {
		this.batch = batch;
		post();
	}
	
	public SpriteBatch getBatch() {
		return batch;
	}
	
	@Override
	public void post() {
		forEachListener((location, consumer) -> consumer.accept(this));
	}
}
