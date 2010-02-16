package com.alma.rmilite.client;

import java.lang.reflect.Proxy;

public class StubFactory {

	public static Object createStub(String host, int port, Class<?> anInterface) {
		Stub s = new Stub(host, port);
		return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{anInterface}, s);		
	}
}
