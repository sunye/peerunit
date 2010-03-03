package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;

public class RemoteMethodResultImpl implements RemoteMethodResult {

	private static final long serialVersionUID = 1950048720714271916L;

	private Object result;
	private Class<?> resultClass;

	public RemoteMethodResultImpl(Method method, Object result)
			throws UnexportedException, NotSerializableException {
		this.resultClass = method.getReturnType();
		this.result = result;
		
		/* Serializes the remote result. */
		this.result2ref();
	}
	
	/**
	 * Serializes the remote result.<br/>
	 * <br/>
	 * Transforms a remote result in reference.
	 * 
	 * @throws UnexportedException
	 * @throws NotSerializableException
	 */
	private void result2ref() throws UnexportedException, NotSerializableException {
		if (result != null) { // if the method doesn't return void
			if (result instanceof Remote) { // if the result is a remote object
				Remote remoteResult = (Remote) result;
				if (StubFactory.isStub(remoteResult)) { // if the remote result is already a stub
					this.result = StubFactory.getStubReference(remoteResult);
				} else { // else the remote result is serialized
					this.result = RemoteMethodFactory.createSerializableRemoteObject(remoteResult);
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
			this.result = ((SerializableRemoteObject) this.result).getObject(); // returns a stub
		}
		return this.result;
	}

}
