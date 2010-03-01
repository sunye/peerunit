package com.alma.rmilite;

import java.lang.reflect.Method;

public class SerializableMethodImpl implements SerializableMethod {

	private static final long serialVersionUID = -6596635905397715978L;

	private Class<?> classe;
	private String name;
	private Class<?>[] parameterTypes;

	public SerializableMethodImpl(Method method) {
		this.classe = method.getDeclaringClass();
		this.name = method.getName();
		this.parameterTypes = method.getParameterTypes();
	}

	/* (non-Javadoc)
	 * @see com.alma.rmilite.SerializableMethod#getMethod()
	 */
	public Method getMethod() throws SecurityException, NoSuchMethodException {
		return this.classe.getMethod(name, parameterTypes);
	}
}
