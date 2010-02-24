package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.SkeletonProvider;

/** The result of an operation called on a server<br />
 * It is sent back by the server to the Client<br />
 * It can be created by a client as a result of a method invocation
 */
public class RemoteMethodResult implements Serializable {

	private static final long serialVersionUID = 1950048720714271916L;
	
	private Object result;
	private Class<?> resultClass;
	private boolean exception;
	
	public RemoteMethodResult(Method method) throws NotSerializableException, UnexportedException {	
		this.resultClass = method.getReturnType();
	}
	
	public void setResult(Object result, boolean exception) throws NotSerializableException, UnexportedException {
		this.exception = exception;
		SkeletonProvider skeletonProvider = (SkeletonProvider) RemoteObjectProvider.instance;
		
		if (exception) {
			this.result = result;
		} else if (result instanceof Remote) {
			Remote remoteResult = (Remote) result;
			if ( skeletonProvider.isExported(remoteResult)) {
				this.result = new InetSocketAddress(skeletonProvider.getPort(remoteResult));
			} else {
				throw new UnexportedException("Unexported result");
			}
		} else if (!(result instanceof Serializable)) {
			throw new NotSerializableException("Not serializable result");
		} else {
			this.result = result;
		}
	}
	
	/**
	 * @return the object contained in that container is an exception?
	 */
	public boolean isException() {
		return this.exception;
	}
	
	/**
	 * @return the object the network layer brougth us
	 */
	public Object getObject() {
		if (Remote.class.isAssignableFrom(this.resultClass)) {
			this.result = StubFactory.createStub((InetSocketAddress) this.result, this.resultClass);
		}
		return this.result;
	}

}
