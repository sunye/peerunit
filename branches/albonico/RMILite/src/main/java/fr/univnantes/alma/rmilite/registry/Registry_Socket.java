package fr.univnantes.alma.rmilite.registry;

import java.rmi.Remote;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Registry_Socket implements Registry {

	private Map<String, Remote> name2object;

	public Registry_Socket() {
		this.name2object = Collections
				.synchronizedMap(new HashMap<String, Remote>());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.univnantes.alma.rmilite.registry.Registry#bind(java.lang.String,
	 * java.rmi.Remote)
	 */
	@Override
	public void bind(String name, Remote object)
			throws Exception {
		this.name2object.put(name,object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.univnantes.alma.rmilite.registry.Registry#lookup(java.lang.String)
	 */
	@Override
	public Remote lookup(String name) throws Exception {
		if (!this.name2object.containsKey(name)) {
			throw new Exception("Key " + name
					+ " doesn't exist in the registry");
		}
		return this.name2object.get(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.univnantes.alma.rmilite.registry.Registry#unbind(java.lang.String)
	 */
	@Override
	public void unbind(String name) throws Exception {
		this.name2object.remove(name);
	}
}
