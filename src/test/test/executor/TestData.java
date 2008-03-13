package test.executor;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;

/**
 * @author sunye
 *
 * This is not a test case !!!
 *
 * This class is only used to test the class ParserImpl
 */
public class TestData extends TestCaseImpl {

	@BeforeClass(place = -1, timeout = 100)
	public void begin() {
	}

	@Test(place = -1, timeout = 1000, name = "action1", step = 1)
	public void startingNetwork() {
	}

	@Test(place = -1, timeout = 1000000, name = "action1", step = 2)
	public void joinNet() {
	}

	@Test(place = -1, timeout = 1000000, name = "action2", step = 0)
	public void stabilize() {
	}

	@Test(place = -1, timeout = 1000000, name = "action3", step = 0)
	public void put() {
	}

	@Test(place = -1, timeout = 1000000, name = "action4", step = 0)
	public void get() {
	}

	@Test(place = 42, timeout = 1000000, name = "action4", step = 0)
	public void notHere() {
	}

	@Test(from = 10, to = 100, timeout = 1000000, name = "action4", step = 0)
	public void alsoNotHere() {
	}

	@Test(from = 0, to = 100, timeout = 1000000, name = "action4", step = 0)
	public void here() {
	}

	@AfterClass(timeout = 100, place = -1)
	public void end() {
	}
}
