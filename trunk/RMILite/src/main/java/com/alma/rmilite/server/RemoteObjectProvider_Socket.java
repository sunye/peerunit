package com.alma.rmilite.server;

import java.rmi.Remote;
import java.util.Map;

public class RemoteObjectProvider_Socket implements RemoteObjectProvider, SkeletonProvider {
	
	private Map<Remote, Skeleton> skeletons;

	private void putSkeleton(Remote obj, Skeleton skel) {
		skel.open();		
		this.skeletons.put(obj, skel);
	}
	
	@Override
	public Remote exportObject(Remote object) {
		Skeleton skel = SkeletonFactory.createSkeleton(object);
		this.putSkeleton(object, skel);
		return object;
	}

	@Override
	public Remote exportObject(Remote object, int port) {
		Skeleton skel = SkeletonFactory.createSkeleton(port, object);
		this.putSkeleton(object, skel);
		return object;
	}

	@Override
	public boolean unexportObject(Remote object) {
		Skeleton skel = this.skeletons.remove(object);
		return skel.close();
	}

	@Override
	public int getPort(Remote object) {
		return this.skeletons.get(object).getPort();
	}

	@Override
	public boolean isExported(Remote object) {
		return this.skeletons.containsKey(object);
	}
}
