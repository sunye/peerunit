package com.alma.rmilite.server;

import java.rmi.Remote;

public interface SkeletonProvider {

	public boolean isExported(Remote object);
	
	public int getPort(Remote object);
}
