package com.alma.rmilite.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.alma.rmilite.RemoteMethodResult;
import com.alma.rmilite.RemoteMethod;

public class Stub implements InvocationHandler {

	private InetSocketAddress reference;

	public Stub(InetSocketAddress adress) {
		this.reference = adress;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Socket skel = new Socket(this.reference.getAddress(), this.reference.getPort());		

		ObjectOutputStream out = new ObjectOutputStream(skel.getOutputStream());
		out.writeUnshared(new RemoteMethod(method, args));
		out.flush();
		
		ObjectInputStream in = new ObjectInputStream(skel.getInputStream());
		RemoteMethodResult remoteMethodResult = (RemoteMethodResult) in.readUnshared();
		
		skel.close();
		
		if (remoteMethodResult.isException()) {
			throw (Throwable) remoteMethodResult.getObject();
		} 
		return remoteMethodResult.getObject();
	}
}
