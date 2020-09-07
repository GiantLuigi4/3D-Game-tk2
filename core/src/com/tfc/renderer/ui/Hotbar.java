package com.tfc.renderer.ui;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.blocks.Block;
import com.tfc.events.render.RenderUI;
import com.tfc.registry.Blocks;
import com.tfc.utils.SpriteMap;

public class Hotbar {
	public static void render(RenderUI event) {
		for (int i = 0; i < 10; i++) {
			int slot = ThreeDeeFirstPersonGame.getInstance().getSlot();
			float offXSelected = 1.9575f;
			float offXNormal = 2;
			ThreeDeeFirstPersonGame.getInstance().spritehotbar.setSize(slot == i ? 52 : 48, slot == i ? 52 : 48);
			ThreeDeeFirstPersonGame.getInstance().spritehotbar.setPosition((i + (slot == i ? offXSelected : offXNormal)) * 48, (2 - (slot == i ? 4.5f : 2)));
			ThreeDeeFirstPersonGame.getInstance().spritehotbar.draw(event.getBatch());
			if (i < Blocks.count()) {
				Block block = Blocks.getByID(i);
				int size = 36;
				Sprite sprite = SpriteMap.getOrCreate(block.getName());
				sprite.setPosition(102 + (i * (size + 12)), 6);
				sprite.setSize(size, size);
				sprite.draw(event.getBatch());
			}
		}
	}
}
