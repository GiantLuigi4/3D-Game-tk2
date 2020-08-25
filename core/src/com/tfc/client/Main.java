package com.tfc.client;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.entity.Player;
import com.tfc.utils.BiObject;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tfc.ThreeDeeFirstPersonGame.getInstance;

public class Main {
	public static void tick(ArrayList<Integer> keys) {
		Player player = getInstance().player;
		player.tick();
		if (keys.contains(21)) {
			getInstance().camRotX += 1;
		} else if (keys.contains(22)) {
			getInstance().camRotX -= 1;
		}
		if (keys.contains(20)) {
			getInstance().camRotY--;
		}
		if (keys.contains(19)) {
			getInstance().camRotY++;
		}
		if (keys.contains(51)) {
			int rotOffset = 0;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		if (keys.contains(47)) {
			int rotOffset = 180;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		if (keys.contains(32)) {
			int rotOffset = 90;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		if (keys.contains(29)) {
			int rotOffset = -90;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		AtomicBoolean onGround = new AtomicBoolean(false);
		float playerHeight = 4;
		float playerWidth = 0.5f;
		float padding = 0.1f;
		
		float widthMinusPadding = playerWidth - padding;
		float widthPlusPadding = playerWidth + padding;
		float widthPlusOnePlusPadding = ((playerWidth + 1) + padding);
		
		getInstance().world.chunks.forEach((chunkPos, chunk) -> {
			for (BiObject<Block, BlockPos> block : chunk.getBlocks()) {
				if (block != null && block.getObj1() != null) {
					float wallCollisionYBottom = player.pos.y - (playerHeight - (1 + padding));
					float wallCollisionYTop = player.pos.y - (1 + padding);
					BoundingBox cb = block.getObj1().getCollisionBox(block.getObj2());
					//Floor Collision
					if (cb.intersects(new BoundingBox(
							new Vector3(player.pos.x - widthMinusPadding, player.pos.y - playerHeight, player.pos.z - widthMinusPadding),
							new Vector3(player.pos.x + widthMinusPadding, player.pos.y - (playerHeight / 2f), player.pos.z + widthMinusPadding)
					))) {
						player.pos.y += (cb.max.y - (player.pos.y - playerHeight));
						player.velocity.y = 0;
						onGround.set(true);
					}
					//Wall Collision -X
					if (cb.intersects(new BoundingBox(
							new Vector3(player.pos.x - widthPlusPadding, wallCollisionYBottom, player.pos.z - widthMinusPadding),
							new Vector3(player.pos.x + 0, wallCollisionYTop, player.pos.z + widthMinusPadding)
					))) {
						player.pos.x += (cb.max.x - (player.pos.x - widthPlusPadding));
						player.velocity.x = 0;
					}
					//Wall Collision +X
					if (cb.intersects(new BoundingBox(
							new Vector3(player.pos.x - 0, wallCollisionYBottom, player.pos.z - widthMinusPadding),
							new Vector3(player.pos.x + widthPlusOnePlusPadding + (padding*3.999f), wallCollisionYTop, player.pos.z + widthMinusPadding)
					))) {
						player.pos.x += (cb.min.x - (player.pos.x + widthPlusOnePlusPadding + (padding*4)));
						player.velocity.x = 0;
					}
					//Wall Collision -Z
					if (cb.intersects(new BoundingBox(
							new Vector3(player.pos.x - widthMinusPadding, wallCollisionYBottom, player.pos.z - widthPlusPadding),
							new Vector3(player.pos.x + widthMinusPadding, wallCollisionYTop, player.pos.z + 0)
					))) {
						player.pos.z += (cb.max.z - (player.pos.z - widthPlusPadding));
						player.velocity.z = 0;
					}
					//Wall Collision +Z
					if (cb.intersects(new BoundingBox(
							new Vector3(player.pos.x - widthMinusPadding, wallCollisionYBottom, player.pos.z - 0),
							new Vector3(player.pos.x + widthMinusPadding, wallCollisionYTop, player.pos.z + widthPlusOnePlusPadding + (padding*3.999f))
					))) {
						player.pos.z += (cb.min.z - (player.pos.z + widthPlusOnePlusPadding + (padding*4)));
						player.velocity.z = 0;
					}
				}
			}
		});
		if (keys.contains(62)) {
			if (player.onGround) {
				player.velocity.lerp(new Vector3(player.velocity.x, 2.0f, player.velocity.z), 0.1f);
			}
		}
		player.onGround = onGround.get();
//		player.pos.y = 5;
//		player.velocity.y = -0.1f;
		try {
			Thread.sleep(10);
		} catch (Throwable ignored) {
		}
	}
}
