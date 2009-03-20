package test;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.TestCase;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.ExecutorImpl;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.PeerUnitLogger;

public class ExecutorImplTest {
	private static ExecutorImpl executor ;
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
			PeerUnitLogger logger = new PeerUnitLogger("logger");
		    executor = new ExecutorImpl(tester, logger);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void setup() throws Exception{
		inititalize();
		testExecutorImpl() ;
		testRegister();
		testShouldIExecute();
	}
	
	@Test
	public void testExecutorImpl() {
		
		try {
			executor.validatePeerRange(2, -1);
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation FROM without TO");
		}
		
		try {
			executor.validatePeerRange(-1, 2);
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Annotation TO without FROM");
		}
		
		try {
			executor.validatePeerRange(-2, -2);
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"Invalid value for FROM / TO");
		}
		
		try {
			executor.validatePeerRange(2, 1);
		} catch (AnnotationFailure af) {
			assertEquals(af.getLocalizedMessage(),"The value of FROM must be smaller than TO");
		}
		
	}
	
	@Test
	public void testRegister() throws Exception {
		Class<? extends TestCase> c = TestCase.class;
		List<MethodDescription> listMethodDesc = executor.register(c);

		int id2 = executor.getTester().getId();
		TestCase testcase = executor.getTestcase();
		
		fr.inria.peerunit.parser.Test t;
		fr.inria.peerunit.parser.BeforeClass bc;
		fr.inria.peerunit.parser.AfterClass ac;
		int valid = 0;
	
		if(tester.getPeerName() != 0){
			assertNotNull(executor.getTestcase().getId());
		}
		//assertTrue(testcase.getId()==id2);
		
		System.out.println(c.getMethods().length);
		assertTrue(c.getMethods().length == 4);
		
		for(Method each : c.getMethods()) {
			t = (fr.inria.peerunit.parser.Test) each.getAnnotation(Test.class);
			if (executor.isValid(t)) {
				valid++;
				assertTrue(listMethodDesc.contains(new MethodDescription(each, t)));
			}
		}

		for(Method each : c.getMethods()) {
			bc = (fr.inria.peerunit.parser.BeforeClass) each.getAnnotation(BeforeClass.class);
			if (executor.isValid(bc)) {
				valid++;
				assertTrue(listMethodDesc.contains(new MethodDescription(each, bc)));
			}
		}

		for(Method each : c.getMethods()) {
			ac = each.getAnnotation(AfterClass.class);
			if (executor.isValid(ac)) {
				valid++;
				assertTrue(listMethodDesc.contains(new MethodDescription(each, ac)));
				assertFalse(executor.isLastMethod(each.getName()));
			}
		}
		
		assertEquals(executor.getMethods().size(), valid);
		assertEquals(listMethodDesc.size(), valid);
		}
	
		@Test
		public void testShouldIExecute() {
			int id = tester.getId();
			assertTrue(executor.shouldIExecute(id, -1, 2));
			assertFalse(executor.shouldIExecute(2, 1, 4));
			assertTrue(executor.shouldIExecute(-1, -1, -1));
			assertTrue(executor.shouldIExecute(id+1, id-2, id+2));
			assertFalse(executor.shouldIExecute(id+5, id+2, id+7));
		}
	}
	
	

