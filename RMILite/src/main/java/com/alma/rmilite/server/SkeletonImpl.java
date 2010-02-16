package com.alma.rmilite.server;

public class SkeletonImpl implements Skeleton {
	
	private Object remoteObject;
	
	public SkeletonImpl(int port, Object remoteObject) {
		this(remoteObject);
		//TODO
	}
	
	public SkeletonImpl(Object remoteObject) {
		this.remoteObject = remoteObject;	
		//TODO
	}
	
	@Override
	public boolean close() {
		return false;
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void open() {
		// TODO Auto-generated method stub
		
	}
}
