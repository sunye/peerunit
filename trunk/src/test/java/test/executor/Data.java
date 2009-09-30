package test.executor;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test the class ParserImpl
 */
public class Data extends TestCaseImpl {

	@BeforeClass(place = -1, timeout = 100)
	public void begin() {
	}

	@TestStep(place = -1, timeout = 1000, name = "action1", step = 1)
	public void startingNetwork() {
	}

	@TestStep(place = -1, timeout = 1000000, name = "action1", step = 2)
	public void joinNet() {
	}

	@TestStep(place = -1, timeout = 1000000, name = "action2", step = 0)
	public void stabilize() {
	}

	@TestStep(place = -1, timeout = 1000000, name = "action3", step = 0)
	public void put() {
	}

	@TestStep(place = -1, timeout = 1000000, name = "action4", step = 0)
	public void get() {
	}

	@TestStep(place = 42, timeout = 1000000, name = "action4", step = 0)
	public void notHere() {
	}

	@TestStep(from = 10, to = 100, timeout = 1000000, name = "action4", step = 0)
	public void alsoNotHere() {
	}

	@TestStep(from = 0, to = 100, timeout = 1000000, name = "action4", step = 0)
	public void here() {
	}

	@AfterClass(timeout = 100, place = -1)
	public void end() {
	}

	public void setTester(Tester ti) {
		// TODO Auto-generated method stub
		
	}
}
