package com.alma.rmilite;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Serializes and identifies a specific method.<br/>
 * <br/>
 * Used by a {@link RemoteMethod} instance because the original class
 * {@link Method} isn't serializable.
 * 
 * @see RemoteMethod
 */
public interface SerializableMethod extends Serializable {

	/**
	 * Deserializes the method.
	 * 
	 * @return a {@link Method} instance.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public Method getMethod() throws SecurityException, NoSuchMethodException;

}