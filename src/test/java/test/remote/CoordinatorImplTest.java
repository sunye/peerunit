/**
 *
 */
package test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.junit.Test;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;

/**
 * @author sunye
 *
 */
public class CoordinatorImplTest {

	private CoordinatorImpl coord;
	private Tester tester;
	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#CoordinatorImpl()}.
	 */
	@Test
	public void testCoordinatorImpl() {
		coord = new CoordinatorImpl();
		assertNotNull(coord);
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
		MethodDescription md = new MethodDescription("name", "tc1", 1, "Test", 10);
		ArrayList<MethodDescription> methods = new ArrayList<MethodDescription>();
		methods.add(md);
		try {
			coord = new CoordinatorImpl(1);
			tester = new TesterImpl(coord);
			coord.register(tester, methods);
			assertTrue(coord.getRegisteredTesters().contains(tester));

		} catch (RemoteException e) {
			fail("Remote Error");
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
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#greenLight()}.
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
