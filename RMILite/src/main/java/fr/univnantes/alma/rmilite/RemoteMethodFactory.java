package fr.univnantes.alma.rmilite;

import java.io.NotSerializableException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import fr.univnantes.alma.rmilite.ioLayer.Manager;
import fr.univnantes.alma.rmilite.server.RemoteObjectManager;

/**
 * Provides {@link RemoteMethod}, {@link RemoteMethodResult} and {@link SerializableRemoteObject}
 * instances.<br/>
 * <br/>
 * Each RemoteMethod instance contain a {@link SerializableMethod}.
 * 
 * @see RemoteMethod
 * @see RemoteMethodResult
 * @see SerializableRemoteObject
 */
public class RemoteMethodFactory {
	
	/**
	 * The {@link RemoteObjectManager}, it's the same instance used by the {@link Manager};
	 */
	public static RemoteObjectManager remoteObjectManager;

	/**
	 * Returns a serializable {@link RemoteMethod} object, included serializable arguments.
	 * 
	 * @param method - the method
	 * @param args - arguments
	 * @return the serializable call/method
	 * @throws NotSerializableException
	 * @throws UnexportedException
	 */
	static public RemoteMethod createRemoteMethod(Method method, Object[] args)
			throws NotSerializableException, UnexportedException {
		return new RemoteMethodImpl(new SerializableMethodImpl(method), args);
	}

	/**
	 * Returns a serializable result of the specified {@code method}.
	 * 
	 * @param method - the method
	 * @param result - the result object
	 * @return the serializable result object
	 * @throws NotSerializableException
	 * @throws UnexportedException
	 */
	static public RemoteMethodResult createRemoteMethodResult(Method method,
			Object result) throws NotSerializableException, UnexportedException {
		return new RemoteMethodResultImpl(method, result);
	}
	
	/**
	 * Returns a serializable remote {@code object}.
	 * @param object - the remote object
	 * @return the serializable remote object
	 * @throws UnexportedException
	 */
	static public SerializableRemoteObject createSerializableRemoteObject(
			Remote object) throws UnexportedException {
		if (remoteObjectManager.isExported(object)) { // if the remote object is
														// exported
			return new SerializableRemoteObjectImpl(object,
					new InetSocketAddress(remoteObjectManager.getPort(object)));
		} else {
			throw new UnexportedException("Unexported argument");
		}
	}
}
