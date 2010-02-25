package com.alma.rmilite.client;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import com.alma.rmilite.registry.Registry;

public class StubFactory {

	public static Object createStub(String host, int port, Class<?> anInterface) {
		return createStub(new InetSocketAddress(host, port), anInterface);
	}
	
	public static Object createStub(InetSocketAddress reference, Class<?> anInterface) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{anInterface}, new Stub(reference));		
	}
	
	public static Object createRegistryStub(String host, int port) {
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Registry.class}, new RegistryStub(new InetSocketAddress(host, port)));
	}
}
