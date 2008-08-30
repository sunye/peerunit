package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Map;

public interface StorageTester {
	public void put(Integer key,Object object) ;

	public  Map<Integer,Object> getCollection() throws RemoteException;

	public void kill() ;

	public Object get(Integer key) ;

	public boolean containsKey(Object key)throws RemoteException;
	
	public void clear() ;

}
