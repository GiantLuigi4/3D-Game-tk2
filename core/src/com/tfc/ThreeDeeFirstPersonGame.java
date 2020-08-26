package com.tfc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.client.Main;
import com.tfc.entity.Player;
import com.tfc.events.EventBase;
import com.tfc.events.registry.Registry;
import com.tfc.events.render.RenderUI;
import com.tfc.model.Cube;
import com.tfc.registry.Blocks;
import com.tfc.registry.Textures;
import com.tfc.utils.BiObject;
import com.tfc.utils.Compression;
import com.tfc.utils.Location;
import com.tfc.utils.TransformStack;
import com.tfc.world.ChunkPos;
import com.tfc.world.World;
import net.rgsw.ptg.noise.perlin.Perlin2D;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreeDeeFirstPersonGame extends ApplicationAdapter implements InputProcessor {
	public SpriteBatch batch2d;
	private static ThreeDeeFirstPersonGame INSTANCE;
	public ModelBuilder modelBuilder;
	
	private static final RenderUI renderEvent = (RenderUI) Objects.requireNonNull(EventBase.getOrCreateInstance(RenderUI.class));
	
	public static ThreeDeeFirstPersonGame getInstance() {
		return INSTANCE;
	}
	
	public PerspectiveCamera camera;
	public Texture hotbar;
	private static String dir = System.getProperty("user.dir");
	public ModelBatch batch;
	public TransformStack stack;
	public Environment environment;
	public World world = new World();
	
	AtomicBoolean running = new AtomicBoolean(true);
	
	public Player player = new Player();
	public final HashMap<ChunkPos, BiObject<ModelBuilder, HashMap<BiObject<Integer, Material>, MeshBuilder>>> meshDatas = new HashMap<>();
	
	public static String namespace = "game";
	public Sprite spritehotbar;
	
	public final HashMap<ChunkPos, ModelInstance> chunkModels = new HashMap<>();
	int mx = 0;
	int my = 0;
	
	private static void register(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createModel(Textures.get(new Location(namespace + ":" + name)))));
	}
	
	private final ArrayList<Integer> keys = new ArrayList<>();
	boolean leftDown = false;
	private boolean ingame = false;
	private final Thread logic = new Thread(new Runnable() {
		@Override
		public void run() {
			while (running.get()) {
				if (ingame) {
					Main.tick(keys);
				}
			}
		}
	});
	
	private void register(EventBase eventBase) {
		Textures.register(new Location(namespace + ":sand"), new Texture("blocks\\fine_sand.png"));
		Textures.register(new Location(namespace + ":stone"), new Texture("blocks\\stone.png"));
		Textures.register(new Location(namespace + ":green_sand"), new Texture("blocks\\green_sand.png"));
		Textures.register(new Location(namespace + ":sand_stone"), new Texture("blocks\\sand_stone.png"));
		Textures.register(new Location(namespace + ":hotbar"), new Texture("ui\\ingame\\hotbar_slot.png"));
		Textures.register(new Location(namespace + ":button"), new Texture("ui\\menu\\button.png"));
		Textures.register(new Location(namespace + ":button_hovered"), new Texture("ui\\menu\\button_hovered.png"));
		
		register("sand");
		register("stone");
		register("green_sand");
		register("sand_stone");
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
	public void create() {
		INSTANCE = this;
		
		modelBuilder = new ModelBuilder();
		
		Registry registryEvent = (Registry) Objects.requireNonNull(EventBase.getOrCreateInstance(Registry.class));
		
		registryEvent.register(new Location(namespace + ":register"), this::register);
		renderEvent.register(new Location(namespace + ":ui"), this::renderUI);
		
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
		
		player.pos.y = 128;
		
		hotbar = Textures.get(new Location(namespace + ":hotbar"));
		
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
		
		spritehotbar = new Sprite(hotbar);
		
		Gdx.input.setInputProcessor(this);
		
		logic.start();
	}
	
	private void renderUI(EventBase eventBase) {
		RenderUI event = (RenderUI) eventBase;
		if (ingame) {
			for (int i = 0; i < 10; i++) {
				spritehotbar.setScale((480f / hotbar.getWidth()) * 0.1f, (480f / hotbar.getHeight()) * 0.1f);
				if (slot == i) {
					spritehotbar.setScale((480f / hotbar.getWidth()) * 0.11f, (480f / hotbar.getHeight()) * 0.11f);
				}
				spritehotbar.setPosition((i - (2.5f)) * 48, -216);
				spritehotbar.draw(event.getBatch());
			}
		} else {
			Texture texture;
			float percentX = mx / (float) Gdx.graphics.getWidth();
			float percentY = my / (float) Gdx.graphics.getHeight();
			if (percentX >= 0.405 && percentX <= 0.605 && percentY >= 0.4075 && percentY <= 0.5425) {
				texture = Textures.get(new Location(namespace + ":button_hovered"));
				if (leftDown) {
					ingame = true;
					world = new World();
					
					File file = new File(dir + "\\saves\\.demo_save");
					if (!file.exists()) {
						try {
							file.mkdirs();
							int size = 6;
							Perlin2D noise = new Perlin2D(new Random().nextInt(), 30, 30);
							for (int x = -size; x <= size; x++) {
								for (int z = -size; z <= size; z++) {
									int yPos = Math.abs(z) == (size - 2) ? 2 : Math.abs(x) == (size - 2) ? 2 : 0;
//									int yPos = ((int) (noise.generate(x / 16f, z / 16f) * 32) + 16);
//									int y = yPos;
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
						} catch (Throwable ignored) {
						}
						
						
						StringBuilder saveDataBuilder = new StringBuilder();
						world.chunks.forEach((chunkPos, chunk) -> {
							for (BiObject<Block, BlockPos> block : chunk.getBlocks()) {
								if (block != null) {
									saveDataBuilder
											.append(block.getObj1().getName().toString())
											.append(',').append(block.getObj2().x)
											.append(',').append(block.getObj2().y)
											.append(',').append(block.getObj2().z)
											.append('\n');
								}
							}
						});
						
						String saveData = saveDataBuilder.toString();
						System.out.println(Compression.makeIllegible(Compression.compress(saveData)));
						System.out.println(Compression.makeLegible(Compression.makeIllegible(Compression.compress(saveData))));
						File file1 = new File(dir + "\\saves\\.demo_save\\save.data");
						
						try {
							file1.createNewFile();
							FileWriter writer = new FileWriter(file1);
							writer.write(Compression.makeIllegible(Compression.compress(saveData)));
							writer.close();
						} catch (Throwable ignored) {
						}
						try {
							File player = new File(dir + "\\saves\\.demo_save\\players\\player1.data");
							player.getParentFile().mkdirs();
							player.createNewFile();
							FileWriter writer = new FileWriter(player);
							writer.write(Compression.makeIllegible(Compression.compress("pos:0,128,0")));
							writer.close();
						} catch (Throwable ignored) {
						}
					} else {
						try {
							File file1 = new File(dir + "\\saves\\.demo_save\\save.data");
							Scanner sc = new Scanner(file1);
							StringBuilder builder = new StringBuilder();
							while (sc.hasNextLine()) {
								builder.append(sc.nextLine()).append('\n');
							}
							sc.close();
							String saveData = Compression.decompress(Compression.makeLegible(builder.toString()));
//							System.out.println(saveData);
							for (String s : saveData.split("\n")) {
								String[] strings = s.split(",");
								try {
									Block block = Blocks.get(new Location(strings[0].replace(",", "")));
									BlockPos pos = new BlockPos(
											Integer.parseInt(strings[1].replace(",", "")),
											Integer.parseInt(strings[2].replace(",", "")),
											Integer.parseInt(strings[3].replace(",", ""))
									);
									world.setBlock(pos, block);
								} catch (Throwable err) {
									err.printStackTrace();
								}
							}
						} catch (Throwable ignored) {
						}
					}
				}
			} else {
				texture = Textures.get(new Location(namespace + ":button"));
			}
			event.getBatch().draw(
					texture,
					260, 220,
					128, 64
			);
		}
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
		if (ingame) {
			AtomicInteger bakedInFrame = new AtomicInteger(0);
			world.chunks.forEach((chunkPos, chunk) -> {
				if (chunkModels.containsKey(chunkPos)) {
					batch.render(chunkModels.get(chunkPos));
				} else {
					if (meshDatas.containsKey(chunkPos)) {
						chunkModels.put(chunkPos, new ModelInstance(chunk.bake(meshDatas.get(chunkPos))));
					} else if (bakedInFrame.get() < 1) {
						ModelInstance instance = new ModelInstance(chunk.bake(chunk.createMesh()));
						batch.render(instance);
						chunkModels.put(chunkPos, instance);
						bakedInFrame.getAndAdd(1);
					}
				}
			});
		}
		batch.end();
		
		batch2d.begin();
		renderEvent.post(batch2d);
		batch2d.end();
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
		mx = screenX;
		my = screenY;
		if (button == Input.Buttons.LEFT) {
			leftDown = true;
		}
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		mx = screenX;
		my = screenY;
		if (button == Input.Buttons.LEFT) {
			leftDown = false;
		}
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		mx = screenX;
		my = screenY;
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		mx = screenX;
		my = screenY;
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
