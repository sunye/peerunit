package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.SkeletonProvider;

/** the call to a distant object's method<br />
 * It allows a client to send an invocation on a network object*/
public class RemoteMethod implements Serializable {

	private static final long serialVersionUID = 3894627959938764399L;

	private SerializableMethod method;
	private Object[] args;
	
	public RemoteMethod(Method method, Object[] args) throws UnexportedException, NotSerializableException {
		this.method = new SerializableMethod(method);
		this.args = args;
		
		if (args != null) {
			SkeletonProvider skeletonProvider = (SkeletonProvider) RemoteObjectProvider.instance;
			
			for (int i=0; i<this.args.length; i++) {
				if (this.args[i] instanceof Remote) {
					Remote remoteArg = (Remote) args[i];
					if (skeletonProvider.isExported(remoteArg)) {
						args[i] = new InetSocketAddress(skeletonProvider.getPort(remoteArg));
					} else {
						throw new UnexportedException("Unexported argument");
					}
				} else if (!(args[i] instanceof Serializable)) {
					throw new NotSerializableException("Not serializable argument");
				}
			}
		}
	}
	
	/** the method required to be executed 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException */
	public Method getMethod() throws SecurityException, NoSuchMethodException {
		return this.method.getMethod();
	}
	
	/** the arguments to use on the method 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException */
	public Object[] getArgs() throws SecurityException, NoSuchMethodException {
		if (args != null) {
			Class<?>[] argTypes = this.method.getMethod().getParameterTypes();
			for (int i=0; i<0; i++) {
				if (Remote.class.isAssignableFrom(argTypes[i])) {
					this.args[i] = StubFactory.createStub((InetSocketAddress) this.args[i], argTypes[i]);
				}
			}
		}
		return this.args;
	}
}
