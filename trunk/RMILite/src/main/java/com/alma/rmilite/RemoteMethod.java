package com.alma.rmilite;

import java.io.NotSerializableException;
import java.lang.reflect.InvocationTargetException;

public interface RemoteMethod {

	public Object invoke(Object object) throws SecurityException,
			NoSuchMethodException, NotSerializableException,
			IllegalArgumentException, UnexportedException,
			IllegalAccessException, InvocationTargetException;
}