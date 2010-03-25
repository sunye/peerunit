package fr.univnantes.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import fr.univnantes.alma.rmilite.io.IOManager;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;

/**
 * StubFactory provides static methods for creating dynamic proxy/{@link Stub}
 * classes and instances who implement the specified interfaces.
 * 
 * @see Proxy
 */
public class StubFactory {
	
	/**
	 * The {@link IOManager}, it's the same instance used by the {@link RemoteObjectProvider};
	 */
	public static IOManager ioManager;

	/**
	 * Returns a stub for the specified interface.<br/>
	 * The {@link InvocationHandler} is an instance of {@link Stub} linked to
	 * the remote object (referenced by {@code host} and {@code port}).
	 * 
	 * @param host - the host for the remote object
	 * @param port - the port on which the remote object accepts requests
	 * @param interfaces
	 *            - interface implemented by the remote object
	 * @return a stub
	 * @see Stub
	 */
	public static Remote createStub(String host, int port, Class<?>[] interfaces) {
		return createStub(new InetSocketAddress(host, port), interfaces);
	}

	/**
	 * Returns a stub for the specified interface.<br/>
	 * The {@link InvocationHandler} is an instance of {@link Stub} linked to
	 * the remote object (referenced by {@code reference}).
	 * 
	 * @param reference - the host and the port on whiches the remote object accepts requests
	 * @param interfaces
	 *            - interface implemented by the remote object
	 * @return a stub
	 * @see Stub
	 */
	public static Remote createStub(InetSocketAddress reference,
			Class<?>[] interfaces) {
		/* Adds StubMarker */
		Class<?>[] allInterfaces = new Class<?>[interfaces.length +1];
		System.arraycopy(interfaces, 0, allInterfaces, 0, interfaces.length);
		allInterfaces[interfaces.length] = StubMarker.class;
		return (Remote) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				allInterfaces, new Stub(reference, ioManager));
	}
	
	/**
	 * If a remote object is a {@link Stub} or not.
	 * @param object - a remote object
	 * @return true if the remote object is a Stub, false otherwise
	 */
	public static boolean isStub(Remote object) {
		return object instanceof StubMarker;
	}
	
	/**
	 * If the specified remote object is a {@link Stub}, returns her reference.
	 * @param object - a remote object
	 * @return her reference
	 */
	public static InetSocketAddress getStubReference(Remote object) {
		return ((Stub) Proxy.getInvocationHandler(object)).getReference();
	}
}
