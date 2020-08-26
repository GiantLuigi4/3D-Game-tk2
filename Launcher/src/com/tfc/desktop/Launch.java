package com.tfc.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.tfc.ThreeDeeFirstPersonGame;

public class Launch {
	public static LwjglApplication application;
	public static LwjglApplicationConfiguration config;
	
	public static void launch(String[] args) {
		System.out.println("Game running on " + Launch.class.getClassLoader());
		config = new LwjglApplicationConfiguration();
		application = new LwjglApplication(new ThreeDeeFirstPersonGame(), config);
	}
}
