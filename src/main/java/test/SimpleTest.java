package test;


import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;

public class SimpleTest extends TestCaseImpl{	
	
	/**
	 * This method starts the test
	 */
	@BeforeClass(place=-1,timeout=100)
	public void begin(){
			
	}


	@Test(place=-1,timeout=1000, name = "action2", step = 1)
	public void action2(){
		
	}


	@Test(from=1,to=3,timeout=1000000, name = "action3", step = 1)
	public void action3(){
	
	}

	@Test(place=2,timeout=1000000, name = "action4", step = 1)
	public void action4(){
	
	}
	@Test(place=-1,timeout=1000000, name = "action5", step = 1)
	public void action5(){
	
	}
	@Test(place=-1,timeout=1000000, name = "action6", step = 1)
	public void action6(){
	
	}
	@Test(place=-1,timeout=1000000, name = "action7", step = 1)
	public void action7(){
	
	}
	
	@AfterClass(timeout=100,place=-1)
	public void end() {
	
	}


	public void setTester(Tester t) {
		// TODO Auto-generated method stub
		
	}

}

