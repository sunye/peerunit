package test.remote;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;


import fr.inria.peerunit.Tester;
import fr.inria.peerunit.rmi.tester.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;


public class DistributedTesterTest {
	
	@Test
	public void testSerialization() {
        Registry registry;
		try {
			registry = LocateRegistry.createRegistry(1099);
	        DistributedTester dt = new DistributedTester(null, null, TesterUtil.instance);
	        Tester stub = (Tester) UnicastRemoteObject.exportObject(dt, 0);
			registry.bind("DT", stub);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
