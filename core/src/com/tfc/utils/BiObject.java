package com.tfc.utils;

import java.util.Objects;

public class BiObject<V, T> {
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BiObject<?, ?> biObject = (BiObject<?, ?>) o;
		return Objects.equals(obj1, biObject.obj1) &&
				Objects.equals(obj2, biObject.obj2);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(obj1, obj2);
	}
}
