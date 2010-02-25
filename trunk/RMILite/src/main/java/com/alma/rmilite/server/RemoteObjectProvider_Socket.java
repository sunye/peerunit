package com.alma.rmilite.server;

import java.io.IOException;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

public class RemoteObjectProvider_Socket implements RemoteObjectProvider, SkeletonProvider {
	
	private Map<Remote, Skeleton> skeletons;

	public RemoteObjectProvider_Socket() {
		this.skeletons = new HashMap<Remote, Skeleton>();
	}
	
	@Override
	public Remote exportObject(Remote object) throws IOException {
		Skeleton skel = SkeletonFactory.createSkeleton(object);
		this.skeletons.put(object, skel);
		return object;
	}

	@Override
	public Remote exportObject(Remote object, int port) throws IOException {
		Skeleton skel = SkeletonFactory.createSkeleton(port, object);
		this.skeletons.put(object, skel);
		return object;
	}

	@Override
	public boolean unexportObject(Remote object) throws IOException {
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
