package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

/** The result of an operation called on a server<br />
 * It is sent back by the server to the Client<br />
 * It can be created by a client as a result of a method invocation
 */
public class RemoteMethodResultImpl implements Serializable, RemoteMethodResult {

	private static final long serialVersionUID = 1950048720714271916L;
	
	private Object result;
	private Class<?> resultClass;
	
	public RemoteMethodResultImpl(Method method) {	
		this.resultClass = method.getReturnType();
	}
	
	public void setResult(Object result) throws NotSerializableException, UnexportedException {
		if (result != null) {
			RemoteObjectManager manager = (RemoteObjectManager) RemoteObjectProvider.instance;
			
			if (result instanceof Remote) {
				Remote remoteResult = (Remote) result;
				if (manager.isExported(remoteResult)) {
					this.result = new InetSocketAddress(manager.getPort(remoteResult));
				} else {
					throw new UnexportedException("Unexported result");
				}
			} else if (!(result instanceof Serializable)) {
				throw new NotSerializableException("Not serializable result");
			} else {
				this.result = result;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.alma.rmilite.RemoteMethod#getObject()
	 */
	public Object getObject() {
		if (Remote.class.isAssignableFrom(this.resultClass)) {
			this.result = StubFactory.createStub((InetSocketAddress) this.result, RemoteObject.class);
		}
		return this.result;
	}

}
