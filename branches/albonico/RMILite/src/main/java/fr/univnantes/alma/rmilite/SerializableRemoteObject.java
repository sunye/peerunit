package fr.univnantes.alma.rmilite;

import java.io.Serializable;
import java.rmi.Remote;

import fr.univnantes.alma.rmilite.client.Stub;

/**
 * Serializes a specific remote object and provides his {@link Stub}.<br/>
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
