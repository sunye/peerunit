/**
 *
 */
package test.remote;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;

import fr.inria.peerunit.MessageType;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.test.oracle.Verdicts;
import fr.inria.peerunit.util.TesterUtil;

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
		log.setLevel(Level.FINE);
	}
	
	@Before
	public void setup() {
		tester = mock(Tester.class);
		methods = new ArrayList<MethodDescription>(3);
		methods.add(new MethodDescription("first", "tc1", 1, "Test", 10));
		methods.add(new MethodDescription("second", "tc1", 2, "Test", 10));
		methods.add(new MethodDescription("third", "tc1", 3, "Test", 10));

	}
	
	/**
	 * Test method for {@link fr.inria.peerunit.rmi.coord.CoordinatorImpl#CoordinatorImpl(int)}.
	 */
	@Test
	public void testCoordinatorImplInt() {
		coord = new CoordinatorImpl(TesterUtil.instance);
		assertNotNull(coord);
	}

	@Test
	public void testSingleTester() {
		int size = 1;
		Properties properties = new Properties();
		properties.setProperty("tester.peers",Integer.toString(size));
		TesterUtil defaults = new TesterUtil(properties);
		coord = new CoordinatorImpl(defaults);
		coordination = new Thread(coord, "Coordinator");
		
		coordination.start();
		try {
			coord.registerMethods(tester, methods);
			for (MethodDescription each : methods) {
				assertTrue(coord.getTesterMap().containsKey(each));
			}
			for (int i = 0; i < methods.size(); i++) {
				Thread.sleep(100);
				coord.methodExecutionFinished(tester, MessageType.OK);
			}
			Thread.sleep(1000);
			coord.quit(tester, Verdicts.PASS);
			coordination.join();
			System.out.println(coord);
			
			InOrder order = inOrder(tester);
			for (MethodDescription each : methods) {
				order.verify(tester).execute(each);
			}
			
		} catch (RemoteException e) {
			fail("Remote Error");
		} catch (InterruptedException e) {
			fail("InterruptedException");
		}
	}

	//@Test
	public void testSeveralTesters() {
		int size = 10000;
		Properties properties = new Properties();
		properties.setProperty("tester.peers",Integer.toString(size));
		TesterUtil defaults = new TesterUtil(properties);
		coord = new CoordinatorImpl(defaults);
		coordination = new Thread(coord, "Coordinator");
		coordination.start();
		
		try {
			
			Tester[] testers = new Tester[size];
			for (int i = 0; i < testers.length; i++) {
				testers[i] = mock(Tester.class);
				coord.registerMethods(testers[i], methods);
			}
			for (int i = 0; i < methods.size(); i++) {
				Thread.sleep(100 + size/10);
				for (int j = 0; j < testers.length; j++) {
					coord.methodExecutionFinished(testers[j], MessageType.OK);
				}
			}
			Thread.sleep(100 + size/10);
			for (int i = 0; i < testers.length; i++) {
				coord.quit(testers[i], Verdicts.PASS);
			}		
			
			coordination.join(10000);
			System.out.println(coord);
			
			for (int i = 0; i < testers.length; i++) {
				InOrder order = inOrder(testers[i]);
				for (MethodDescription each : methods) {
					order.verify(testers[i]).execute(each);
				}				
			}
			
		} catch (RemoteException e) {
			fail("Remote Error");
		} catch (InterruptedException e) {
			fail("InterruptedException");
		}

	}

}
