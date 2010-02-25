package com.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

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
	 * @param host - host for the remote object
	 * @param port - port on which the remote object accepts requests
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
	 * @param reference - host and port on whiches the remote object accepts requests
	 * @param anInterface
	 *            - interface implemented by the remote object
	 * @return a stub
	 * @see Stub
	 */
	public static Object createStub(InetSocketAddress reference,
			Class<?> anInterface) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { anInterface }, new Stub(reference));
	}

	/**
	 * Returns a specific stub for {@link Registry}.<br/>
	 * The {@link InvocationHandler} is an instance of {@link RegistryStub}
	 * linked to the remote registry (referenced by {@code host} and {@code port}).
	 * 
	 * @param host - host for the remote object
	 * @param port - port on which the remote object accepts requests
	 * @return a stub
	 * @see Stub
	 */
	public static Object createRegistryStub(String host, int port) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
				new Class[] { Registry.class }, new RegistryStub(
						new InetSocketAddress(host, port)));
	}
}
