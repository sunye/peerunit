/**
 *
 */
package test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.test.oracle.Verdicts;

/**
 * @author sunye
 *
 */
public class CoordinatorImplTest {

	private CoordinatorImpl coord;
	private Tester tester;
	private List<MethodDescription> methods;
	private Thread coordination;
	private static final Logger log = Logger.getLogger(CoordinatorImpl.class.getName());
	
	
	@BeforeClass
	public static void init() {
		Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
		log.setLevel(Level.INFO);
	}
	
	@Before
	public void setup() {
		tester = mock(Tester.class);
		methods = new ArrayList<MethodDescription>(3);
		methods.add(new MethodDescription("first", "tc1", 1, "Test", 10));
		methods.add(new MethodDescription("second", "tc1", 2, "Test", 10));
		methods.add(new MethodDescription("third", "tc1", 3, "Test", 10));
		
		coord = new CoordinatorImpl(1);
		coordination = new Thread(coord, "Coordinator");
		
		coordination.start();
	}
	
	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#CoordinatorImpl(int)}.
	 */
	@Test
	public void testCoordinatorImplInt() {
		coord = new CoordinatorImpl(0);
		assertNotNull(coord);
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#main(java.lang.String[])}.
	 */
	//@Test
	public void testMain() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#register(fr.inria.peerunit.Tester, java.util.List)}.
	 */
	@Test
	public void testRegister() {
		try {
			coord.register(tester, methods);
			for (MethodDescription each : methods) {
				assertTrue(coord.getTesterMap().containsKey(each));
			}
			for (int i = 0; i < methods.size(); i++) {
				Thread.sleep(100);
				coord.executionFinished();
			}

			coord.quit(tester, Verdicts.PASS);
			coordination.join();

			for (MethodDescription each : methods) {
				verify(tester).execute(each);
			}
			
		} catch (RemoteException e) {
			fail("Remote Error");
		} catch (InterruptedException e) {
			fail("InterruptedException");
		}
		
		


	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#run()}.
	 */
	//@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#getNewId(fr.inria.peerunit.Tester)}.
	 */
	//@Test
	public void testGetNewId() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#executionFinished()}.
	 */
	//@Test
	public void testGreenLight() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#quit(fr.inria.peerunit.Tester, boolean, fr.inria.peerunit.test.oracle.Verdicts)}.
	 */
	//@Test
	public void testQuit() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#put(java.lang.Integer, java.lang.Object)}.
	 */
	//@Test
	public void testPut() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#get(java.lang.Integer)}.
	 */
	//@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#getCollection()}.
	 */
	//@Test
	public void testGetCollection() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#containsKey(java.lang.Object)}.
	 */
	//@Test
	public void testContainsKey() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#clearCollection()}.
	 */
	//@Test
	public void testClearCollection() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#getTesterMap()}.
	 */
	//@Test
	public void testGetTesterMap() {
		fail("Not yet implemented");
	}

}
