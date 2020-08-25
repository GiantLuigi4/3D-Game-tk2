package com.tfc.events.registry;

import com.tfc.events.EventBase;

//This is the best point to actually register your stuff
public class Registry extends EventBase {
	@Override
	public void post() {
		forEachListener((location, consumer) -> consumer.accept(this));
	}
}
