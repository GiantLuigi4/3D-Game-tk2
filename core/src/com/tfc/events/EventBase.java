package com.tfc.events;

import com.tfc.utils.Location;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

//Each event has it's own event bus
public class EventBase {
	private HashMap<Location, Consumer<? super EventBase>> listeners = new HashMap<>();
	
	private static HashMap<Class<? extends EventBase>, EventBase> events = new HashMap<>();
	
	/**
	 * @param eventClass the class of the event you want
	 * @return the pre existing instance of the event or a new instance of the event if none exists
	 */
	public static EventBase getOrCreateInstance(Class<? extends EventBase> eventClass) {
		if (events.containsKey(eventClass)) {
			return events.get(eventClass);
		} else {
			try {
				EventBase eventBase = eventClass.newInstance();
				events.put(eventClass, eventBase);
				return eventBase;
			} catch (Throwable ignored) {
				return null;
			}
		}
	}
	
	public void register(Location name, Consumer<? super EventBase> consumer) {
		listeners.put(name, consumer);
	}
	
	public void post() {
//		listeners.forEach((location, consumer) -> {
//			consumer.accept(this);
//		});
	}
	
	public void forEachListener(BiConsumer<Location, Consumer<? super EventBase>> consumer1) {
		this.listeners.forEach(consumer1::accept);
	}
}
