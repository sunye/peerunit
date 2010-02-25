package com.alma.rmilite;

import java.io.Serializable;
import java.lang.reflect.Method;

public class SerializableMethod implements Serializable {
	
	private static final long serialVersionUID = -6596635905397715978L;
	
	private Class<?> classe;
	private String name;
	private Class<?>[] parameterTypes;
	
	public SerializableMethod(Method method) {
		this.classe = method.getDeclaringClass();
		this.name = method.getName();
		this.parameterTypes = method.getParameterTypes();
	}

	public Method getMethod() throws SecurityException, NoSuchMethodException {
		return this.classe.getMethod(name, parameterTypes);
	}
}
