package fr.inria.peerunit;

import java.rmi.RemoteException;
import java.util.Map;

import fr.inria.peerunit.btree.TreeTesterImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public abstract class TestCaseImpl implements TestCase {

	private int id;
	private VolatileTester vt;
	private StorageTester tester;	

	public void setTester(TesterImpl ti) {
		tester = ti;
		try {
			id = ti.getPeerName();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void setTester(TreeTesterImpl tt) {	
		vt = tt;		
		id = tt.getID();		
	}

	public int getPeerId() {
		return id;
	}

	public int getPeerName() throws RemoteException  {
		return id;
	}

	public void put(Integer key,Object object) {
		tester.put(key, object);
	}

	public  Map<Integer,Object> getCollection() throws RemoteException {
		return  tester.getCollection();
	}

	public void kill() {
		if(tester!=null)
			vt=tester;
			
		vt.kill();
	}

	public Object get(Integer key)  {
		return tester.get(key);
	}

	public boolean containsKey(Object key)throws RemoteException {
		return tester.containsKey(key);
	}

	public void clear() {
		tester.clear();
	}
}
