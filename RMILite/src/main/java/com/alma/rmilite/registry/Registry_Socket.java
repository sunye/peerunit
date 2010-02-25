package com.alma.rmilite.registry;

import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

public class Registry_Socket implements Registry, TypesRegistry {
	
	private class RegistryObject {
		
		private Remote object;
		private Class<? extends Remote> type;
		
		public RegistryObject(Remote object, Class<? extends Remote> type) {
			this.object = object;
			this.type = type;
		}
	}
	
	private Map<String, RegistryObject> name2object;
	
	public Registry_Socket() {
		this.name2object = new HashMap<String, RegistryObject>();
	}

	@Override
	public void bind(String name, Remote object, Class<? extends Remote> type) throws Exception {
		this.name2object.put(name, new RegistryObject(object, type));
	}

	@Override
	public Remote lookup(String name) throws Exception {
		if (!this.name2object.containsKey(name)) {
			throw new Exception("Key " + name + " doesn't exist in the registry");
		}
		return this.name2object.get(name).object;
	}

	@Override
	public void unbind(String name) throws Exception {
		this.name2object.remove(name);
	}

	@Override
	public Class<? extends Remote> getType(String object) throws Exception {
		if (!this.name2object.containsKey(object)) {
			throw new Exception("Key " + object + " doesn't exist in the registry");
		}
		return this.name2object.get(object).type;
	}
}
