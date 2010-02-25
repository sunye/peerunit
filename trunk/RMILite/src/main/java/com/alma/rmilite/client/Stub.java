package com.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;

import com.alma.rmilite.RemoteMethodResult;
import com.alma.rmilite.RemoteMethodImpl;
import com.alma.rmilite.io.RemoteProxy;
import com.alma.rmilite.io.IOManager;

public class Stub implements InvocationHandler {

	private InetSocketAddress reference;

	public Stub(InetSocketAddress adress) {
		this.reference = adress;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RemoteProxy skeleton = IOManager.instance.getRemoteProxy(this.reference);		

		skeleton.sendObject(new RemoteMethodImpl(method, args));
		
		Object result = skeleton.recieveObject();
		
		skeleton.close();
		
		if (result instanceof Exception) {
			throw (Exception) result;
		} else if (result instanceof RemoteMethodResult) {
			return ((RemoteMethodResult) result).getObject();
		} else {
			throw new RemoteException("Invalid data !");
		}
	}
}
