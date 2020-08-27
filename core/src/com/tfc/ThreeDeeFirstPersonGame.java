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
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.tfc.blocks.Block;
import com.tfc.blocks.BlockPos;
import com.tfc.client.Main;
import com.tfc.entity.Player;
import com.tfc.events.EventBase;
import com.tfc.events.registry.Registry;
import com.tfc.events.render.RenderUI;
import com.tfc.flame.FlameConfig;
import com.tfc.model.Cube;
import com.tfc.registry.Blocks;
import com.tfc.registry.Textures;
import com.tfc.utils.*;
import com.tfc.world.Chunk;
import com.tfc.world.ChunkPos;
import com.tfc.world.World;
import net.rgsw.ptg.noise.perlin.Perlin2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
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
	boolean rightDown = false;
	
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
	
	private ModelInstance boundingBox = null;
	
	private void register(EventBase eventBase) {
		Textures.register(new Location(namespace + ":sand"), new Texture("assets\\blocks\\fine_sand.png"));
		Textures.register(new Location(namespace + ":stone"), new Texture("assets\\blocks\\stone.png"));
		Textures.register(new Location(namespace + ":green_sand"), new Texture("assets\\blocks\\green_sand.png"));
		Textures.register(new Location(namespace + ":sand_stone"), new Texture("assets\\blocks\\sand_stone.png"));
		Textures.register(new Location(namespace + ":grass"), new Texture("assets\\blocks\\grass.png"));
		Textures.register(new Location(namespace + ":bounding_box"), new Texture("assets\\ui\\bounding_box\\black_border.png"));
		Textures.register(new Location(namespace + ":hotbar"), new Texture("assets\\ui\\ingame\\hotbar_slot.png"));
		Textures.register(new Location(namespace + ":button"), new Texture("assets\\ui\\menu\\button.png"));
		Textures.register(new Location(namespace + ":button_hovered"), new Texture("assets\\ui\\menu\\button_hovered.png"));
		
		register("sand");
		register("stone");
		register("green_sand");
		register("sand_stone");
		register("grass");
		
		boundingBox = Cube.createModel(Textures.get(new Location(namespace + ":bounding_box")));
		
		//https://stackoverflow.com/questions/19112349/libgdx-3d-texture-transparency
		boundingBox.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
	}
	
	@Override
	public void dispose() {
		batch.dispose();
		batch2d.dispose();
		Textures.close();
		running.set(false);
		
		try {
			if (ingame) {
				File player = new File(dir + "\\saves\\.demo_save\\players\\player1.data");
				player.getParentFile().mkdirs();
				player.createNewFile();
				FileOutputStream writer = new FileOutputStream(player);
				String text1 = "pos:" + this.player.pos.x + "," + this.player.pos.y + "," + this.player.pos.z + "\n";
				text1 += "rot:" + camRotX + "," + camRotY + "," + camRotZ + "\n";
				String text = Compression.makeIllegible(Compression.compress(text1));
				char[] chars = text.toCharArray();
				byte[] bytes = new byte[chars.length];
				for (int i = 0; i < chars.length; i++) {
					bytes[i] = (byte) chars[i];
				}
				writer.write(bytes);
				writer.close();
			}
			
			StringBuilder builder = new StringBuilder();
			for (Chunk chunk : world.chunks.values()) {
				for (BiObject<Block, BlockPos> block : chunk.getBlocks()) {
					if (block != null && block.getObj1() != null) {
						builder
								.append(block.getObj1().getName())
								.append(',').append(block.getObj2().x)
								.append(',').append(block.getObj2().y)
								.append(',').append(block.getObj2().z)
								.append('\n')
						;
					}
				}
			}
			String save = Compression.makeIllegible(Compression.compress(builder.toString()));
//			System.out.println(save);
			File saveF = new File(dir + "\\saves\\.demo_save\\save.data");
			FileOutputStream writer = new FileOutputStream(saveF);
			byte[] bytes = new byte[save.length()];
			char[] chars = save.toCharArray();
			for (int i = 0; i < save.length(); i++) {
				bytes[i] = (byte) chars[i];
			}
			writer.write(bytes);
			writer.close();
		} catch (Throwable ignored) {
		}
		
		try {
			File log = new File(dir + "\\logs\\" + "game " + new SimpleDateFormat("yyyy-MM-dd. hh:mm:ss").format(new Date()) + ".log");
			log.getParentFile().mkdirs();
			log.createNewFile();
			FileWriter writer = new FileWriter(log);
			writer.write(FlameConfig.field.getText());
			writer.close();
		} catch (Throwable ignored) {
		}
	}
	
	
	public double camRotX = 0;
	public double camRotY = 45;
	public double camRotZ = 0;
	
	@Override
	public boolean keyDown(int keycode) {
//		System.out.println(keycode);
		if (!keys.contains(keycode)) {
			keys.add(keycode);
		}
		return false;
	}
	
	@Override
	public void create() {
		try {
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
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
		
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
				if (i < Blocks.count()) {
					Block block = Blocks.getByID(i);
					Texture texture = Textures.get(block.getName());
					int size = 36;
					batch2d.draw(texture, 102 + ((size + 12) * i), 6, size, size, 0, 0, texture.getWidth(), texture.getHeight());
				}
			}
		} else {
			Texture texture;
			float percentX = mx / (float) Gdx.graphics.getWidth();
			float percentY = my / (float) Gdx.graphics.getHeight();
			if (percentX >= 0.405 && percentX <= 0.605 && percentY >= 0.4075 && percentY <= 0.5425) {
				texture = Textures.get(new Location(namespace + ":button_hovered"));
				if (leftDown) {
					leftDown = false;
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
//						System.out.println(Compression.makeIllegible(Compression.compress(saveData)));
//						System.out.println(Compression.makeLegible(Compression.makeIllegible(Compression.compress(saveData))));
						File file1 = new File(dir + "\\saves\\.demo_save\\save.data");
						
						try {
							file1.createNewFile();
							FileOutputStream writer = new FileOutputStream(file1);
							String text = Compression.makeIllegible(Compression.compress(saveData));
							char[] chars = text.toCharArray();
							byte[] bytes = new byte[chars.length];
							for (int i = 0; i < chars.length; i++) {
								bytes[i] = (byte) chars[i];
							}
							writer.write(bytes);
							writer.close();
						} catch (Throwable ignored) {
						}
						try {
							File player = new File(dir + "\\saves\\.demo_save\\players\\player1.data");
							player.getParentFile().mkdirs();
							player.createNewFile();
							FileOutputStream writer = new FileOutputStream(player);
							String text = Compression.makeIllegible(Compression.compress("pos:0,128,0"));
							char[] chars = text.toCharArray();
							byte[] bytes = new byte[chars.length];
							for (int i = 0; i < chars.length; i++) {
								bytes[i] = (byte) chars[i];
							}
							writer.write(bytes);
							writer.close();
						} catch (Throwable ignored) {
						}
					} else {
						try {
							File file1 = new File(dir + "\\saves\\.demo_save\\save.data");
							File file2 = new File(dir + "\\saves\\.demo_save\\players\\player1.data");
							FileInputStream input = new FileInputStream(file1);
							FileInputStream input2 = new FileInputStream(file2);
							StringBuilder builder = new StringBuilder();
							StringBuilder builder2 = new StringBuilder();
							byte[] bytes = new byte[input.available()];
							byte[] bytes2 = new byte[input2.available()];
							input.read(bytes);
							input2.read(bytes2);
							for (byte b : bytes) {
								builder.append((char) b);
							}
							for (byte b : bytes2) {
								builder2.append((char) b);
							}
							input.close();
							input2.close();
							String saveData = Compression.decompress(Compression.makeLegible(builder.toString()));
							String playerData = Compression.decompress(Compression.makeLegible(builder2.toString()));
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
									Logger.logErrFull(err);
								}
							}
							for (String s : playerData.split("\n")) {
								String[] strings = s.split(",");
								try {
									if (strings[0].startsWith("pos:")) {
										player.pos.x = Float.parseFloat(strings[0].replace("pos:", ""));
										player.pos.y = Float.parseFloat(strings[1]);
										player.pos.z = Float.parseFloat(strings[2]);
									} else if (strings[0].startsWith("rot:")) {
										camRotX = Double.parseDouble(strings[0].replace("rot:", ""));
										camRotY = Double.parseDouble(strings[1]);
										camRotZ = Double.parseDouble(strings[2]);
									}
								} catch (Throwable err) {
									Logger.logErrFull(err);
								}
							}
						} catch (Throwable ignored) {
						}
					}
					
					ingame = true;
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
				if (world.needsRefresh.contains(chunkPos)) {
					chunkModels.remove(chunkPos);
					meshDatas.remove(chunkPos);
				}
				if (chunkModels.containsKey(chunkPos)) {
					batch.render(chunkModels.get(chunkPos));
				} else {
					if (meshDatas.containsKey(chunkPos)) {
						chunkModels.put(chunkPos, new ModelInstance(chunk.bake(meshDatas.get(chunkPos))));
					} else if (bakedInFrame.get() < 16) {
						ModelInstance instance = new ModelInstance(chunk.bake(chunk.createMesh()));
						batch.render(instance);
						chunkModels.put(chunkPos, instance);
						bakedInFrame.getAndAdd(1);
					}
				}
			});
			world.needsRefresh.clear();
			Vector3 pos = new Vector3(player.pos.x, player.pos.y, player.pos.z);
			for (int i = 0; i < 16; i += 1) {
				pos = pos.add(camera.direction.nor());
//				BlockPos pos1 = new BlockPos(
//						(int)Math.ceil(pos.x)/2,
//						(int)Math.ceil(pos.y)/2,
//						(int)Math.ceil(pos.z)/2
//				);
				BlockPos pos2 = new BlockPos(
						Math.round(pos.x) / 2,
						Math.round(pos.y) / 2,
						Math.round(pos.z) / 2
				);
//				BlockPos pos3 = new BlockPos(
//						(int)Math.floor(pos.x)/2,
//						(int)Math.floor(pos.y)/2,
//						(int)Math.floor(pos.z)/2
//				);
				if (world.hasChunk(pos2)
//						|| world.hasChunk(pos1) || world.hasChunk(pos3)
				) {
					Block block = world.getBlock(pos2);
//					if (block == null) {
//						block = world.getBlock(pos1);
//						pos2 = pos1;
//					}
//					if (block == null) {
//						block = world.getBlock(pos3);
//						pos2 = pos3;
//					}
					if (block != null) {
						if (slot < Blocks.count()) {
							Vector3 posLoc = new Vector3(pos2.x, pos2.y, pos2.z);
							pos = posLoc.add(pos.sub(posLoc)).sub(camera.direction.nor());
							Block place = Blocks.getByID(slot);
//							ModelInstance instance = place.modelInstance;
							float scale = 1;
							float undoScale = 1 / scale;
							boundingBox.transform.scale(scale, scale, scale);
							boundingBox.transform.setTranslation(
									pos2.x * 2,
									pos2.y * 2,
									pos2.z * 2
							);
							batch.render(boundingBox);
							boundingBox.transform.scale(undoScale, undoScale, undoScale);
							if (leftDown) {
								pos = pos.sub(camera.direction.nor());
								world.setBlock(new BlockPos(
										Math.round(pos.x / 2),
										Math.round(pos.y / 2),
										Math.round(pos.z / 2)
								), place);
								leftDown = false;
							} else if (rightDown) {
								pos = new Vector3(pos2.x, pos2.y, pos2.z);
								world.setBlock(new BlockPos(
										Math.round(pos.x),
										Math.round(pos.y),
										Math.round(pos.z)
								), null);
								rightDown = false;
							}
						}
						break;
					}
				}
			}
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
		} else if (button == Input.Buttons.RIGHT) {
			rightDown = true;
		}
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		mx = screenX;
		my = screenY;
		if (button == Input.Buttons.LEFT) {
			leftDown = false;
		} else if (button == Input.Buttons.RIGHT) {
			rightDown = false;
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
