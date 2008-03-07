package test.remote;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.BeforeClass;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.parser.ParserImpl;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public class TesterImplTest {

	private Logger log = Logger.getLogger("test");
	ParserImpl parser = new ParserImpl(-1, log);

	private static CoordinatorImpl coord;
	private static TesterImpl tester0, tester1, tester2;

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
			tester1 = new TesterImpl(coord);
			tester2 = new TesterImpl(coord);
			tester0.export(TestCaseSample.class);
			tester1.export(TestCaseSample.class);
			tester2.export(TestCaseSample.class);
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
	public void testSetId() {
		fail("Not yet implemented");
	}

	//@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testExport() {

		List<MethodDescription> methods =  parser.parse(TestCaseSample.class);


		assertEquals(1, methods.size());
		assertEquals(1, coord.getTesterMap().size());

		for (MethodDescription md : methods) {
			assertTrue(coord.getTesterMap().containsKey(md));
			assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester0));
			assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester1));
			assertTrue(coord.getTesterMap().get(md).getTesters().contains(tester2));
		}

	}

	@Test
	public void testExecute() {
		try {
			System.setProperty("executed", "nok");
			List<MethodDescription> methods = parser
					.parse(TestCaseSample.class);
			Thread tt = new Thread(tester0, "Tester 0");
			tt.start();
			for (MethodDescription md : methods) {
				tester0.execute(md);
			}
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (RemoteException e) {
			fail("Error");
		}
		tester0.executionInterrupt(true);
		assertEquals("ok", System.getProperty("executed"));
	}

	//@Test
	public void testGetPeerName() {
		fail("Not yet implemented");
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

	//@Test
	public void testPut() {
		fail("Not yet implemented");
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

}
