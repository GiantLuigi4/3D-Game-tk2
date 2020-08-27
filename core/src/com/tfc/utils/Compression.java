package com.tfc.utils;

import com.tfc.ThreeDeeFirstPersonGame;

public class Compression {
	private static final String[][] compression = new String[][]{
			{ThreeDeeFirstPersonGame.namespace, "|"},
			{"sand", "+"},
			{"stone", "/"},
			{"green", ">"},
			{"st", "<"},
			{"pos:", "*"},
			{"on", ")"},
			{"qe", "("},
			{"tb", "&"},
			{"12", "^"},
			{"ft", "%"},
			{"64", "$"},
			{"ab", "#"},
			{"no", "@"},
			{"de", "!"},
			{"-1", "\""},
			{"-6", "{"},
			{"-4", "}"},
			{"vel:", "\\"},
			{"rot:", "~"}
	};
	
	public static String compress(String text) {
		String out = text;
		for (String[] query : compression) {
			out = out.replace(query[0], query[1]);
		}
		return out;
//		return text;
	}
	
	public static String decompress(String text) {
		String out = text;
		for (String[] query : compression) {
			out = out.replace(query[1], query[0]);
		}
		return out;
//		return text;
	}
	
	//Just so the average user can't tamper with the files, lol
	//Expands file size by 1 byte, but whatever...
	public static String makeIllegible(String text) {
//		int lowestChar = 99999;
//		StringBuilder out = new StringBuilder();
//		for (char c:text.toCharArray()) {
//			lowestChar = Math.min(c,lowestChar);
//		}
//		out.append((char) lowestChar);
//		for (char c : text.toCharArray()) {
//			out.append((char) (((int) c) - lowestChar));
//		}
//		return out.toString();
		return text;
	}
	
	public static String makeLegible(String text) {
//		int lowestChar = text.toCharArray()[0];
//		StringBuilder out = new StringBuilder();
//		boolean skip = true;
//		for (char c : text.toCharArray()) {
//			if (!skip) {
//				out.append((char) (((int) c) + lowestChar));
//			}
//			skip = false;
//		}
//		return out.toString();
		return text;
	}
}
