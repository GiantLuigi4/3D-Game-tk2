package com.tfc.client;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.entity.Player;
import com.tfc.utils.BiObject;
import com.tfc.utils.discord.rich_presence.RichPresence;
import com.tfc.world.chunks.Chunk;
import com.tfc.world.chunks.ChunkPos;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.tfc.ThreeDeeFirstPersonGame.getInstance;

public class Main {
	
	private static boolean isFlying = false;
	private static boolean releasedSpaceInAir = false;
	
	private static int lastDRPCUpdate = 0;
	
	private static int loadX = -10;
	private static int loadY = -10;
	private static int loadZ = -10;
	
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
		//Walk Forward
		getInstance().camera.fieldOfView = 67;
		if (keys.contains(Input.Keys.W)) {
			int rotOffset = 0;
			int speed = keys.contains(Input.Keys.CONTROL_LEFT) ? 3 : 1;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)) * speed, player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset)) * speed), 0.1f);
			getInstance().camera.fieldOfView = keys.contains(Input.Keys.CONTROL_LEFT) ? 70 : 67;
		}
		//Walk Backwards
		if (keys.contains(Input.Keys.S)) {
			int rotOffset = 180;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		//Strafe Right
		if (keys.contains(Input.Keys.D)) {
			int rotOffset = 90;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		//Strafe Left
		if (keys.contains(Input.Keys.A)) {
			int rotOffset = -90;
			player.velocity.lerp(new Vector3((float) Math.sin(Math.toRadians(getInstance().camRotX - rotOffset)), player.velocity.y, (float) Math.cos(Math.toRadians(getInstance().camRotX - rotOffset))), 0.1f);
		}
		AtomicBoolean onGround = new AtomicBoolean(false);
		float playerHeight = 3;
		float playerWidth = 0.5f;
		float padding = 0.1f;
		
		float widthMinusPadding = playerWidth - padding;
		float widthPlusPadding = playerWidth + padding;
		float widthPlusOnePlusPadding = ((playerWidth + 1) + padding);
		
		try {
			getInstance().world.chunks.forEach((chunkPos, chunk) -> {
				for (BiObject<Block, BlockPos> block : chunk.getBlocks()) {
					if (block != null && block.getObj1() != null) {
						float wallCollisionYBottom = player.pos.y - (playerHeight - (1 + padding));
						float wallCollisionYTop = player.pos.y - (1 + padding);
						BoundingBox cb = block.getObj1().getCollisionBox(block.getObj2());
						//Floor Collision
						if (cb.intersects(new BoundingBox(
								new Vector3(player.pos.x + 0.5f - widthMinusPadding, player.pos.y - playerHeight, player.pos.z + 0.5f - widthMinusPadding),
								new Vector3(player.pos.x + 1.45f + widthMinusPadding, player.pos.y - (playerHeight / 2f), player.pos.z + 1.45f + widthMinusPadding)
						))) {
							player.pos.y += (cb.max.y - (player.pos.y - playerHeight));
							player.velocity.y = 0;
							onGround.set(true);
						}
						//Wall Collision -X
						if (cb.intersects(new BoundingBox(
								new Vector3(player.pos.x + 0.5f - widthPlusPadding, wallCollisionYBottom, player.pos.z + 0.5f - widthMinusPadding),
								new Vector3(player.pos.x + 0.5f, wallCollisionYTop, player.pos.z + 0.5f + widthMinusPadding)
						))) {
							player.pos.x += (cb.max.x - (player.pos.x + 0.5f - (widthPlusPadding + 0.01f)));
							player.velocity.x = 0;
						}
						//Wall Collision +X
						if (cb.intersects(new BoundingBox(
								new Vector3(player.pos.x + 0.5f, wallCollisionYBottom, player.pos.z + 0.5f - widthMinusPadding),
								new Vector3(player.pos.x + 1.5f + widthPlusPadding, wallCollisionYTop, player.pos.z + 0.5f + widthMinusPadding)
						))) {
							player.pos.x += (cb.min.x - (player.pos.x + 1.5f + (widthPlusPadding + 0.01f)));
							player.velocity.x = 0;
						}
						//Wall Collision -Z
						if (cb.intersects(new BoundingBox(
								new Vector3(player.pos.x + 0.5f - widthMinusPadding, wallCollisionYBottom, player.pos.z + 0.5f - widthPlusPadding),
								new Vector3(player.pos.x + 0.5f + widthMinusPadding, wallCollisionYTop, player.pos.z + 0.5f)
						))) {
							player.pos.z += (cb.max.z - (player.pos.z + 0.5f - (widthPlusPadding + 0.01f)));
							player.velocity.z = 0;
						}
						//Wall Collision +Z
						if (cb.intersects(new BoundingBox(
								new Vector3(player.pos.x + 0.5f - widthMinusPadding, wallCollisionYBottom, player.pos.z + 0.5f),
								new Vector3(player.pos.x + 1.5f + widthMinusPadding, wallCollisionYTop, player.pos.z + 1.5f + widthPlusPadding)
						))) {
							player.pos.z += (cb.min.z - (player.pos.z + 1.5f + (widthPlusPadding + 0.01f)));
							player.velocity.z = 0;
						}
					}
				}
			});
		} catch (Throwable ignored) {
		}
		
		//Terrain collision
		if (getInstance().world.terrainChunks != null) {
			getInstance().world.terrainChunks.forEach((pos, chunk) -> chunk.forEach(tri -> {
				if (chunk.pos.blockPos.distance(player.pos) <= Chunk.size * 4) {
					if (tri.collides(player.pos)) {
						float newY = Math.max(player.pos.y, tri.getCollisionPosY(player.pos));
						if (newY > player.pos.y) {
							player.velocity.y = Math.max(player.velocity.y, 0);
							player.pos.y = newY;
							player.onGround = true;
						}
					}
				}
			}));
		}
		
		if (keys.contains(Input.Keys.SPACE)) {
			if (player.onGround) {
				player.velocity.lerp(new Vector3(player.velocity.x, 0.25f, player.velocity.z), 1f);
				releasedSpaceInAir = false;
			} else if (releasedSpaceInAir) {
				isFlying = true;
				player.velocity.lerp(new Vector3(player.velocity.x, 0.25f, player.velocity.z), 1f);
			}
		} else {
			if (!player.onGround) {
				releasedSpaceInAir = true;
			}
		}
		if (player.onGround) {
			isFlying = false;
			releasedSpaceInAir = false;
			player.noGravity = false;
		} else if (isFlying) {
			player.noGravity = true;
		}
		if (keys.contains(Input.Keys.SHIFT_LEFT)) {
			if (isFlying) {
				player.velocity.lerp(new Vector3(player.velocity.x, -2.0f, player.velocity.z), 0.1f);
			}
		}
		if (keys.contains(Input.Keys.R)) {
			player.velocity.y = 0;
			player.pos.y = 12;
		}
		player.onGround = onGround.get();
//		player.pos.y = 5;
//		player.velocity.y = -0.1f;
		try {
			Thread.sleep(10);
		} catch (Throwable ignored) {
		}
		getInstance().dayTime++;
		
		if (lastDRPCUpdate >= 50) {
			lastDRPCUpdate = 0;
			if (RichPresence.isReady()) {
				RichPresence.update("In world", ".demo_save");
			}
		}
		
		ChunkPos pos = new ChunkPos(
				(int) player.pos.x / Chunk.size + loadX,
				(int) player.pos.y / Chunk.size + loadY,
				(int) player.pos.z / Chunk.size + loadZ
		);
		
		if (!getInstance().world.terrainChunks.containsKey(pos)) {
			getInstance().world.load(getInstance().file, pos);
		}
		
		ChunkPos pos1 = new ChunkPos(
				(int) (((int) player.pos.x / Chunk.size) - ((player.pos.x < 0) ? 1f : -0.5f)),
				((int) player.pos.y / Chunk.size),
				(int) (((int) player.pos.z / Chunk.size) - ((player.pos.z < 0) ? 1f : -0.5f))
		);
		
		if (!getInstance().world.terrainChunks.containsKey(pos1)) {
			getInstance().world.load(getInstance().file, pos1);
		}
		
		if (!getInstance().world.terrainChunks.containsKey(pos)) {
			boolean exists = false;
			for (int i = -10; i <= 10; i++) {
				ChunkPos pos2 = new ChunkPos(
						pos1.chunkX,
						i,
						pos1.chunkZ
				);
				if (getInstance().world.terrainChunks.containsKey(pos2)) {
					exists = true;
				}
				pos2 = new ChunkPos(
						pos1.chunkX,
						(pos2.chunkY + i),
						pos1.chunkZ
				);
				if (getInstance().world.terrainChunks.containsKey(pos2)) {
					exists = true;
				}
			}
			if (!exists) {
				getInstance().world.generate(pos1.chunkX, pos1.chunkZ, getInstance().getNoise());
			}
		}
		
		if (!getInstance().world.terrainChunks.containsKey(pos1)) {
			boolean exists = false;
			for (int i = -10; i <= 10; i++) {
				ChunkPos pos2 = new ChunkPos(
						pos.chunkX,
						i,
						pos.chunkZ
				);
				if (!getInstance().world.terrainChunks.containsKey(pos2)) {
					getInstance().world.load(getInstance().file, pos2);
				}
				if (getInstance().world.terrainChunks.containsKey(pos2)) {
					exists = true;
				}
			}
			if (!exists) {
				getInstance().world.generate(pos.chunkX, pos.chunkZ, getInstance().getNoise());
			}
		}
		
		loadX++;
		
		if (loadX >= 10) {
			loadX = -10;
			loadZ++;
		}
		if (loadY >= 10) {
			loadY = -10;
		}
		if (loadZ >= 10) {
			loadZ = -10;
			loadY++;
		}
		
		Object[] chunkPoses = getInstance().world.terrainChunks.keySet().toArray();
		Object[] chunkPoses1 = getInstance().world.chunks.keySet().toArray();
		for (Object o : chunkPoses) {
			ChunkPos posCheck = (ChunkPos) o;
			float xOff = Math.abs(posCheck.blockPos.x - player.pos.x);
			float yOff = Math.abs(posCheck.blockPos.y - player.pos.y);
			float zOff = Math.abs(posCheck.blockPos.z - player.pos.z);
			xOff /= Chunk.size;
			yOff /= (Chunk.size * 16);
			zOff /= Chunk.size;
			int off = (int) Math.max(xOff, Math.max(yOff, zOff));
			if (off >= 11) {
				getInstance().world.unload(getInstance().file, posCheck);
			}
		}
		for (Object o : chunkPoses1) {
			ChunkPos posCheck = (ChunkPos) o;
			float xOff = Math.abs(posCheck.blockPos.x - player.pos.x);
			float yOff = Math.abs(posCheck.blockPos.y - player.pos.y);
			float zOff = Math.abs(posCheck.blockPos.z - player.pos.z);
			xOff /= Chunk.size;
			yOff /= (Chunk.size * 16);
			zOff /= Chunk.size;
			int off = (int) Math.max(xOff, Math.max(yOff, zOff));
			if (off >= 11) {
				getInstance().world.unload(getInstance().file, posCheck);
			}
		}
		
		lastDRPCUpdate++;
	}
}
