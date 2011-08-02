package fr.univnantes.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.Remote;

import fr.univnantes.alma.rmilite.client.Stub;

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
		Class<?>[] argTypes = this.serializableMethod.getParameterTypes();
		for (int i = 0; i < argTypes.length; i++) {
			if (Remote.class.isAssignableFrom(argTypes[i])) { // if the argument is a
																// remote object
				args[i] = RemoteMethodFactory
						.createSerializableRemoteObject((Remote) args[i]);
			} else if (!(Serializable.class.isAssignableFrom(argTypes[i]))) {
				throw new NotSerializableException("Not serializable argument");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.univnantes.alma.rmilite.RemoteMethod#invoke(java.rmi.Remote)
	 */
	@Override
	public Object invoke(Remote object) throws SecurityException,
			NoSuchMethodException, NotSerializableException,
			IllegalArgumentException, UnexportedException,
			IllegalAccessException, InvocationTargetException {
		/* Transforms the remote arguments in stub. */
		this.args2stubs();
		
		/* Deserializes the method. */
		Method method = this.serializableMethod.getMethod();

		/* Invokes the method. */
		Object result = method.invoke(object, this.args);
		
		/* And returns the result. */
		Class<?> returnType = method.getReturnType();
		if (Remote.class.isAssignableFrom(returnType)) {
			return RemoteMethodFactory.createSerializableRemoteObject(object);
		} else if (Serializable.class.isAssignableFrom(returnType)) {
			return result;
		} else {
			throw new NotSerializableException("Not serializable result");
		}		
	}

	/**
	 * Transforms remote arguments in {@link Stub}.
	 * 
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private void args2stubs() throws SecurityException,
			NoSuchMethodException {
		Class<?>[] argTypes = this.serializableMethod.getParameterTypes();
		if (args != null) { // method without arguments
			for (int i = 0; i < args.length; i++) {
				if (Remote.class.isAssignableFrom(argTypes[i])) { // if the
																	// argument
																	// is a
																	// remote
																	// object
					/* Deserializes the remote object */
					this.args[i] = ((SerializableRemoteObject) args[i]).getObject(); // returns a stub
				}
			}
		}
	}
}
