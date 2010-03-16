package fr.univnantes.alma.rmilite;

import java.rmi.RemoteException;

import fr.univnantes.alma.rmilite.server.RemoteObjectProvider;

public class RemoteObjectTestImpl implements RemoteObjectTest {

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
    public void setNb(RemoteObjectTest nb) throws RemoteException {
	this.nb = nb.getNb();

    }

    @Override
    public RemoteObjectTest add2Nb(RemoteObjectTest nb1, int nb2)
	    throws RemoteException {
	RemoteObjectTest newNb = new RemoteObjectTestImpl();

	// XXX CHEAT = ON
	RemoteObjectProvider remoteObjectProvider = new ConfigManagerSocketStrategy()
		.getRemoteObjectProvider();
	// XXX CHEAT = OFF

	try {
	    remoteObjectProvider.exportObject(newNb, 0);
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
