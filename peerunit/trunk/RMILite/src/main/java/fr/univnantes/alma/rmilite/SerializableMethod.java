package fr.univnantes.alma.rmilite;

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

	/**
	 * Returns an array of Class objects that represent the formal parameter
	 * types, in declaration order, of the method represented by this Method
	 * object. Returns an array of length 0 if the underlying method takes no
	 * parameters.
	 * 
	 * @return the parameter types for the method this object represents
	 */
	public Class<?>[] getParameterTypes();
}