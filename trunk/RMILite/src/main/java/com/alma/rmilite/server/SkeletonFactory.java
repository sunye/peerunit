package com.alma.rmilite.server;

public class SkeletonFactory {
	
	public static Skeleton createSkeleton(Object remoteObject) {
		return new SkeletonImpl(remoteObject);
	}
	
	public static Skeleton createSkeleton(int port, Object remoteObject) {
		return new SkeletonImpl(port, remoteObject);
	}
}
