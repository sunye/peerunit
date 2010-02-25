package com.alma.rmilite.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import com.alma.rmilite.registry.TypesRegistry;

/**
 * Specific stub for the remote registry. Allows to cast the returned reference
 * to a subinterface.
 * 
 * @see Registry
 */
public class RegistryStub extends Stub {

	public RegistryStub(InetSocketAddress adress) {
		super(adress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.client.Stub#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = super.invoke(proxy, method, args);

		/* If the method invoked is... */
		if (method.getName().equals("lookup")) {
			Class<?> type = (Class<?>) super.invoke(proxy, TypesRegistry.class
					.getMethod("getType", String.class), args); // the subinterface of the remote object
			result = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
					new Class[] { type }, Proxy.getInvocationHandler(result)); // specific proxy
		}

		return result;
	}
}
