package com.alma.rmilite.client;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import com.alma.rmilite.registry.TypesRegistry;

public class RegistryStub extends Stub {

	public RegistryStub(InetSocketAddress adress) {
		super(adress);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = super.invoke(proxy, method, args);
		
		if (method.getName().equals("lookup")) {
			Class<?> type = (Class<?>) super.invoke(proxy, TypesRegistry.class.getMethod("getType", String.class), args);
			result = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{type}, Proxy.getInvocationHandler(result));
		}
		
		return result;
	}
}
