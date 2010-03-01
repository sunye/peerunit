package com.alma.rmilite;

import java.io.NotSerializableException;
import java.lang.reflect.Method;

/**
 * Provides {@link RemoteMethod} instances and {@link RemoteMethodResult}
 * instances.<br/>
 * <br/>
 * Each RemoteMethod instance contain a {@link SerializableMethod}.
 * 
 * @see RemoteMethod
 * @see RemoteMethodResult
 */
public class RemoteMethodFactory {

	/**
	 * Returns a RemoteMethod
	 * 
	 * @param method
	 * @param args
	 * @return
	 * @throws NotSerializableException
	 * @throws UnexportedException
	 */
	static public RemoteMethod createRemoteMethod(Method method, Object[] args)
			throws NotSerializableException, UnexportedException {
		return new RemoteMethodImpl(new SerializableMethodImpl(method), args);
	}

	static public RemoteMethodResult createRemoteMethodResult(Method method,
			Object result) throws NotSerializableException, UnexportedException {
		return new RemoteMethodResultImpl(method, result);
	}
}
