package com.tfc.launcher;

import com.tfc.flame.FlameConfig;
import com.tfc.flame.FlameLog;
import com.tfc.flame.FlameURLLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

public class Launch {
	public static FlameURLLoader loader;
	private static String dir = System.getProperty("user.dir");
	private static boolean isDev =
			new File(dir + "\\core").exists() &&
					(new File(dir + "\\game").exists() ||
							new File(dir + "\\build.gradle").exists()
					);
	
	public static void main(String[] arg) {
		File versionLaunch = new File(dir + "\\version.txt");
		String versionJar = "";
		if (!versionLaunch.exists()) {
			try {
				versionLaunch.createNewFile();
				FileWriter writer = new FileWriter(versionLaunch);
				writer.write("1.0.jar");
				writer.close();
			} catch (Throwable ignored) {
			}
		} else {
			try {
				Scanner sc = new Scanner(versionLaunch);
				versionJar = sc.nextLine().replace("\n", "");
				sc.close();
			} catch (Throwable ignored) {
			}
		}
		try {
			List<URL> urls = new List<>();
			ArrayList<String> mods = new ArrayList<>();
			if (isDev) {
				urls.add(new File(dir + "\\game\\build\\classes\\java\\main").toURL());
				urls.add(new File(dir + "\\core\\build\\classes\\java\\main").toURL());
			} else {
				File f = new File(dir + "\\versions\\" + versionJar);
				urls.add(f.toURL());
				if (!f.exists()) f.getParentFile().mkdirs();
			}
			
			FlameConfig.field = new FlameLog();
			
			File fi = new File(dir + "\\flame_mods");
			if (!fi.exists()) {
				fi.mkdirs();
			}
			
			for (File fi1 : Objects.requireNonNull(fi.listFiles())) {
				urls.add(fi1.toURL());
				mods.add(fi1.getPath());
			}
			loader = new FlameURLLoader(urls.toArray(new URL[urls.size()]));
			ArrayList<Object> mods_list = new ArrayList<>();
			InputStream stream = loader.getResourceAsStream("version/dependencies.csv");
			Scanner sc = new Scanner(stream);
			StringBuilder dependencies = new StringBuilder();
			while (sc.hasNextLine()) {
				dependencies.append(sc.nextLine().replace("\n", ""));
			}
			InputStream stream2 = loader.getResourceAsStream("version/moredeps.csv");
			Scanner sc2 = new Scanner(stream2);
			StringBuilder dependencies2 = new StringBuilder();
			while (sc2.hasNextLine()) {
				dependencies2.append(sc2.nextLine().replace("\n", ""));
			}
			sc.close();
			sc2.close();
			stream.close();
			stream2.close();
			String repo = "";
			for (String s : dependencies.toString().split(",")) {
				try {
					if (repo.equals("")) {
						repo = s;
					} else {
						System.out.println(repo);
						System.out.println(s);
						String s1 = s.replace(".", "" + File.separatorChar);
						String[] info = s1.split(":", 3);
						info[2] = info[2].replace("" + File.separatorChar, ".");
						s1 = info[0] + File.separatorChar + info[2] + File.separatorChar + info[1] + File.separatorChar + info[1] + "-" + info[2] + ".jar";
						String urlS = info[0] + File.separatorChar + info[1] + File.separatorChar + info[2] + File.separatorChar + info[1] + "-" + info[2] + ".jar";
						File output = new File(dir + File.separatorChar + "libs" + File.separatorChar + s1);
						if (!output.exists()) {
							output.getParentFile().mkdirs();
							output.createNewFile();
							
							String url = repo + (urlS);
							try {
								URL url1 = new URL(url.replace("" + File.separatorChar, "/"));
								System.out.println(url1.toString());
								//https://stackabuse.com/how-to-download-a-file-from-a-url-in-java/
								Files.copy(url1.openStream(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
							} catch (Throwable err) {
								err.printStackTrace();
							}
						}
						loader.addURL(output.toURL());
						repo = "";
					}
				} catch (Throwable err) {
					err.printStackTrace();
					repo = "";
				}
			}
			String file = "";
			for (String s : dependencies2.toString().split(",")) {
				try {
					if (file.equals("")) {
						file = s;
					} else {
						File output = new File(dir + File.separatorChar + file.replace("/", "" + File.separatorChar));
						if (!output.exists()) {
							output.getParentFile().mkdirs();
							output.createNewFile();
							
							String url = s;
							try {
								URL url1 = new URL(url.replace("" + File.separatorChar, "/"));
								System.out.println(url1.toString());
								//https://stackabuse.com/how-to-download-a-file-from-a-url-in-java/
								Files.copy(url1.openStream(), output.toPath(), StandardCopyOption.REPLACE_EXISTING);
							} catch (Throwable err) {
								err.printStackTrace();
							}
						}
						loader.addURL(output.toURL());
						file = "";
					}
				} catch (Throwable err) {
					err.printStackTrace();
					file = "";
				}
			}
			try {
				for (String s : mods) {
					File fi1 = new File(s);
					try {
						Object mod = loader.load("entries." + fi1.getName().split("-")[0].replace("-", "").replace(".zip", "").replace(".jar", "") + ".Main", false).newInstance();
						mods_list.add(mod);
					} catch (Throwable err) {
						FlameConfig.logError(err);
					}
				}
			} catch (Throwable err) {
				FlameConfig.logError(err);
			}
			for (Object mod : mods_list) {
				try {
					if (loader.load("com.tfc.flame.IFlameAPIMod", false).isInstance(mod)) {
						mod.getClass().getMethod("setupAPI", String[].class).invoke(mod, (Object) arg);
					}
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
			for (Object mod : mods_list) {
				try {
					mod.getClass().getMethod("preinit", String[].class).invoke(mod, (Object) arg);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
			for (Object mod : mods_list) {
				try {
					mod.getClass().getMethod("init", String[].class).invoke(mod, (Object) arg);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
			for (Object mod : mods_list) {
				try {
					mod.getClass().getMethod("postinit", String[].class).invoke(mod, (Object) arg);
				} catch (Throwable err) {
					FlameConfig.logError(err);
				}
			}
			
			System.out.println("Initializing Flame.");
			Class<?> main = loader.loadClass("com.tfc.desktop.Launch");
			main.getMethod("launch", String[].class).invoke(null, (Object) arg);
		} catch (Throwable err) {
			System.out.println("Could not initialize Flame Mod Loader.");
			try {
				File log = new File(dir + "\\logs\\"+"flame " + new SimpleDateFormat("yyyy-MM-dd. hh:mm:ss").format(new Date())+".log");
				log.getParentFile().mkdirs();
				log.createNewFile();
				FileWriter writer = new FileWriter(log);
				writer.write(FlameConfig.field.getText());
				writer.close();
			} catch (Throwable ignored) {}
			throw new RuntimeException(err);
		}
	}
}
