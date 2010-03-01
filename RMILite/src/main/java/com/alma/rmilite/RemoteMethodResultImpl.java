package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

public class RemoteMethodResultImpl implements RemoteMethodResult {

	private static final long serialVersionUID = 1950048720714271916L;

	private Object result;
	private Class<?> resultClass;

	public RemoteMethodResultImpl(Method method, Object result)
			throws UnexportedException, NotSerializableException {
		this.resultClass = method.getReturnType();
		this.result = result;
		
		/* Serializes remote result. */
		this.result2ref();
	}
	
	/**
	 * Serializes remote result.<br/>
	 * <br/>
	 * Transforms a remote result in reference.
	 * 
	 * @throws UnexportedException
	 * @throws NotSerializableException
	 */
	private void result2ref() throws UnexportedException, NotSerializableException {
		if (result != null) { // if the method doesn't return void
			RemoteObjectManager manager = (RemoteObjectManager) RemoteObjectProvider.instance;

			if (result instanceof Remote) { // if the result is a remote object
				Remote remoteResult = (Remote) result;
				if (StubFactory.isStub(remoteResult)) { // if the remote result is already a stub
					this.result = StubFactory.getStubReference(remoteResult);
				} else if (manager.isExported(remoteResult)) { // else if the remote result is exported
					this.result = new InetSocketAddress(manager
							.getPort(remoteResult)); // creates a reference
				} else {
					throw new UnexportedException("Unexported result");
				}
			} else if (!(result instanceof Serializable)) {
				throw new NotSerializableException("Not serializable result");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.RemoteMethod#getObject()
	 */
	public Object getObject() {
		if (Remote.class.isAssignableFrom(this.resultClass)) { // if the result is a remote object
			this.result = StubFactory.createStub(
					(InetSocketAddress) this.result, RemoteObject.class); // returns a stub
		}
		return this.result;
	}

}
