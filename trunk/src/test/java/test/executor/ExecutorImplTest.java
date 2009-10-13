/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package test.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.GlobalVariablesImpl;
import fr.inria.peerunit.exception.AnnotationFailure;
import fr.inria.peerunit.base.TestCaseWrapper;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.rmi.coord.CoordinatorImpl;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.TesterUtil;

public class ExecutorImplTest {

	private static TestCaseWrapper executor;

	private static CoordinatorImpl coord;

    private static GlobalVariables globals; 

    private static TesterImpl tester;
	
	private static Logger LOG = Logger.getLogger("test");

	@BeforeClass
	public static void  inititalize() {
		Properties properties = new Properties();
		properties.setProperty("tester.peers","3");
		properties.setProperty("tester.log.dateformat","yyyy-MM-dd");
		properties.setProperty("tester.log.timeformat","HH:mm:ss.SSS");
		properties.setProperty("tester.log.level","FINEST");
		properties.setProperty("tester.logfolder","/tmp/");
		properties.setProperty("tester.log.delimiter","|");
		properties.setProperty("tester.waitForMethod","500");
		try {
			TesterUtil defaults = new TesterUtil(properties);
			coord = new CoordinatorImpl(defaults);
			globals = new GlobalVariablesImpl();
			new Thread(coord, "Coordinator").start();
			tester = new TesterImpl(coord, globals);
			tester.setCoordinator(coord);
			executor = new TestCaseWrapper(tester, LOG);

		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}



	public ExecutorImplTest() {
		//LOG.setLevel(Level.FINEST);
	}

	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void testBis() {

		assert executor != null;
		
		List<MethodDescription> m = executor.register(Data.class);
		assertEquals(8, m.size());
		assertTrue(m.contains(new MethodDescription("here","action4", 0,"TestStep",1000000)));
		assertFalse(m.contains(new MethodDescription("notHere","action4", 0,"TestStep",1000000)));
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
