package com.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.registry.Registry;

/**
 * StubFactory provides static methods for creating dynamic proxy/{@link Stub}
 * classes and instances who implement the specified interfaces.
 * 
 * @see Proxy
 */
public class StubFactory {

	/**
	 * Returns a stub for the specified interface.<br/>
	 * The {@link InvocationHandler} is an instance of {@link Stub} linked to
	 * the remote object (referenced by {@code host} and {@code port}).
	 * 
	 * @param host - the host for the remote object
	 * @param port - the port on which the remote object accepts requests
	 * @param anInterface
	 *            - interface implemented by the remote object
	 * @return a stub
	 * @see Stub
	 */
	public static Object createStub(String host, int port, Class<?> anInterface) {
		return createStub(new InetSocketAddress(host, port), anInterface);
	}

	/**
	 * Returns a stub for the specified interface.<br/>
	 * The {@link InvocationHandler} is an instance of {@link Stub} linked to
	 * the remote object (referenced by {@code reference}).
	 * 
	 * @param reference - the host and the port on whiches the remote object accepts requests
	 * @param anInterface
	 *            - interface implemented by the remote object
	 * @return a stub
	 * @see Stub
	 */
	public static Object createStub(InetSocketAddress reference,
			Class<?> anInterface) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { anInterface, StubMarker.class }, new Stub(reference));
	}

	/**
	 * Returns a specific stub for {@link Registry}.<br/>
	 * The {@link InvocationHandler} is an instance of {@link RegistryStub}
	 * linked to the remote registry (referenced by {@code host} and {@code port}).
	 * 
	 * @param host - the host for the remote object
	 * @param port - the port on which the remote object accepts requests
	 * @return a stub
	 * @see Stub
	 */
	public static Object createRegistryStub(String host, int port) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { Registry.class, StubMarker.class }, new RegistryStub(
						new InetSocketAddress(host, port)));
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
