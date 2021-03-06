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
import com.tfc.files.tfile.TFile;
import com.tfc.flame.FlameConfig;
import com.tfc.inputs.Mouse;
import com.tfc.model.Cube;
import com.tfc.model.Triangle;
import com.tfc.registry.Blocks;
import com.tfc.registry.Textures;
import com.tfc.renderer.ui.All;
import com.tfc.utils.BiObject;
import com.tfc.utils.Location;
import com.tfc.utils.Logger;
import com.tfc.utils.awt.AwtWrapper;
import com.tfc.utils.discord.rich_presence.RichPresence;
import com.tfc.utils.files.Compression;
import com.tfc.utils.files.Files;
import com.tfc.utils.files.GZip;
import com.tfc.utils.rendering.Font;
import com.tfc.world.TerrainTriangle;
import com.tfc.world.World;
import com.tfc.world.chunks.Chunk;
import com.tfc.world.chunks.ChunkPos;
import com.tfc.world.chunks.TerrainChunk;
import net.rgsw.ptg.noise.perlin.Perlin2D;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;

public class ThreeDeeFirstPersonGame extends ApplicationAdapter implements InputProcessor {
	public SpriteBatch batch2d;
	private static ThreeDeeFirstPersonGame INSTANCE;
	public ModelBuilder modelBuilder;
	
	private static final RenderUI renderEvent = (RenderUI) Objects.requireNonNull(EventBase.getOrCreateInstance(RenderUI.class));
	
	public static ThreeDeeFirstPersonGame getInstance() {
		return INSTANCE;
	}
	
	private static int lastGC = 0;
	
	public PerspectiveCamera camera;
	public Texture hotbar;
	public static final String dir = System.getProperty("user.dir");
	public ModelBatch batch;
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
	
	/*
	0 = title
	1 = world list
	2 = pause (nyi)
	 */
	public static int menu = 0;
	public static Font defaultFont;
	public String worldFile = "";
	public TFile file = null;
	public boolean ingame = false;
	
	public int getMouseX() {
		return mx;
	}
	
