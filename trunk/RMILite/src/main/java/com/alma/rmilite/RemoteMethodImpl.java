package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.rmi.Remote;

import com.alma.rmilite.client.Stub;
import com.alma.rmilite.client.StubFactory;
import com.alma.rmilite.server.RemoteObjectProvider;
import com.alma.rmilite.server.RemoteObjectManager;

public class RemoteMethodImpl implements RemoteMethod {

	private static final long serialVersionUID = 3894627959938764399L;

	private SerializableMethod serializableMethod;
	private Object[] args;

	public RemoteMethodImpl(SerializableMethod method, Object[] args)
			throws UnexportedException, NotSerializableException {
		this.serializableMethod = method;
		this.args = args;

		/* Serializes remote arguments. */
		this.args2refs();
	}

	/**
	 * Serializes remote arguments.<br/>
	 * <br/>
	 * Transforms a remote argument in reference.
	 * 
	 * @throws UnexportedException
	 * @throws NotSerializableException
	 */
	private void args2refs() throws UnexportedException,
			NotSerializableException {
		if (args != null) { // method without argument
			RemoteObjectManager manager = (RemoteObjectManager) RemoteObjectProvider.instance;

			for (int i = 0; i < this.args.length; i++) {
				if (this.args[i] instanceof Remote) { // if the argument is a
														// remote object
					Remote remoteArg = (Remote) args[i];
					if (StubFactory.isStub(remoteArg)) { // if the remote
															// argument is
															// already a stub
						args[i] = StubFactory.getStubReference(remoteArg);
					} else if (manager.isExported(remoteArg)) { // else if the
																// remote
																// argument is
																// exported
						args[i] = new InetSocketAddress(manager
								.getPort(remoteArg)); // creates a reference
					} else {
						throw new UnexportedException("Unexported argument");
					}
				} else if (!(args[i] instanceof Serializable)) {
					throw new NotSerializableException(
							"Not serializable argument");
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.alma.rmilite.RemoteMethod#invoke(java.rmi.Remote)
	 */
	public Object invoke(Remote object) throws SecurityException,
			NoSuchMethodException, NotSerializableException,
			IllegalArgumentException, UnexportedException,
			IllegalAccessException, InvocationTargetException {
		/* Deserializes the method. */
		Method method = this.serializableMethod.getMethod();

		/* Transforms the remote arguments in stub. */
		this.args2stubs(method.getParameterTypes());

		/* Invokes the method and returns the result. */
		return RemoteMethodFactory.createRemoteMethodResult(method, method
				.invoke(object, this.args));
	}

	/**
	 * Transforms remote arguments in {@link Stub}.
	 * 
	 * @param argTypes
	 *            - arguments
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private void args2stubs(Class<?>[] argTypes) throws SecurityException,
			NoSuchMethodException {
		if (args != null) { // method without arguments
			for (int i = 0; i < args.length; i++) {
				if (Remote.class.isAssignableFrom(argTypes[i])) { // if the
																	// argument
																	// is a
																	// remote
																	// object
					this.args[i] = StubFactory.createStub(
							(InetSocketAddress) this.args[i], argTypes[i]);
				}
			}
		}
	}
}
