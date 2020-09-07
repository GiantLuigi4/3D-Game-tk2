package com.tfc.renderer.ui;

import com.badlogic.gdx.Gdx;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.events.render.RenderUI;
import com.tfc.inputs.Mouse;
import com.tfc.registry.Textures;
import com.tfc.utils.Location;
import com.tfc.utils.files.Files;
import com.tfc.world.World;

import java.io.File;

public class WorldList {
	private static int selection = 0;
	
	public static void render(RenderUI event) {
		File[] files = Files.get("saves").listFiles();
		int y = 438;
		float mx = Mouse.getMouseX() / (float) Gdx.graphics.getWidth();
		float my = Mouse.getMouseY() / (float) Gdx.graphics.getHeight();
		int index = 0;
		for (File f : files) {
			if (f.getName().endsWith(".gz")) {
				boolean isHovered = drawWorld(f, event, index, mx, my, y);
				y -= 42;
				if (isHovered && Mouse.isPressed(0)) {
					Mouse.release(0);
					if (selection == index) {
						ThreeDeeFirstPersonGame.getInstance().world = new World();
						
						if (!f.exists()) ThreeDeeFirstPersonGame.getInstance().createWorld(f);
						else ThreeDeeFirstPersonGame.getInstance().loadWorld("saves/" + f.getName());
						
						ThreeDeeFirstPersonGame.getInstance().ingame = true;
					} else {
						selection = index;
					}
				}
			}
			index++;
		}
		
		File f = Files.get("saves/" + "world " + (files.length + 1) + ".gz");
		boolean isHovered = drawWorld(f, event, index, mx, my, y);
		if (isHovered && Mouse.isPressed(0)) {
			Mouse.release(0);
			ThreeDeeFirstPersonGame.getInstance().world = new World();
			
			if (!f.exists()) ThreeDeeFirstPersonGame.getInstance().createWorld(f);
			else ThreeDeeFirstPersonGame.getInstance().loadWorld("saves/" + f.getName());
			
			ThreeDeeFirstPersonGame.getInstance().ingame = true;
		}
	}
	
	private static boolean drawWorld(File f, RenderUI event, int index, float mx, float my, int y) {
		boolean isHovered =
				((int) (438 - (my * 438)) <= y)
						&& ((int) (438 - (my * 438)) >= (y - 40));
		float color = index == selection ? 1 : isHovered ? 0.75f : 0.25f;
		event.getBatch().setColor(color, color, color, 1);
		event.getBatch().draw(
				Textures.get(new Location(ThreeDeeFirstPersonGame.namespace + ":save_bg")),
				0, y,
				1, 42,
				0, 0,
				(1f / 32), 1
		);
		event.getBatch().draw(
				Textures.get(new Location(ThreeDeeFirstPersonGame.namespace + ":save_bg")),
				41, y,
				1, 42,
				(31f / 32), 0,
				1, 1
		);
		event.getBatch().setColor(1, 1, 1, 1);
		for (int i = 42; i < 638; i += 42) {
			event.getBatch().draw(
					Textures.get(new Location(ThreeDeeFirstPersonGame.namespace + ":save_bg")),
					i, y,
					42, 42,
					(1f / 32), 0,
					(31f / 32), 1
			);
		}
		event.getBatch().setColor(color, color, color, 1);
		event.getBatch().draw(
				Textures.get(new Location(ThreeDeeFirstPersonGame.namespace + ":white_outline")),
				0, y,
				639, 42,
				(1f / 32), 0,
				(31f / 32), 1
		);
		event.getBatch().draw(
				Textures.get(new Location(ThreeDeeFirstPersonGame.namespace + ":save_bg")),
				639, y,
				1, 42,
				(31f / 32), 0,
				1, 1
		);
		event.getBatch().setColor(0, 0, 0, 1);
		ThreeDeeFirstPersonGame.defaultFont.draw(event.getBatch(), f.getName().substring(0, f.getName().length() - 3), 46, y + 5, 14, 30);
		event.getBatch().setColor(1, 1, 1, 1);
		return isHovered;
	}
}
