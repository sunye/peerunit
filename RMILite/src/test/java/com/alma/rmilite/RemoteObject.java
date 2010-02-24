package com.alma.rmilite;

import java.rmi.Remote;

public interface RemoteObject extends Remote{
	
	public int getNbCall();
}
