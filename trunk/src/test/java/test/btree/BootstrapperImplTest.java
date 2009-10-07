package test.btree;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.btree.BootstrapperImpl;
import fr.inria.peerunit.rmi.tester.DistributedTester;
import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImplTest {

	private BootstrapperImpl bootstrapper;
	private Properties properties;
	private TesterUtil defaults;

	@Before
	public void setup() {
		properties = new Properties();
		properties.setProperty("tester.peers", "3");
		properties.setProperty("test.treeStrategy", "1");
		defaults = new TesterUtil(properties);
		bootstrapper = new BootstrapperImpl(defaults);
	}

	// @Test
	public void testBootstrapperIml() {
		/*
		 * properties.setProperty("tester.peers", "1"); TesterUtil defaults =
		 * new TesterUtil(properties); fr.inria.peerunit.Bootstrapper b = new
		 * BootstrapperImpl(defaults); Node node = mock(Node.class); try {
		 * b.register(node); } catch (RemoteException e) { fail(e.getMessage());
		 * }
		 * 
		 * assertTrue(b != null);
		 */
	}

	@Test
	public void testRegister() {
		int id = 0;
		try {
			for (int i = 0; i < 5; i++) {
				DistributedTester tester = mock(DistributedTester.class);
				id = bootstrapper.register(tester);
				assertTrue(id == i + 1);
			}
			
		} catch (RemoteException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRemoteRegister() {
        try {
            Registry registry = LocateRegistry.createRegistry(1099);
            Bootstrapper stub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper, 0);
			registry.bind("BootTest", stub);
			
			Bootstrapper remoteBoot = (Bootstrapper) registry.lookup("BootTest");
			
			DistributedTester tester = new DistributedTester(remoteBoot, null, defaults);
			tester.register();
			
			assertTrue(tester.getId() == 1);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Test
	public void testGetRegistered() {
		// fail("Not yet implemented");
	}

	@Test
	public void testIsRoot() {
		// fail("Not yet implemented");
	}

}
