package com.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class Stub implements InvocationHandler {

	private String host;
	private int port;

	public Stub(String host, int port) {
		this.host = host;
		this.port = port;		
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//TODO
		return null;
	}
}
