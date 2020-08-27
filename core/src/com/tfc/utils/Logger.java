package com.tfc.utils;

import com.tfc.flame.FlameConfig;

//A copy of the logger class from my other project,
public class Logger {
	public static void log(Object obj) {
		if (obj instanceof String) {
			FlameConfig.field.append((String) obj);
		} else {
			FlameConfig.field.append(obj + "");
		}
	}
	
	public static void logLine(Object obj) {
		if (obj instanceof String) {
			FlameConfig.field.append((String) obj + "\n");
		} else {
			FlameConfig.field.append(obj + "" + "\n");
		}
	}
	
	public static void logErr(Throwable err) {
		FlameConfig.logError(err);
	}
	
	public static void logErrFull(Throwable err) {
		StringBuilder s = new StringBuilder();
		FlameConfig.field.append("\n\n");
		s.append("Flame encountered an error:\n");
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		if (err.getCause() != null) {
			logErrFullNoPrefixFull(err.getCause());
		}
		FlameConfig.field.append(s.toString());
		err.getStackTrace();
	}
	
	public static void logErrNoPrefix(Throwable err) {
		StringBuilder s = new StringBuilder();
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		FlameConfig.field.append(s.toString());
	}
	
	public static void logErrFullNoPrefixFull(Throwable err) {
		StringBuilder s = new StringBuilder();
		s.append(err.getClass().getName()).append(": ").append(err.getLocalizedMessage()).append("\n");
		for (StackTraceElement element : err.getStackTrace()) {
			s.append(element.toString()).append("\n");
		}
		FlameConfig.field.append(s.toString());
		if (err.getCause() != null) {
			logErrFullNoPrefixFull(err.getCause());
		}
	}
}
