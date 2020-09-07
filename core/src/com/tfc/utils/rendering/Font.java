package com.tfc.utils.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tfc.registry.Textures;
import com.tfc.utils.Location;

public class Font {
	private final Texture texture;
	private final int charWidth;
	private final int charHeight;
	
	public Font(Texture texture, int charWidth, int charHeight) {
		this.texture = texture;
		this.charWidth = charWidth;
		this.charHeight = charHeight;
	}
	
	//Row1 = upper
	//Row2 = lower
	//Everything else = special characters
	public Font(Location namespace, String texture, int charWidth, int charHeight) {
		if (Textures.contains(namespace)) {
			this.texture = Textures.get(namespace);
		} else {
			this.texture = Textures.register(namespace, new Texture(texture)).get();
		}
		this.charWidth = charWidth;
		this.charHeight = charHeight;
	}
	
	public void draw(SpriteBatch batch, String text, int x, int y, int fontWidth, int fontHeight) {
		int posX = 0;
		for (char c : text.toCharArray()) {
			draw(batch, c, x + posX, y, fontWidth, fontHeight);
			posX += fontWidth + 2;
		}
	}
	
	public void draw(SpriteBatch batch, char c, int x, int y, int width, int height) {
		int cx = 0;
		int cy = 0;
		switch (Character.toUpperCase(c)) {
			case 'A':
				break;
			case 'B':
				cx = 1;
				break;
			case 'C':
				cx = 2;
				break;
			case 'D':
				cx = 3;
				break;
			case 'E':
				cx = 4;
				break;
			case 'F':
				cx = 5;
				break;
			case 'G':
				cx = 6;
				break;
			case 'H':
				cx = 7;
				break;
			case 'I':
				cx = 8;
				break;
			case 'J':
				cx = 9;
				break;
			case 'K':
				cx = 10;
				break;
			case 'L':
				cx = 11;
				break;
			case 'M':
				cx = 12;
				break;
			case 'N':
				cx = 13;
				break;
			case 'O':
				cx = 14;
				break;
			case 'P':
				cx = 15;
				break;
			case 'Q':
				cx = 16;
				break;
			case 'R':
				cx = 17;
				break;
			case 'S':
				cx = 18;
				break;
			case 'T':
				cx = 19;
				break;
			case 'U':
				cx = 20;
				break;
			case 'V':
				cx = 21;
				break;
			case 'W':
				cx = 22;
				break;
			case 'X':
				cx = 23;
				break;
			case 'Y':
				cx = 24;
				break;
			case 'Z':
				cx = 25;
				break;
			case ' ':
				cy = 2;
				break;
			case '.':
				cx = 2;
				cy = 2;
				break;
			case '!':
				cx = 3;
				cy = 2;
				break;
			case ',':
				cx = 4;
				cy = 2;
				break;
			case '_':
				cx = 5;
				cy = 2;
				break;
			case '-':
				cx = 6;
				cy = 2;
				break;
			case '1':
				cx = 7;
				cy = 2;
				break;
			case '2':
				cx = 8;
				cy = 2;
				break;
			case '3':
				cx = 9;
				cy = 2;
				break;
			case '4':
				cx = 10;
				cy = 2;
				break;
			case '5':
				cx = 11;
				cy = 2;
				break;
			case '6':
				cx = 12;
				cy = 2;
				break;
			case '7':
				cx = 13;
				cy = 2;
				break;
			case '8':
				cx = 14;
				cy = 2;
				break;
			case '9':
				cx = 15;
				cy = 2;
				break;
			case '0':
				cx = 16;
				cy = 2;
				break;
			default:
				cx = 1;
				cy = 2;
				break;
		}
		if (cy == 0) {
			if (Character.toLowerCase(c) == c) {
				cy += 1;
				if (cx == 24) y -= (height / 6f);
				if (cx == 15) y -= (height / 2f);
			}
		} else if (cy == 2) {
			if (cx == 4) y -= (height / 16f);
		}
		draw(batch, cx, cy, x, y, width, height);
	}
	
	private void draw(SpriteBatch batch, int idX, int idY, int x, int y, int width, int height) {
		batch.draw(
				texture,
				x, y + height,
				width, -height,
				(charWidth * idX) / (float) texture.getWidth(), (charHeight * idY) / (float) texture.getHeight(),
				((charWidth * idX) + charWidth) / (float) texture.getWidth(), ((charHeight * idY) + charHeight) / (float) texture.getHeight()
		);
	}
}
