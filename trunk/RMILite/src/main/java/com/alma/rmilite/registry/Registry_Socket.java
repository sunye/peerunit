package com.alma.rmilite.registry;

import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

public class Registry_Socket implements Registry {
	
	private Map<String, Remote> name2object;
	
	public Registry_Socket() {
		this.name2object = new HashMap<String, Remote>();
	}

	@Override
	public void bind(String name, Remote object) throws Exception {		
		this.name2object.put(name, object);
	}

	@Override
	public Remote lookup(String name) throws Exception {
		if (!this.name2object.containsKey(name)) {
			throw new Exception("Key " + name + " doesn't exist in the registry");
		}
		return this.name2object.get(name);
	}

	@Override
	public void unbind(String name) throws Exception {
		this.name2object.remove(name);
	}
}
