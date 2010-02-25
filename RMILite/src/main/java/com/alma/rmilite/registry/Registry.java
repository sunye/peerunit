package com.alma.rmilite.registry;

import java.rmi.Remote;

public interface Registry extends Remote {

	/**
	 * register a object in the registry
	 * @param name an identifier, should be unique on the registry
	 * @param object the object that will be available given the identifier
	 * @param type TODO
	 */
	public void bind(String name, Remote object, Class<? extends Remote> type) throws Exception;
	
	/**
	 * unregister an object. if a lookup is done after unbounding, no object
	 * will be returned
	 * @param name
	 * @throws Exception
	 */
	public void unbind(String name) throws Exception;
	
	/**
	 * return the object that have been registered using {@link #bind}
	 * @param name an identifier
	 * @return the object bound to the identifier
	 * @throws Exception
	 */
	public Remote lookup(String name) throws Exception;
}
