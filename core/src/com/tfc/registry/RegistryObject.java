package com.tfc.registry;

public class RegistryObject<T> {
	private final T object;
	
	protected RegistryObject(T object) {
		this.object = object;
	}
	
	public T get() {
		return object;
	}
}
