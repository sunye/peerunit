package com.alma.rmilite;

import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;

public class SerializableRemoteObjectImpl implements SerializableRemoteObject {

	private static final long serialVersionUID = 614211378151954592L;
	
	private Class<?>[] interfaces;
	private InetSocketAddress reference;

	public SerializableRemoteObjectImpl(Remote object,
			InetSocketAddress reference) {
		this.interfaces = object.getClass().getInterfaces();
		this.reference = reference;
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.SerializableRemoteObject#getObject()
	 */
	@Override
	public Remote getObject() {
		return (Remote) StubFactory.createStub(reference, interfaces);
	}
}
