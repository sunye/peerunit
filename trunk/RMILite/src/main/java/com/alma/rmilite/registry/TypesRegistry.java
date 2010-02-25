package com.alma.rmilite.registry;

import java.rmi.Remote;

public interface TypesRegistry extends Remote {

	public Class<? extends Remote> getType(String object) throws Exception;
}
