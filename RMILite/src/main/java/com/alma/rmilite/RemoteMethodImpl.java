package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

/** the call to a distant object's method<br />
 * It allows a client to send an invocation on a network object*/
public class RemoteMethodImpl implements Serializable, RemoteMethod {

	private static final long serialVersionUID = 3894627959938764399L;

	private SerializableMethod serializableMethod;
	private Object[] args;
	
	public RemoteMethodImpl(Method method, Object[] args) throws UnexportedException, NotSerializableException {
		this.serializableMethod = new SerializableMethod(method);
		this.args = args;
		this.args2refs();
	}
	
	private void args2refs() throws UnexportedException, NotSerializableException {
		if (args != null) {
			RemoteObjectManager manager = (RemoteObjectManager) RemoteObjectProvider.instance;
			
			for (int i=0; i<this.args.length; i++) {
				if (this.args[i] instanceof Remote) {
					Remote remoteArg = (Remote) args[i];
					if (manager.isExported(remoteArg)) {
						args[i] = new InetSocketAddress(manager.getPort(remoteArg));
					} else {
						throw new UnexportedException("Unexported argument");
					}
				} else if (!(args[i] instanceof Serializable)) {
					throw new NotSerializableException("Not serializable argument");
				}
			}
		}
	}
	
	public Object invoke(Object object) throws SecurityException, NoSuchMethodException, NotSerializableException, IllegalArgumentException, UnexportedException, IllegalAccessException, InvocationTargetException {
		Method method = this.serializableMethod.getMethod();
		RemoteMethodResultImpl remoteMethodResult = new RemoteMethodResultImpl(method);
		this.args2stubs();
		
		remoteMethodResult.setResult(method.invoke(object, this.args));

		return remoteMethodResult;
	}
	
	private void args2stubs() throws SecurityException, NoSuchMethodException {
		if (args != null) {
			Class<?>[] argTypes = this.serializableMethod.getMethod().getParameterTypes();
			for (int i=0; i<0; i++) {
				if (Remote.class.isAssignableFrom(argTypes[i])) {
					this.args[i] = StubFactory.createStub((InetSocketAddress) this.args[i], argTypes[i]);
				}
			}
		}
	}
}
