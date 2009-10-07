package test.remote;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.TestStep;
import static fr.inria.peerunit.test.assertion.Assert.*;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test TesterImpl and CoordinatorImpl
 */
public class Sample extends TestCaseImpl {

	@TestStep(place = -1, timeout = 1000, name = "action1", step = 1)
	public void first() {
		System.setProperty("executed", "ok");
	}

	
	@TestStep(place = -1, timeout = 1000, name = "action2", step = 2)
	public void error() {
		assertTrue(false);
	}
	
	@TestStep(place = -1, timeout = 1000, name = "action3", step = 3)
	public void assertionError() {
		assert false;
	}	

        @Override
	public void setTester(Tester ti) {

		// TODO Auto-generated method stub
		
	}

}
