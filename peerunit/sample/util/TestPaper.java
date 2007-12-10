package util;


import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.rmi.tester.TesterImpl;
public class TestPaper extends TesterImpl {
	static TestPaper test;
	
	// Data to exercise a DHT
	String expected="fourteen";
	int expectedKey=14;
	
	String actual;
	
	Peer peer;

	public static void main(String[] args) {
		
		// Instantiate this class
		test = new TestPaper();
		
		// Export in order to parse the test methods
		test.export(test.getClass());		
		
		// Run the test itself
		test.run();
	}
	
	@BeforeClass(place=-1,timeout=100)
	public void start(){		
		// Pseudocode to instantiate a peer  
		//peer=new Peer();
	}

	@Test(from=0,to=2,timeout=100, name = "action1", step = 0)
	public void join(){		
		// Let's join the system
		peer.join();
	}
	
	@Test(place=2,timeout=100, name = "action2", step = 0)
	public void put(){		
		// Put data
		peer.put(expectedKey,expected);
	}
	
	@Test(from=3,to=4,timeout=100, name = "action3", step = 0)
	public void joinOthers(){
		// The rest of the peers join the system
		peer.join();
	}
	
	@Test(from=3,to=4,timeout=100, name = "action4", step = 0)
	public void retrieve(){
		// Retrieving the inserted data
		actual=peer.get(expectedKey);		
	}
	
	@Test(from=3,to=4,timeout=100, name = "action5", step = 0)
	public void assertRetrieve(){
		// Let's see if we got the expected data
		assertEquals(expected, actual);
	}
	
	@AfterClass(place=-1,timeout=100)
	public void stop(){
		peer.leave();
	}
}
