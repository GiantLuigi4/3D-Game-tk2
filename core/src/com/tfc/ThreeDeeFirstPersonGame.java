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
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
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
import com.tfc.model.Triangle;
import com.tfc.registry.Blocks;
import com.tfc.registry.Textures;
import com.tfc.utils.*;
import com.tfc.world.World;
import com.tfc.world.chunks.Chunk;
import com.tfc.world.chunks.ChunkPos;
import com.tfc.world.chunks.TerrainChunk;
import net.rgsw.ptg.noise.perlin.Perlin2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
	public static final String dir = System.getProperty("user.dir");
	public ModelBatch batch;
	public TransformStack stack;
	private static final DirectionalLight sunLight = new DirectionalLight();
	public World world = new World();
	
	AtomicBoolean running = new AtomicBoolean(true);
	
	public Player player = new Player();
	public final HashMap<ChunkPos, BiObject<ModelBuilder, HashMap<BiObject<Integer, Material>, MeshBuilder>>> meshDatas = new HashMap<>();
	
	public static final String namespace = "game";
	public Sprite spritehotbar;
	
	public final HashMap<ChunkPos, ModelInstance> chunkModels = new HashMap<>();
	public Environment environmentSurface;
	int mx = 0;
	int my = 0;
	public long dayTime = 0;
	public int seed = 0;
	
	private static void register(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createModel(Textures.get(new Location(namespace + ":" + name)))));
	}
	
	private static void registerTransparent(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createTransparentModel(Textures.get(new Location(namespace + ":" + name)))));
	}
	
	private final ArrayList<Integer> keys = new ArrayList<>();
	
	boolean leftDown = false;
	boolean rightDown = false;
	
	private boolean ingame = false;
	public static SpriteMap sprites = new SpriteMap();
	
	private final Vector3 lastPos = new Vector3();
	
	private final AtomicLong lastTick = new AtomicLong();
	private final Thread logic = new Thread(() -> {
		while (running.get()) {
			if (ingame) {
				try {
					Main.tick(keys);
					lastTick.set(new Date().getTime());
					lastPos.set(player.pos);
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
			}
		}
	});
	private ModelInstance boundingBox = null;
	Perlin2D noise;
	
	
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
	
	private ModelInstance instance;
	
	private void register(EventBase eventBase) {
		Textures.register(new Location(namespace + ":sand"), new Texture("assets\\blocks\\fine_sand.png"));
		Textures.register(new Location(namespace + ":stone"), new Texture("assets\\blocks\\stone.png"));
		Textures.register(new Location(namespace + ":green_sand"), new Texture("assets\\blocks\\green_sand.png"));
		Textures.register(new Location(namespace + ":sand_stone"), new Texture("assets\\blocks\\sand_stone.png"));
		Textures.register(new Location(namespace + ":grass"), new Texture("assets\\blocks\\grass.png"));
		Textures.register(new Location(namespace + ":glass"), new Texture("assets\\blocks\\glass.png"));
		Textures.register(new Location(namespace + ":debug_one"), new Texture("assets\\blocks\\debug\\debug1.png"));
		Textures.register(new Location(namespace + ":bounding_box"), new Texture("assets\\ui\\bounding_box\\black_border.png"));
		Textures.register(new Location(namespace + ":hotbar"), new Texture("assets\\ui\\ingame\\hotbar_slot.png"));
		Textures.register(new Location(namespace + ":button"), new Texture("assets\\ui\\menu\\button.png"));
		Textures.register(new Location(namespace + ":button_hovered"), new Texture("assets\\ui\\menu\\button_hovered.png"));
		
		register("sand");
		register("stone");
		register("green_sand");
		register("sand_stone");
		register("grass");
		registerTransparent("glass");
		
		instance = Triangle.createTriangle(
				new Vector3(0, 0, 0),
				new Vector3(0, 0, 1),
				new Vector3(0, 1, 1),
				Textures.get(new Location(namespace + ":stone"))
		);
		
		boundingBox = Cube.createModel(Textures.get(new Location(namespace + ":bounding_box")));
		
		//https://stackoverflow.com/questions/19112349/libgdx-3d-texture-transparency
		boundingBox.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
	}
	
	public final HashMap<ChunkPos, ModelInstance> terrainChunkModels = new HashMap<>();
	
	@Override
	public void create() {
		try {
			
			File vert = new File(dir + "\\shaders\\vert.glsl");
			File frag = new File(dir + "\\shaders\\frag.glsl");
			
			if (!vert.exists()) {
				vert.getParentFile().mkdirs();
				vert.createNewFile();
				FileWriter writer = new FileWriter(vert);
				//TODO: learn glsl
				writer.write("gdx");
				writer.close();
			}
			if (!frag.exists()) {
				frag.getParentFile().mkdirs();
				frag.createNewFile();
				FileWriter writer = new FileWriter(frag);
				//TODO: learn glsl
				writer.write("gdx");
				writer.close();
			}
			
			StringBuilder vertText = new StringBuilder();
			if (vert.exists()) {
				Scanner sc = new Scanner(vert);
				while (sc.hasNextLine()) {
					vertText.append(sc.nextLine());
				}
				sc.close();
			}
			StringBuilder fragText = new StringBuilder();
			if (vert.exists()) {
				Scanner sc = new Scanner(frag);
				while (sc.hasNextLine()) {
					fragText.append(sc.nextLine());
				}
				sc.close();
			}
			
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
			if (vertText.toString().equals("gdx") && fragText.toString().equals("gdx")) {
				batch = new ModelBatch();
			} else {
				System.out.println(vertText.toString());
				System.out.println(fragText.toString());
				batch = new ModelBatch(vertText.toString(), fragText.toString());
			}
			stack = new TransformStack(batch2d);
			
			registryEvent.post();
			
			player.pos.y = 128;
			
			hotbar = Textures.get(new Location(namespace + ":hotbar"));
			
			environmentSurface = new Environment();
			environmentSurface.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
			
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
//				spritehotbar.setScale((480f / hotbar.getWidth()) * 0.1f, (480f / hotbar.getHeight()) * 0.1f);
//				if (slot == i) {
//					spritehotbar.setScale((480f / hotbar.getWidth()) * 0.11f, (480f / hotbar.getHeight()) * 0.11f);
//				}
				float offXSelected = 1.9575f;
				float offXNormal = 2;
				spritehotbar.setSize(slot == i ? 52 : 48, slot == i ? 52 : 48);
				spritehotbar.setPosition((i + (slot == i ? offXSelected : offXNormal)) * 48, (2 - (slot == i ? 4.5f : 2)));
				spritehotbar.draw(event.getBatch());
				if (i < Blocks.count()) {
					Block block = Blocks.getByID(i);
					int size = 36;
					Sprite sprite = sprites.getOrCreate(block.getName());
					sprite.setPosition(102 + (i * (size + 12)), 6);
					sprite.setSize(size, size);
					sprite.draw(event.getBatch());
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
						createWorld(file);
					} else {
						loadWorld();
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
	
	int scaleX = 30;
	int scaleY = 30;
	
	@Override
	public void dispose() {
		batch.dispose();
		batch2d.dispose();
		Textures.close();
		running.set(false);
		
		if (ingame) {
			try {
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
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
			
			try {
				//https://www.codejava.net/java-se/file-io/how-to-compress-files-in-zip-format-in-java#:~:text=Here%20are%20the%20steps%20to,ZipEntry)%20method%20on%20the%20ZipOutputStream.
				File zfile = new File(dir + "\\saves\\.demo_save\\chunks.zip");
				if (!zfile.exists()) {
					zfile.createNewFile();
				}
				FileOutputStream stream = new FileOutputStream(zfile);
				ZipOutputStream fileSave = new ZipOutputStream(stream);
				for (Chunk chunk : world.chunks.values()) {
					String pos = chunk.pos.chunkX + "," + chunk.pos.chunkY + "," + chunk.pos.chunkZ;
					fileSave.putNextEntry(new ZipEntry(pos + ".data"));
					char[] chars = Compression.deQuadruple(Compression.makeIllegible(Compression.compress(chunk.toString()))).toCharArray();
					byte[] bytes = new byte[chars.length];
					for (int i = 0; i < chars.length; i++) {
						bytes[i] = (byte) chars[i];
					}
					fileSave.write(bytes);
					fileSave.closeEntry();
				}
				fileSave.close();
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
			
			try {
				//https://www.codejava.net/java-se/file-io/how-to-compress-files-in-zip-format-in-java#:~:text=Here%20are%20the%20steps%20to,ZipEntry)%20method%20on%20the%20ZipOutputStream.
				File zfile = new File(dir + "\\saves\\.demo_save\\terrain.zip");
				if (!zfile.exists()) {
					zfile.createNewFile();
				}
				FileOutputStream stream = new FileOutputStream(zfile);
				ZipOutputStream fileSave = new ZipOutputStream(stream);
				for (TerrainChunk chunk : world.terrainChunks.values()) {
					String pos = chunk.pos.chunkX + "," + chunk.pos.chunkY + "," + chunk.pos.chunkZ;
					fileSave.putNextEntry(new ZipEntry(pos + ".data"));
					char[] chars = Compression.deQuadruple(Compression.makeIllegible(Compression.compress(chunk.toString()))).toCharArray();
					byte[] bytes = new byte[chars.length];
					for (int i = 0; i < chars.length; i++) {
						bytes[i] = (byte) chars[i];
					}
					fileSave.write(bytes);
					fileSave.closeEntry();
				}
				fileSave.close();
			} catch (Throwable err) {
				Logger.logErrFull(err);
			}
			Files.createFile("saves\\.demo_save\\level.properties",
					Compression.makeIllegible(Compression.deQuadruple(Compression.compress(
							"//I guess you can tamper with this (for now)" + "\n" +
									"seed:" + seed + "\n" +
									"scaleX:" + scaleX + "\n" +
									"scaleY:" + scaleY + "\n" +
									"createNoise\n" +
									"time:" + dayTime + "\n"
					)))
			);
		}
		
		try {
			File log = new File(dir + "\\logs\\" + "game " + new SimpleDateFormat("yyyy-MM-dd. hh:mm:ss").format(new Date()).replace(":", "'") + ".log");
			log.getParentFile().mkdirs();
			log.createNewFile();
			FileWriter writer = new FileWriter(log);
			writer.write(FlameConfig.field.getText());
			writer.close();
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	private void loadWorld() {
		try {
			File file1 = new File(dir + "\\saves\\.demo_save\\chunks.zip");
			File file2 = new File(dir + "\\saves\\.demo_save\\players\\player1.data");
			File file3 = new File(dir + "\\saves\\.demo_save\\terrain.zip");
			FileInputStream input2 = new FileInputStream(file2);
			StringBuilder builder2 = new StringBuilder();
			byte[] bytes2 = new byte[input2.available()];
			input2.read(bytes2);
			for (byte b : bytes2) {
				builder2.append((char) b);
			}
			input2.close();
			world.loadAll(file1);
			String playerData = Compression.decompress(Compression.makeLegible(builder2.toString()));
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
			world.loadTerrainChunks(file3);
			String level = Compression.decompress(Compression.reQuadruple(Compression.makeLegible(Files.read("saves\\.demo_save\\level.data"))));
			for (String line : level.split("\n")) {
				if (line.startsWith("seed:")) {
					seed = Integer.parseInt(line.replace("seed:", ""));
				} else if (line.startsWith("time:")) {
					dayTime = Long.parseLong(line.replace("time:", ""));
				} else if (line.startsWith("scaleX:")) {
					scaleX = Integer.parseInt(line.replace("scaleX:", ""));
				} else if (line.startsWith("scaleY:")) {
					scaleY = Integer.parseInt(line.replace("scaleY:", ""));
				} else if (line.startsWith("createNoise")) {
					noise = new Perlin2D(seed, scaleX, scaleY);
				}
			}
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	private void createWorld(File file) {
		try {
			file.mkdirs();
			int size = 16;
			seed = new Random().nextInt();
			scaleX = new Random(seed).nextInt(16) + 16;
			scaleY = new Random(seed * 2).nextInt(16) + 16;
			noise = new Perlin2D(seed, scaleX, scaleY);
			for (int x = -size; x <= size; x++) {
				for (int z = -size; z <= size; z++) {
					world.generate(x, z, noise);

//					int yPos = Math.abs(z) == (size - 2) ? 2 : Math.abs(x) == (size - 2) ? 2 : 0;
//					for (int y = 0; y <= yPos; y++) {
//						if (new Random().nextDouble() >= 0.75) {
//							world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":sand")));
//						} else if (new Random().nextDouble() >= 0.75) {
//							world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":stone")));
//						} else if (new Random().nextDouble() >= 0.75) {
//							world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":green_sand")));
//						} else {
//							world.setBlock(new BlockPos(x, y, z), Blocks.get(new Location(namespace + ":sand_stone")));
//						}
//					}
				}
			}
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}

//		StringBuilder terrainS = new StringBuilder();
//		for (TerrainTriangle tri : terrain) {
//			terrainS.append(tri.toString()).append("\n");
//		}
//
//		File fileT = new File(dir + "\\saves\\.demo_save\\terrain.data");

//		try {
//			fileT.createNewFile();
//			FileOutputStream writer2 = new FileOutputStream(fileT);
//			String text2 = Compression.makeIllegible((terrainS.toString()));
//			char[] chars2 = text2.toCharArray();
//			byte[] bytes2 = new byte[chars2.length];
//			for (int i = 0; i < chars2.length; i++) {
//				bytes2[i] = (byte) chars2[i];
//			}
//			writer2.write(bytes2);
//			writer2.close();
//		} catch (Throwable err) {
//			Logger.logErrFull(err);
//		}
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
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	@Override
	public void render() {
		try {
			environmentSurface.remove(sunLight);
			sunLight.setDirection(new Vector3(
					(float) Math.cos(Math.toRadians((dayTime / 10f) % 360)) / 2f,
					(float) Math.sin(Math.toRadians((dayTime / 10f) % 360)),
					(float) Math.cos(Math.toRadians((dayTime / 10f) % 360)) / 4f
			).nor());
			final float brightness = 0.5f;
			environmentSurface.add(sunLight.setColor(brightness, brightness, brightness, 1f));
			
			camera.direction.set(0, -90, -1);
			camera.up.set(0, -90, 0);
			
			camRotY = Math.max(0, Math.min(180, camRotY));
			camera.rotate((float) -camRotY, 1, 0, 0);
			camera.rotate((float) camRotX, 0, 1, 0);
			
			camera.update();
			
			Vector3 lerped = lastPos.lerp(player.pos,Math.max(0,Math.min(1,1-(lastTick.get()/10f))));
			Vector3 offset = new Vector3(-lerped.x, -lerped.y, -lerped.z);
			
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
						chunkModels.get(chunkPos).transform.setTranslation(offset);
						batch.render(chunkModels.get(chunkPos), environmentSurface);
					} else {
						if (meshDatas.containsKey(chunkPos)) {
							chunkModels.put(chunkPos, new ModelInstance(chunk.bake(meshDatas.get(chunkPos))));
						} else if (bakedInFrame.get() < 16) {
							ModelInstance instance = new ModelInstance(chunk.bake(chunk.createMesh()));
							batch.render(instance);
							chunkModels.put(chunkPos, instance);
							batch.render(chunkModels.get(chunkPos), environmentSurface);
							bakedInFrame.getAndAdd(1);
						}
					}
				});
				world.needsRefresh.clear();

//				world.terrainChunks.forEach((pos, chunk) -> chunk.forEach(triangle -> triangle.draw(batch, environment, offset)));
				world.terrainChunks.forEach((pos, chunk) -> {
					if (!terrainChunkModels.containsKey(pos)) {
						ModelInstance instance = chunk.bake();
						terrainChunkModels.put(pos, instance);
					}
					ModelInstance instance = terrainChunkModels.get(pos);
					instance.transform.setTranslation(offset);
					batch.render(instance, environmentSurface);
				});
				
				Vector3 pos = new Vector3(player.pos.x / 2, player.pos.y / 2, player.pos.z / 2);
				pos = pos.add(player.pos.x <= 0 ? -0.5f : 0.5f, 0.5f, player.pos.z <= 0 ? -0.5f : 0.5f);
				for (float i = 0; i < 16; i += 0.01f) {
					pos = pos.add(camera.direction.nor().scl(0.01f));
					BlockPos pos2 = new BlockPos(pos);
					if (world.hasChunk(pos2)) {
						Block block = world.getBlock(pos2);
						if (block != null) {
							if (slot < Blocks.count()) {
								Vector3 posLoc = new Vector3(pos2.x, pos2.y, pos2.z);
								pos = posLoc.add(pos.sub(posLoc)).sub(camera.direction.nor());
								Block place = Blocks.getByID(slot);
								float scale = 1;
								float undoScale = 1 / scale;
								boundingBox.transform.scale(scale, scale, scale);
								boundingBox.transform.setTranslation(
										pos2.x * 2,
										pos2.y * 2,
										pos2.z * 2
								);
								boundingBox.transform.translate(offset);
								batch.render(boundingBox, environmentSurface);
								boundingBox.transform.scale(undoScale, undoScale, undoScale);
								if (leftDown) {
									for (float be = 0; be <= 2; be += 0.1f) {
										Vector3 posPlusX = new Vector3(pos.x + be, pos.y, pos.z);
										Vector3 posMinusX = new Vector3(pos.x - be, pos.y, pos.z);
										Vector3 posPlusY = new Vector3(pos.x, pos.y + be, pos.z);
										Vector3 posMinusY = new Vector3(pos.x, pos.y - be, pos.z);
										Vector3 posPlusZ = new Vector3(pos.x, pos.y, pos.z + be);
										Vector3 posMinusZ = new Vector3(pos.x, pos.y, pos.z - be);
										Block block1 = world.getBlock(new BlockPos(posPlusX));
										if (block1==null) {
											world.setBlock(new BlockPos(posPlusX), place);
											break;
										}
										block1 = world.getBlock(new BlockPos(posMinusX));
										if (block1==null) {
											world.setBlock(new BlockPos(posMinusX), place);
											break;
										}
										block1 = world.getBlock(new BlockPos(posPlusY));
										if (block1==null) {
											world.setBlock(new BlockPos(posPlusY), place);
											break;
										}
										block1 = world.getBlock(new BlockPos(posMinusY));
										if (block1==null) {
											world.setBlock(new BlockPos(posMinusY), place);
											break;
										}
										block1 = world.getBlock(new BlockPos(posPlusZ));
										if (block1==null) {
											world.setBlock(new BlockPos(posPlusZ), place);
											break;
										}
										block1 = world.getBlock(new BlockPos(posMinusZ));
										if (block1==null) {
											world.setBlock(new BlockPos(posMinusZ), place);
											break;
										}
									}
									pos = pos.sub(camera.direction.nor());
//								world.setBlock(new BlockPos(
//										Math.round(pos.x / 2),
//										Math.round(pos.y / 2),
//										Math.round(pos.z / 2)
//								), place);
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
		} catch (Throwable err) {
			Logger.logErrFull(err);
			dispose();
			Runtime.getRuntime().exit(-1);
		}
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
