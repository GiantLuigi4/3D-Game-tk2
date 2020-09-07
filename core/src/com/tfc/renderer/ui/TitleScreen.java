package com.tfc.renderer.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.events.render.RenderUI;
import com.tfc.inputs.Mouse;
import com.tfc.registry.Textures;
import com.tfc.utils.Location;

import static com.tfc.ThreeDeeFirstPersonGame.menu;
import static com.tfc.ThreeDeeFirstPersonGame.namespace;

public class TitleScreen {
	public static void render(RenderUI event) {
		Texture texture;
		float percentX = Mouse.getMouseX() / (float) Gdx.graphics.getWidth();
		float percentY = Mouse.getMouseY() / (float) Gdx.graphics.getHeight();
		if (percentX >= 0.405 && percentX <= 0.605 && percentY >= 0.4075 && percentY <= 0.5425) {
			texture = Textures.get(new Location(namespace + ":button_hovered"));
			if (ThreeDeeFirstPersonGame.getInstance().isLeftDown()) {
				ThreeDeeFirstPersonGame.getInstance().setLeftDown(false);
				menu = 1;
			}
		} else {
			texture = Textures.get(new Location(namespace + ":button"));
		}
		event.getBatch().draw(
				texture,
				260, 220,
				128, 64
		);
		event.getBatch().setColor(0, 0, 0, 1);
		ThreeDeeFirstPersonGame.defaultFont.draw(event.getBatch(), "Play", 295, 235, 14, 30);
		event.getBatch().setColor(1, 1, 1, 1);
	}
}
