package fr.inria.peerunit.openchordtest.test;

import java.util.logging.Logger;

import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import static fr.inria.peerunit.tester.Assert.*;

public class TestInconclusive extends AbstractOpenChordTest {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	static TestInconclusive test;

	private static final Logger log = Logger.getLogger(TestInconclusive.class.getName());

	private String testString;




	@BeforeClass(range = "*",timeout = 10000)
	public void init() {
		log.info("[TestInc] Init");
		testString=null;
	}

	@TestStep(order = 1,timeout = 100000,range = "*")
	public void testInconc() {

		
			if(this.getPeerName()==0){
				log.info("[TestInc] Will assert");
				assertEquals("TESTE",testString);
			}else log.info("[TestInc] Will continue");


	}

	@AfterClass(range = "*",timeout = 10000)
	public void end() {
		log.info("[TestInc] Peer end");
	}
}
