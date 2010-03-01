package com.alma.rmilite.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;

import com.alma.rmilite.RemoteMethodFactory;
import com.alma.rmilite.RemoteMethodResult;
import com.alma.rmilite.io.RemoteProxy;
import com.alma.rmilite.io.IOManager;

/**
 * Each stub instance has an associated {@link InvocationHandler}.
 * 
 * @see InvocationHandler
 */
public class Stub implements InvocationHandler {

	/**
	 * Reference to the remote object.
	 */
	private InetSocketAddress reference;

	public Stub(InetSocketAddress adress) {
		this.reference = adress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		/* Connect to remote object */
		RemoteProxy skeleton = IOManager.instance
				.getRemoteProxy(this.reference);

		/* Send method and arguments */
		skeleton.sendObject(RemoteMethodFactory.createRemoteMethod(method, args));

		/* Receive the result of the method */
		Object result = skeleton.receiveObject();

		/* Close the connection */
		skeleton.close();

		/* Process the result */
		if (result instanceof Exception) {
			throw (Exception) result; // an exception occurs during the
									  // execution of the remote method
		} else if (result instanceof RemoteMethodResult) {
			return ((RemoteMethodResult) result).getObject();
		} else {
			throw new RemoteException("Invalid data !");
		}
	}

	/**
	 * Returns the host and the port of the remote object
	 * @return the reference
	 */
	public InetSocketAddress getReference() {
		return reference;
	}
}
