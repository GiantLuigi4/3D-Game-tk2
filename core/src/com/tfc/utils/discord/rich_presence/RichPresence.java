package com.tfc.utils.discord.rich_presence;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class RichPresence {
	private static boolean ready = false;
	
	//This will likely be used for maps or smth
	private static String avatar = "https://cdn.discordapp.com/avatars/%id%/%avatar%.png?size=128";
	
	private static void init() {
		DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler((user) -> {
			ready = true;
			avatar = avatar
					.replace("%avatar%", user.avatar)
					.replace("%id%", user.userId);
			System.out.println(avatar);
			update("blank", "blank");
		}).build();
		DiscordRPC.discordInitialize("751921403867037717", handlers, false);
		DiscordRPC.discordRegister("751921403867037717", "");
	}
	
	public static boolean isReady() {
		return ready;
	}
	
	public static String getAvatar() {
		return avatar;
	}
	
	public static void main(String[] args) throws InterruptedException {
		init();
		for (int i = 0; i < 100; i++) {
			if (!ready) {
				DiscordRPC.discordRunCallbacks();
				Thread.sleep(50);
			}
		}
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Closing Discord hook.");
			DiscordRPC.discordShutdown();
		}));
		Thread.sleep(1000);
	}
	
	public static void update(String state, String details) {
		DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder(details);
		presence.setDetails(state);
		DiscordRPC.discordUpdatePresence(presence.build());
	}
}
