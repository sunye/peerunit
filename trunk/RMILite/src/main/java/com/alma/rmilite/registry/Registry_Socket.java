package com.alma.rmilite.registry;

import java.rmi.Remote;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Registry_Socket implements Registry, TypesRegistry {

	/**
	 * An remote object and his interface.
	 */
	private class RegistryObject {

		private Remote object;

		/**
		 * Interface implemented by the remote object.
		 */
		private Class<? extends Remote> type;

		private RegistryObject(Remote object, Class<? extends Remote> type) {
			this.object = object;
			this.type = type;
		}
	}

	private Map<String, RegistryObject> name2object;

	public Registry_Socket() {
		this.name2object = Collections
				.synchronizedMap(new HashMap<String, RegistryObject>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.registry.Registry#bind(java.lang.String,
	 * java.rmi.Remote, java.lang.Class)
	 */
	@Override
	public void bind(String name, Remote object, Class<? extends Remote> type)
			throws Exception {
		this.name2object.put(name, new RegistryObject(object, type));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.registry.Registry#lookup(java.lang.String)
	 */
	@Override
	public Remote lookup(String name) throws Exception {
		if (!this.name2object.containsKey(name)) {
			throw new Exception("Key " + name
					+ " doesn't exist in the registry");
		}
		return this.name2object.get(name).object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.registry.Registry#unbind(java.lang.String)
	 */
	@Override
	public void unbind(String name) throws Exception {
		this.name2object.remove(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.registry.TypesRegistry#getType(java.lang.String)
	 */
	@Override
	public Class<? extends Remote> getType(String object) throws Exception {
		if (!this.name2object.containsKey(object)) {
			throw new Exception("Key " + object
					+ " doesn't exist in the registry");
		}
		return this.name2object.get(object).type;
	}
}
