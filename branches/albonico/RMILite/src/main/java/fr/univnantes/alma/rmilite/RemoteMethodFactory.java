package fr.univnantes.alma.rmilite;

import java.io.NotSerializableException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import fr.univnantes.alma.rmilite.client.StubFactory;
import fr.univnantes.alma.rmilite.io.IOManager;
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
	 * The {@link RemoteObjectManager}, it's the same instance used by the {@link IOManager};
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
	 * Returns a serializable remote {@code object}.
	 * @param object - the remote object
	 * @return the serializable remote object
	 * @throws UnexportedException
	 */
	static public SerializableRemoteObject createSerializableRemoteObject(
			Remote object) throws UnexportedException {
		InetSocketAddress reference;
		if (StubFactory.isStub(object)) { // if the remote result is already a stub
			reference = StubFactory.getStubReference(object);
		} else if (remoteObjectManager.isExported(object)) { // if the remote object is
														// exported
			reference = new InetSocketAddress(remoteObjectManager.getPort(object));
		} else {
			throw new UnexportedException("Unexported argument");
		}
		return new SerializableRemoteObjectImpl(object,reference);
	}
}
