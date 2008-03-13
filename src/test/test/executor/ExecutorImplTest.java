package test.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;

public class ExecutorImplTest {

	private static ExecutorImpl executor;

	private static CoordinatorImpl coord;
	private static TesterImpl tester;
	private Logger log = Logger.getLogger("test");

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
			tester = new TesterImpl(coord);
			executor = new ExecutorImpl(tester);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}



	public ExecutorImplTest() {
		log.setLevel(Level.FINEST);
	}

	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void testBis() {

		List<MethodDescription> m = executor.register(TestData.class);
		assertEquals(8, m.size());
		assertTrue(m.contains(new MethodDescription("here","action4", 0,"Test",1000000)));
		assertFalse(m.contains(new MethodDescription("notHere","action4", 0,"Test",1000000)));
	}

	@Test
	public void testHasFailure() {
		try {
			executor.validatePeerRange(0, -1);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation FROM without TO");
		}
		try {
			executor.validatePeerRange(-1, 0);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation TO without FROM");
		}

		try {
			executor.validatePeerRange(-1, -4);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Invalid value for FROM / TO");
		}

		try {
			executor.validatePeerRange(4, 0);
			fail("Exception not catch");
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(), "The value of FROM must be smaller than TO");
		}


	}


/*	@Test
	public void testParse() {
		List<MethodDescription> l = parser.parse(data.getClass());

		System.out.println(l.size());
		assertTrue(l.size() == 7);

		for(MethodDescription each : l) {
			log.info(each.getAnnotation() + " - " + each.getName());
			//assertEquals(each.getAnnotation(), "Test");
		}

	}*/


}
