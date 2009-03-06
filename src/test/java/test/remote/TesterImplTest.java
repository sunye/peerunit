package test.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

public class TesterImplTest {

	private static ExecutorImpl executor;

	private static CoordinatorImpl coord;
	private static TesterImpl tester0, tester1, tester2;
	
	private static PeerUnitLogger LOG = new PeerUnitLogger("test");

	@BeforeClass
	public static void  inititalize() {
		System.setProperty("tester.peers","3");
		System.setProperty("tester.log.dateformat","yyyy-MM-dd");
		System.setProperty("tester.log.timeformat","HH:mm:ss.SSS");
		System.setProperty("tester.log.level","FINEST");
		System.setProperty("tester.logfolder","/tmp/");
		System.setProperty("tester.log.delimiter","|");
		System.setProperty("tester.waitForMethod","500");
		try {
			coord = new CoordinatorImpl(3);
			new Thread(coord, "Coordinator").start();
			tester0 = new TesterImpl(coord);
			executor = new ExecutorImpl(tester0, LOG);
			tester1 = new TesterImpl(coord);
			tester2 = new TesterImpl(coord);
			tester0.export(Sample.class);
			tester1.export(Sample.class);
			tester2.export(Sample.class);
			//new Thread(tester0, "Tester 0").start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	//@Test
	public void testTesterImpl() {
		fail("Not yet implemented");
	}


	//@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegister() {
		MethodDescription md = new MethodDescription("first", "action1", 1,
				"Test", 1000);

		assertEquals(1, coord.getTesterMap().size());

		assertTrue(coord.getTesterMap().containsKey(md));
		assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester0));
		assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester1));
		assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester2));

	}

	@Test
	public void testExecute() {
		try {
			System.setProperty("executed", "nok");
			List<MethodDescription> methods = executor
					.register(Sample.class);
			Thread tt = new Thread(tester0, "Tester 0");
			tt.start();
			for (MethodDescription md : methods) {
				tester0.execute(md);
			}
			try {
				Thread.sleep(600);
				Thread.yield();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			fail("Error");
		}
		tester0.executionInterrupt();
		assertEquals("ok", System.getProperty("executed"));
	}

	@Test
	public void testGetPeerName() {
		assertEquals(0, tester0.getId());
		assertEquals(1, tester1.getId());
		assertEquals(2, tester2.getId());
	}

	@Test
	public void testGetId() {
		try {
			assertEquals(0, tester0.getPeerName());
			assertEquals(1, tester1.getPeerName());
			assertEquals(2, tester2.getPeerName());
		} catch (RemoteException e) {
			fail("Communication error");
		}
	}

	//@Test
	public void testKill() {
		fail("Not yet implemented");
	}

	@Test
	public void testPut() {
		tester0.put(0, "zero");
		tester1.put(1, "one");
		tester2.put(2, "two");
		assertEquals("zero", tester2.get(0));
		assertEquals("one", tester0.get(1));
		assertEquals("two", tester1.get(2));
	}

	//@Test
	public void testClear() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGet() {
		fail("Not yet implemented");
	}

	//@Test
	public void testGetCollection() {
		fail("Not yet implemented");
	}

	//@Test
	public void testContainsKey() {

		fail("Not yet implemented");
	}

	@Test
	public void mysets() {
		Set<Tester> myset = new HashSet<Tester>();

		assertTrue(myset.add(tester0));
		assertTrue(!myset.add(tester0));
		assertTrue(!myset.add(tester0));

	}

}
