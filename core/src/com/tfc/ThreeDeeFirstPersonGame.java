package com.tfc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.client.Main;
import com.tfc.entity.Player;
import com.tfc.events.EventBase;
import com.tfc.events.registry.Registry;
import com.tfc.model.Cube;
import com.tfc.registry.Blocks;
import com.tfc.registry.Textures;
import com.tfc.utils.Location;
import com.tfc.utils.TransformStack;
import com.tfc.world.ChunkPos;
import com.tfc.world.World;
import net.rgsw.ptg.noise.perlin.Perlin2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreeDeeFirstPersonGame extends ApplicationAdapter implements InputProcessor {
	public SpriteBatch batch2d;
	private static ThreeDeeFirstPersonGame INSTANCE;
	public ModelBuilder modelBuilder;
	
	public static ThreeDeeFirstPersonGame getInstance() {
		return INSTANCE;
	}
	
	public PerspectiveCamera camera;
	public Texture sand;
	public Texture hotbar;
	public Sprite sprite;
	public ModelBatch batch;
	public TransformStack stack;
	public Environment environment;
	public World world = new World();
	
	AtomicBoolean running = new AtomicBoolean(true);
	
	public Player player = new Player();
	
	private Thread logic = new Thread(new Runnable() {
		@Override
		public void run() {
			while (running.get()) {
				Main.tick(keys);
			}
		}
	});
	
	public static String namespace = "game";
	
	public final HashMap<ChunkPos, ModelInstance> chunkModels = new HashMap<>();
	
	private void register(EventBase eventBase) {
		Textures.register(new Location(namespace + ":sand"), new Texture("fine_sand.png"));
		Textures.register(new Location(namespace + ":stone"), new Texture("stone.png"));
		Textures.register(new Location(namespace + ":green_sand"), new Texture("green_sand.png"));
		Textures.register(new Location(namespace + ":sand_stone"), new Texture("sand_stone.png"));
		Textures.register(new Location(namespace + ":hotbar"), new Texture("hotbar_slot.png"));
		
		register("sand");
		register("stone");
		register("green_sand");
		register("sand_stone");
	}
	
	private static void register(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createModel(Textures.get(new Location(namespace + ":" + name)))));
	}
	
	private final ArrayList<Integer> keys = new ArrayList<>();
	
	@Override
	public void create() {
		INSTANCE = this;
		
		modelBuilder = new ModelBuilder();
		
		Registry registryEvent = (Registry) Objects.requireNonNull(EventBase.getOrCreateInstance(Registry.class));
		registryEvent.register(new Location(namespace + ":register"), this::register);
		Gdx.graphics.setTitle(namespace);
		Gdx.graphics.setVSync(true);
		
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.lookAt(0, 1, 0);
		camera.near = 1f;
		camera.far = 3000f;
		
		batch2d = new SpriteBatch();
		batch = new ModelBatch();
		stack = new TransformStack(batch2d);
		
		registryEvent.post();
		
		int size = 128;
		Perlin2D noise = new Perlin2D(new Random().nextInt(), 30, 30);
		for (int x = -size; x <= size; x++) {
			for (int z = -size; z <= size; z++) {
//				int yPos = Math.abs(z)==(size-2)?2:Math.abs(x)==(size-2)?2:0;
				int yPos = ((int) (noise.generate(x / 16f, z / 16f) * 32) + 64);
//				int y = yPos;
				for (int y = 0; y <= yPos; y++) {
					if (new Random().nextDouble() >= 0.75) {
						world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":sand")));
					} else if (new Random().nextDouble() >= 0.75) {
						world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":stone")));
					} else if (new Random().nextDouble() >= 0.75) {
						world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":green_sand")));
					} else {
						world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":sand_stone")));
					}
				}
			}
		}
		
		player.pos.y = 5;
		
		hotbar = Textures.get(new Location(namespace + ":hotbar"));
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
		
		sprite = new Sprite(hotbar);
		
		Gdx.input.setInputProcessor(this);
		
		logic.start();
	}
	
	@Override
	public void render() {
		camera.position.set(player.pos.x, player.pos.y, player.pos.z);
		camera.direction.set(0, -90, -1);
		camera.up.set(0, -90, 0);
		
		camRotY = Math.max(0, Math.min(180, camRotY));
		camera.rotate((float) -camRotY, 1, 0, 0);
		camera.rotate((float) camRotX, 0, 1, 0);
		
		camera.update();
		
		Gdx.gl.glClearColor(0, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		batch.begin(camera);
		world.chunks.forEach((chunkPos, chunk) -> {
			if (chunkModels.containsKey(chunkPos)) {
				batch.render(chunkModels.get(chunkPos));
			} else {
				ModelInstance instance = new ModelInstance(chunk.bake());
				batch.render(instance);
				chunkModels.put(chunkPos, instance);
			}
		});
		batch.end();
		
		batch2d.begin();
		for (int i = 0; i < 10; i++) {
			sprite.setScale(0.1f);
			if (slot == i) {
				sprite.setScale(0.11f);
			}
			sprite.setPosition((i - (2.5f)) * 48, -216);
			sprite.draw(batch2d);
		}
		batch2d.end();
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		batch2d.dispose();
		Textures.close();
		running.set(false);
	}
	
	public double camRotX = 0;
	public double camRotY = 45;
	public double camRotZ = 0;
	
	@Override
	public boolean keyDown(int keycode) {
		System.out.println(keycode);
		if (!keys.contains(keycode)) {
			keys.add(keycode);
		}
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (keys.contains(keycode)) {
			keys.remove((Object) keycode);
		}
		return false;
	}
	
	@Override
	public boolean keyTyped(char character) {
		return false;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}
	
	int slot = 0;
	
	@Override
	public boolean scrolled(int amount) {
		int num = amount / Math.abs(amount);
		slot -= num;
		if (slot < 0) {
			slot = 9;
		}
		if (slot > 9) {
			slot = 0;
		}
		return false;
	}
}
