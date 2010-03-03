package com.alma.rmilite;

import java.io.Serializable;
import java.rmi.Remote;

import com.alma.rmilite.client.Stub;

/**
 * Serializes and identifies a specific remote object.<br/>
 *
 */
public interface SerializableRemoteObject extends Serializable {

	/**
	 * Deserializes the remote object.
	 * 
	 * @return a {@link Stub} linked to the remote object.
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @see Stub
	 */
	public Remote getObject(); 
}