	private static void register(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createModel((new Location(namespace + ":" + name)))));
	}
	
	private static void registerTransparent(String name) {
		Blocks.register(new Block(new Location(namespace + ":" + name), Cube.createTransparentModel((new Location(namespace + ":" + name)))));
	}
	
	private final ArrayList<Integer> keys = new ArrayList<>();
	
	boolean leftDown = false;
	boolean rightDown = false;
	
	public int getMouseY() {
		return my;
	}
	
	public boolean isLeftDown() {
		return leftDown;
	}
	
	public void setLeftDown(boolean leftDown) {
		this.leftDown = leftDown;
	}
	
	public boolean isRightDown() {
		return rightDown;
	}
	
	public void setRightDown(boolean rightDown) {
		this.rightDown = rightDown;
	}
	
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
	
	public static final Registry registryEvent = (Registry) Objects.requireNonNull(EventBase.getOrCreateInstance(Registry.class));
	
	public double camRotX = 0;
	public double camRotY = 45;
	public double camRotZ = 0;
	
	@Override
	public boolean keyDown(int keycode) {
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
		Textures.register(new Location(namespace + ":white_outline"), new Texture("assets\\ui\\bounding_box\\white_border.png"));
		Textures.register(new Location(namespace + ":save_bg"), new Texture("assets\\ui\\menu\\world_bg.png"));
		Textures.register(new Location(namespace + ":hotbar"), new Texture("assets\\ui\\ingame\\hotbar_slot.png"));
		Textures.register(new Location(namespace + ":button"), new Texture("assets\\ui\\menu\\button.png"));
		Textures.register(new Location(namespace + ":button_hovered"), new Texture("assets\\ui\\menu\\button_hovered.png"));
		Textures.register(new Location(namespace + ":font"), new Texture("assets\\ui\\font\\font1.png"));
		
		defaultFont = new Font(Textures.get(new Location(namespace + ":font")), 14, 30);
		
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
				(new Location(namespace + ":stone"))
		);
		
		boundingBox = Cube.createModel((new Location(namespace + ":bounding_box")));
		
		//https://stackoverflow.com/questions/19112349/libgdx-3d-texture-transparency
		boundingBox.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
	}
	
	public final HashMap<ChunkPos, ModelInstance> terrainChunkModels = new HashMap<>();
	
	public Perlin2D getNoise() {
		return noise;
	}
	
	@Override
	public void create() {
		try {
			RichPresence.main(new String[0]);
			RichPresence.update("In menu", "");
			
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
			
			registryEvent.register(new Location(namespace + ":register"), this::register);
			renderEvent.register(new Location(namespace + ":ui"), All::render);
			
			registryEvent.post();
			
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
			
			player.pos.y = 128;
			
			hotbar = Textures.get(new Location(namespace + ":hotbar"));
			
			environmentSurface = new Environment();
			environmentSurface.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.1f, 0.1f, 0.1f, 1f));
			
			spritehotbar = new Sprite(hotbar);
			
			Gdx.input.setInputProcessor(this);
			
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
		
		logic.setDaemon(false);
		logic.start();
	}
	
	int scaleX = 30;
	int scaleY = 30;
	
	@Override
	public void dispose() {
		try {
			batch.dispose();
			batch2d.dispose();
			Textures.close();
			running.set(false);
			
			if (ingame) {
//				TFile tfile = new TFile();
				if (file == null) file = new TFile();
				TFile tfile = file;
				for (int i = 0; i <= 10; i++) {
					try {
						tfile
								//Players
								.getOrCreateInnerTFile()
								//Chunks
								.getOrCreateInnerTFile()
								//Terrain
								.getOrCreateInnerTFile();
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}
				
				try {
					String text1 = "pos:" + this.player.pos.x + "," + this.player.pos.y + "," + this.player.pos.z + "\n";
					text1 += "rot:" + camRotX + "," + camRotY + "," + camRotZ + "\n";
					String text = Compression.makeIllegible(Compression.compress(text1));
					tfile.addOrReplaceFile("player1.data", text);
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
				
				try {
					//https://www.codejava.net/java-se/file-io/how-to-compress-files-in-zip-format-in-java#:~:text=Here%20are%20the%20steps%20to,ZipEntry)%20method%20on%20the%20ZipOutputStream.
					for (Chunk chunk : world.chunks.values()) {
						String pos = chunk.pos.chunkX + "," + chunk.pos.chunkY + "," + chunk.pos.chunkZ;
						String text = Compression.deQuadruple(Compression.makeIllegible(Compression.compress(chunk.toString())));
						//add the file to the inner file of the tfile, inner files are an optimization method, and it's not bad optimization
						tfile.getInner().addOrReplaceFile(pos + ".data", text);
					}
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
				
				try {
					//https://www.codejava.net/java-se/file-io/how-to-compress-files-in-zip-format-in-java#:~:text=Here%20are%20the%20steps%20to,ZipEntry)%20method%20on%20the%20ZipOutputStream.
					for (TerrainChunk chunk : world.terrainChunks.values()) {
						try {
							String pos = chunk.pos.chunkX + "," + chunk.pos.chunkY + "," + chunk.pos.chunkZ;
							String text = Compression.deQuadruple(Compression.makeIllegible(Compression.compress(chunk.toString())));
							tfile.getInner().getInner().addOrReplaceFile(pos + ".data", text);
						} catch (Throwable err) {
							Logger.logErrFull(err);
						}
					}
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
				
				String text = Compression.makeIllegible(Compression.deQuadruple(Compression.compress(
						"//I guess you can tamper with this (for now)" + "\n" +
								"seed:" + seed + "\n" +
								"scaleX:" + scaleX + "\n" +
								"scaleY:" + scaleY + "\n" +
								"createNoise\n" +
								"time:" + dayTime + "\n"
				)));
				tfile.addOrReplaceFile("level.properties", text);
				
				try {
					byte[] bytes = GZip.gZip(tfile.toString());
					Files.createFile(this.worldFile, bytes);
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
			}
		} catch (Throwable err) {
			Logger.logErrFull(err);
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
	
	public void loadWorld(String worldFile) {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(Files.readB(worldFile));
			String tfile = GZip.readString(new GZIPInputStream(stream));
			this.worldFile = worldFile;
			file = new TFile(tfile);
			String playerData = Compression.decompress(Compression.makeLegible(file.get("player1.data")));
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
			
			world.loadAll(file.getOrCreateInnerTFile().getOrCreateInnerTFile());
			world.loadTerrainChunks(file.getInner().getInner());
			String level = Compression.decompress(Compression.reQuadruple(Compression.makeLegible(file.get("level.properties"))));
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
	
	public void createWorld(File file) {
		try {
			this.file = new TFile();
			file.getParentFile().mkdirs();
			int size = 8;
			this.worldFile = "saves/" + file.getName();
			seed = new Random().nextInt();
			scaleX = new Random(seed).nextInt(16) + 16;
			scaleY = new Random(seed * 2).nextInt(16) + 16;
			noise = new Perlin2D(seed, scaleX, scaleY);
			for (int x = -size; x <= size; x++) {
				for (int z = -size; z <= size; z++) {
					world.generate(x, z, noise);
					
					int yPos = Math.abs(z) == (size - 2) ? 2 : Math.abs(x) == (size - 2) ? 2 : 0;
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
		} catch (Throwable err) {
			Logger.logErrFull(err);
		}
	}
	
	private static boolean testAndRender(TerrainTriangle triangle, Vector3 finalPos, BlockPos pos2, ModelBatch batch, ModelInstance boundingBox, Environment environmentSurface, Vector3 offset) {
		if (triangle.collides(finalPos)) {
//			ModelInstance instance = triangle.renderable.copy();
//			instance.transform.setTranslation(offset.cpy().add(0,12,0));
			TerrainTriangle render = new TerrainTriangle(triangle.v1, triangle.v2, triangle.v3, new Location(namespace + ":bounding_box"));
			TerrainTriangle render1 = new TerrainTriangle(triangle.v3, triangle.v1, triangle.v2, new Location(namespace + ":bounding_box"));
			render.renderable.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
			render1.renderable.materials.get(0).set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
			render.draw(batch, environmentSurface, offset.cpy().add(0, 0.0001f, 0));
			render1.draw(batch, environmentSurface, offset.cpy().add(0, 0.0001f, 0));
//			batch.render(render.renderable, environmentSurface);
//			boundingBox.transform.setTranslation(
//					triangle.min.x,
//					triangle.min.y,
//					triangle.min.z
//			);
//			boundingBox.transform.translate(offset);
//			batch.render(boundingBox, environmentSurface);
			return true;
		}
		return false;
	}
	
	@Override
	public void render() {
		try {
			lastGC++;
			
			if (lastGC >= 10000) {
				Runtime.getRuntime().gc();
				lastGC = 0;
			}
			
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
			
			Vector3 lerped = lastPos.lerp(player.pos, Math.max(0, Math.min(1, Math.abs(((new Date().getTime() - lastTick.get()) / 10f)))));
			Vector3 offset = new Vector3(-lerped.x, -lerped.y, -lerped.z);
			
			Gdx.gl.glClearColor(0, 1f, 1f, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			
			try {
				batch.begin(camera);
			} catch (Throwable err) {
				batch.end();
				batch.begin(camera);
			}
			if (ingame) {
				try {
					AtomicInteger bakedInFrame = new AtomicInteger(0);
					Object[] chunkPoses = world.chunks.keySet().toArray();
					Object[] chunks = world.chunks.values().toArray();
//					world.chunks.forEach((chunkPos, chunk) -> {
					for (int i = 0; i < chunkPoses.length; i++) {
						ChunkPos chunkPos = (ChunkPos) chunkPoses[i];
						Chunk chunk = (Chunk) chunks[i];
						
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
								chunkModels.get(chunkPos).transform.setTranslation(offset);
								batch.render(chunkModels.get(chunkPos), environmentSurface);
								bakedInFrame.getAndAdd(1);
							}
						}
					}
//					});
					world.needsRefresh.clear();
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
				
				try {
					Object[] chunkPoses = world.terrainChunks.keySet().toArray();
					Object[] chunks = world.terrainChunks.values().toArray();
//					world.terrainChunks.forEach((pos, chunk) -> {
					for (int i = 0; i < chunkPoses.length; i++) {
						ChunkPos pos = (ChunkPos) chunkPoses[i];
						TerrainChunk chunk = (TerrainChunk) chunks[i];
						if (new Random().nextInt(32) == 16) {
							terrainChunkModels.remove(pos);
						}
						try {
							if (!terrainChunkModels.containsKey(pos)) {
								ModelInstance instance = chunk.bake();
								terrainChunkModels.put(pos, instance);
							}
							
							ModelInstance instance = terrainChunkModels.get(pos);
							instance.transform.setTranslation(offset);
							batch.render(instance, environmentSurface);
						} catch (Throwable err) {
							if (terrainChunkModels.containsKey(pos)) {
								terrainChunkModels.remove(pos);
							}
						}
					}
//					});
				} catch (Throwable err) {
					try {
						Object[] chunkPoses = world.terrainChunks.keySet().toArray();
						Object[] chunks = world.terrainChunks.values().toArray();
//					world.terrainChunks.forEach((pos, chunk) -> {
						for (int i = 0; i < chunkPoses.length; i++) {
							ChunkPos pos = (ChunkPos) chunkPoses[i];
							TerrainChunk chunk = (TerrainChunk) chunks[i];
							if (new Random().nextInt(32) == 16) {
								terrainChunkModels.remove(pos);
							}
							try {
								if (!terrainChunkModels.containsKey(pos)) {
									ModelInstance instance = chunk.bake();
									terrainChunkModels.put(pos, instance);
								}
								
								ModelInstance instance = terrainChunkModels.get(pos);
								instance.transform.setTranslation(offset);
								batch.render(instance, environmentSurface);
							} catch (Throwable err1) {
								if (terrainChunkModels.containsKey(pos)) {
									terrainChunkModels.remove(pos);
								}
							}
						}
					} catch (Throwable err2) {
						Logger.logErrFull(err);
					}
				}
				
				BlockPos pos1 = handlePlacementTerrain(offset);
				BlockPos pos2 = handlePlacement(offset);
				try {
					Block place = Blocks.getByID(slot);
					if (pos1 != null && pos2 == null) {
						Mouse.release(0);
						Mouse.release(2);
						world.setBlock(pos1, place);
					} else if (pos1 == null && pos2 != null) {
						Mouse.release(0);
						Mouse.release(2);
						world.setBlock(pos2, place);
					} else {
						if (pos1 != null && pos2 != null) {
							if (pos2.distance(player.pos) > pos1.distance(player.pos)) {
								Mouse.release(0);
								Mouse.release(2);
								world.setBlock(pos1, place);
							} else {
								Mouse.release(0);
								Mouse.release(2);
								world.setBlock(pos2, place);
							}
						}
					}
				} catch (Throwable err) {
					Logger.logErrFull(err);
				}
			}
			batch.end();
			
			try {
				batch2d.begin();
			} catch (Throwable err) {
				batch2d.end();
				batch2d.begin();
			}
			renderEvent.post(batch2d);
			batch2d.end();
		} catch (Throwable err) {
			Logger.logErrFull(err);
//			dispose();
//			Runtime.getRuntime().exit(-1);
		}
	}
	
	private BlockPos handlePlacement(Vector3 offset) {
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
//						Block place = Blocks.getByID(slot);
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
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posPlusX), place);
//									break;
									return new BlockPos(posPlusX);
								}
								
								block1 = world.getBlock(new BlockPos(posMinusX));
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posMinusX), place);
//									break;
									return new BlockPos(posMinusX);
								}
								
								block1 = world.getBlock(new BlockPos(posPlusY));
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posPlusY), place);
//									break;
									return new BlockPos(posPlusY);
								}
								
								block1 = world.getBlock(new BlockPos(posMinusY));
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posMinusY), place);
//									break;
									return new BlockPos(posMinusY);
								}
								
								block1 = world.getBlock(new BlockPos(posPlusZ));
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posPlusZ), place);
//									break;
									return new BlockPos(posPlusZ);
								}
								
								block1 = world.getBlock(new BlockPos(posMinusZ));
								
								if (block1 == null) {
//									world.setBlock(new BlockPos(posMinusZ), place);
//									break;
									return new BlockPos(posMinusZ);
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
		return null;
	}
	
	private BlockPos handlePlacementTerrain(Vector3 offset) {
		Vector3 pos = new Vector3(player.pos.x, player.pos.y, player.pos.z);
		pos = pos.add(
				0.5f,
				3.5f,
				0.5f
		);
		pos = pos.add(
				player.pos.x <= 0 ? 0.5f : -0.5f,
				player.pos.y <= 0 ? 0.5f : -0.5f,
				player.pos.z <= 0 ? 0.5f : -0.5f
		);
		
		AtomicBoolean atomicBoolean = new AtomicBoolean(false);
		pos = pos.add(camera.direction.nor().scl(4.01f));
		for (float i = 0; i < 16; i += 0.01f) {
			if (!atomicBoolean.get()) {
				pos = pos.add(camera.direction.nor().scl(0.01f));
				BlockPos pos2 = new BlockPos(pos);
				ChunkPos pos3 = new ChunkPos(pos2);
				
				TerrainChunk chunk = world.getTerrainChunk(pos3, false);
				
				if (chunk != null) {
					Vector3 finalPos = pos;
					AtomicReference<ModelBatch> batchAtomicReference = new AtomicReference<>(batch);
					
					try {
						chunk.forEach(triangle -> {
							try {
								if (!atomicBoolean.get()) {
									atomicBoolean.set(testAndRender(triangle, finalPos, pos2, batchAtomicReference.get(), boundingBox, environmentSurface, offset));
								}
							} catch (Throwable err) {
								Logger.logErrFull(err);
							}
						});
					} catch (Throwable err) {
						Logger.logErrFull(err);
					}
					
					if (atomicBoolean.get()) {
						pos = pos.add(camera.direction.nor().scl(1.5f));
						BlockPos pos1 = new BlockPos(
								(int) pos.x,
								(int) pos.y,
								(int) pos.z
						);
						if (rightDown) {
//							world.setBlock(pos1, null);
							final BlockPos finalPos1 = pos1;
							final int breakSize = 8;
							Mouse.release(2);
							world.terrainChunks.forEach((cp, tc) -> {
								if (cp.blockPos.distance(player.pos) < 1280) {
									tc.forEach((tri) -> {
										Vector3 avg = tri.min.cpy();
										avg.set(avg.x, finalPos1.y, avg.z);
										if (finalPos1.distance(avg) < breakSize + 8) {
											tc.queueRemove(tri);
											float y1 = breakSize - Math.min(finalPos1.distance(tri.v1), breakSize);
											float y2 = breakSize - Math.min(finalPos1.distance(tri.v2), breakSize);
											float y3 = breakSize - Math.min(finalPos1.distance(tri.v3), breakSize);
											tc.queueAdd(
													new TerrainTriangle(
															new Vector3(tri.v1.x, tri.v1.y - y1, tri.v1.z),
															new Vector3(tri.v2.x, tri.v2.y - y2, tri.v2.z),
															new Vector3(tri.v3.x, tri.v3.y - y3, tri.v3.z),
															tri.texture
													)
											);
										}
									});
									tc.update();
									terrainChunkModels.remove(cp);
								}
							});
//							return pos1;
						} else if (leftDown) {
							pos1 = new BlockPos(
									(int) pos.x / 2,
									(int) (pos.y - 2) / 2,
									(int) pos.z / 2
							);
//							world.setBlock(pos1, Blocks.get(new Location(namespace + ":stone")));
							return pos1;
						}
					}
				}
			}
		}
		return null;
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
		if (ingame) {
			camRotX += (mx - screenX) / 16f;
			camRotY += (my - screenY) / 16f;
			BiObject<Integer, Integer> loc = AwtWrapper.getMouseLocation();
//			AwtWrapper.mouseMove(
//					(loc.getObj1() - screenX) + (Gdx.graphics.getWidth() / 2),
//					(loc.getObj2() - screenY) + (Gdx.graphics.getHeight() / 2)
//			);
			AwtWrapper.mouseOffset(
					(-screenX + (Gdx.graphics.getWidth() / 2)) / 4,
					(-screenY + (Gdx.graphics.getHeight() / 2)) / 4
			);
			AwtWrapper.waitForIdle();
			mx = (Gdx.graphics.getWidth() / 2);
			my = (Gdx.graphics.getHeight() / 2);
		} else {
			mx = screenX;
			my = screenY;
		}
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
	
	public int getSlot() {
		return slot;
	}
	
	public void setSlot(int slot) {
		this.slot = slot;
	}
}
