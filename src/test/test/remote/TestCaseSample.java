package test.remote;

import fr.inria.peerunit.TestCaseImpl;
import static fr.inria.peerunit.test.assertion.Assert.*;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test TesterImpl and CoordinatorImpl
 */
public class TestCaseSample extends TestCaseImpl {

	@Test(place = -1, timeout = 1000, name = "action1", step = 1)
	public void first() {
		System.setProperty("executed", "ok");
	}

}
