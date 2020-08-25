package com.tfc.utils;

import java.util.Objects;

public class Location {
	private final String namespace;
	private final String location;
	
	public Location(String namespace, String location) {
		this.namespace = namespace;
		this.location = location;
	}
	
	public Location(String name) {
		this.namespace = name.split(":",2)[0].replace(":","");
		this.location = name.split(":",2)[1].replace(":","");
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String toString() {
		return namespace+":"+location;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Location location1 = (Location) o;
		return Objects.equals(namespace, location1.namespace) &&
				Objects.equals(location, location1.location);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(namespace, location);
	}
}
