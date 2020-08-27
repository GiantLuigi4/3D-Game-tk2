package com.tfc.desktop;

import com.tfc.flame.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

public class DesktopLauncher {
	private static String dir = System.getProperty("user.dir");
	private static boolean isDev =
			new File(dir + "\\core").exists() &&
					(new File(dir + "\\game").exists() ||
							new File(dir + "\\build.gradle").exists()
					);
	
	public static FlameURLLoader loader;
	
	public static void main(String[] arg) {
		try {
			List<URL> urls = new List<>();
			ArrayList<String> mods = new ArrayList<>();
			if (isDev) {
				urls.add(new File(dir + "\\game\\build\\classes\\java\\main").toURL());
				urls.add(new File(dir + "\\core\\build\\classes\\java\\main").toURL());
			} else {
				urls.add(new File(dir + "\\game-1.0.jar").toURL());
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
			ArrayList<Object> mods_list = new ArrayList<>();
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
			
			loader = new FlameURLLoader(urls.toArray(new URL[urls.size()]));
			System.out.println("Initializing Flame.");
			Class<?> main = loader.loadClass("com.tfc.desktop.Launch");
			main.getMethod("launch", String[].class).invoke(null, (Object) arg);
		} catch (Throwable err) {
			System.out.println("Could not initialize Flame Mod Loader.");
			throw new RuntimeException(err);
		}
	}
}
