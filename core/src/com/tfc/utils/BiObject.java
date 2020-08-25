package com.tfc.utils;

public class BiObject<V,T> {
	private final V obj1;
	private final T obj2;
	
	public BiObject(V obj1, T obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	public V getObj1() {
		return obj1;
	}
	
	public T getObj2() {
		return obj2;
	}
}
