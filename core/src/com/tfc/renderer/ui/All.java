package com.tfc.renderer.ui;

import com.tfc.ThreeDeeFirstPersonGame;
import com.tfc.events.EventBase;
import com.tfc.events.render.RenderUI;

public class All {
	public static void render(EventBase event) {
		if (ThreeDeeFirstPersonGame.getInstance().ingame) {
			Hotbar.render((RenderUI) event);
		} else {
			if (ThreeDeeFirstPersonGame.menu == 0)
				TitleScreen.render((RenderUI) event);
			else if (ThreeDeeFirstPersonGame.menu == 1)
				WorldList.render((RenderUI) event);
		}
	}
}
