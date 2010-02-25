package com.alma.rmilite;

import java.rmi.RemoteException;

import com.alma.rmilite.server.RemoteObjectProvider;

public class RemoteObjectImpl implements RemoteObject {
		
	public int nb = -1;

	@Override
	public int getNb() throws RemoteException {
		return nb;
	}

	@Override
	public void incNb() throws RemoteException {
		nb++;
		
	}

	@Override
	public void setNb(RemoteObject nb) throws RemoteException {
		this.nb = nb.getNb();
		
	}

	@Override
	public RemoteObject add2Nb(RemoteObject nb1, int nb2) {
		RemoteObject newNb = new RemoteObjectImpl();
		RemoteObjectProvider remoteObjectProvider = RemoteObjectProvider.instance;
		try {
			remoteObjectProvider.exportObject(newNb,0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			newNb.setNb(nb + nb1.getNb() + nb2);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newNb;
	}

	@Override
	public void setNb(int nb) throws RemoteException {
		this.nb = nb;
	}
}
