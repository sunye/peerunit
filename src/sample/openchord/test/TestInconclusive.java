package openchord.test;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import static fr.inria.peerunit.test.assertion.Assert.*;
import fr.inria.peerunit.util.LogFormat;
import static fr.inria.peerunit.test.assertion.Assert.*;

public class TestInconclusive extends TestCaseImpl{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	static TestInconclusive test;

	private static final Logger log = Logger.getLogger(TestInconclusive.class.getName());

	private String testString;



	private int getName(){
		int peerName=0;
		try {
			peerName= super.getPeerName();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return peerName;
	}

	@BeforeClass(place=-1,timeout=1000000)
	public void init() {
		log.info("[TestInc] Init");
		testString=null;
	}

	@Test(name="action1",measure=true,step=1,timeout=10000000,place=-1)
	public void testInconc() {

		try {
			if(test.getPeerName()==0){
				log.info("[TestInc] Will assert");
				assertEquals("TESTE",testString);
			}else log.info("[TestInc] Will continue");

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@AfterClass(place=-1,timeout=1000000)
	public void end() {
		log.info("[TestInc] Peer end");
	}
}
