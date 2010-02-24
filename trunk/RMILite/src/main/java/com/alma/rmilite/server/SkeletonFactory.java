package com.alma.rmilite.server;

import java.io.IOException;
import java.rmi.Remote;

public class SkeletonFactory {
	
	public static Skeleton createSkeleton(Remote remoteObject) throws IOException {
		return new SkeletonImpl(remoteObject);
	}
	
	public static Skeleton createSkeleton(int port, Remote remoteObject) throws IOException {
		return new SkeletonImpl(port, remoteObject);
	}
}
