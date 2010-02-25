package com.alma.rmilite;

import java.io.NotSerializableException;
import java.lang.reflect.Method;

public class RemoteMethodFactory {

	static public RemoteMethod createRemoteMethod(Method method, Object[] args) throws NotSerializableException, UnexportedException {
		return new RemoteMethodImpl(method, args);
	}
}
