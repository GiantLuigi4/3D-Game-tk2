package com.tfc.desktop;

import java.util.ArrayList;

public class List<T> {
	ArrayList<T> list = new ArrayList<>();
	
	public void add(T obj) {
		list.add(obj);
	}
	
	public int size() {
		return list.size();
	}
	
	public T[] toArray(T[] dest) {
		for (int i=0;i<list.size(); i++) {
			dest[i] = list.get(i);
		}
		return dest;
	}
}
