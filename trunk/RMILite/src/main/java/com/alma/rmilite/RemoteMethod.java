package com.alma.rmilite;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;

import com.alma.rmilite.server.RemoteObjectProvider;

/**
 * Allows to send a method call to a remote object.<br/>
 * <br/>
 * Must contain the serializable arguments and a serializable object method.
 * 
 * @see SerializableMethod
 */
public interface RemoteMethod extends Serializable {

	/**
	 * Processes a method invocation on a remote object and returns the result.
	 * This method will be invoked by the {@link RemoteObjectProvider} instance
	 * when a method is invoked on a stub instance that it is associated with
	 * the remote {@code object}.
	 * 
	 * @param object
	 *            - the remote object
	 * @return the result, an exception or a {@link RemoteMethodResult} instance
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws NotSerializableException
	 * @throws IllegalArgumentException
	 * @throws UnexportedException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * 
	 * @see RemoteMethodResult
	 */
	public Object invoke(Remote object) throws SecurityException,
			NoSuchMethodException, NotSerializableException,
			IllegalArgumentException, UnexportedException,
			IllegalAccessException, InvocationTargetException;
}