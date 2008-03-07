package freepastry.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import util.FreeLocalPort;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;
import freepastry.test.old.TestInsertLeaveB;

public class SimpleTest extends TestCaseImpl{
	// logger from jdk
	private static Logger log = Logger.getLogger(TestInsertLeaveB.class.getName());
	private static SimpleTest test;
	// Freepastry peer
	Peer peer = new Peer();

	/**
	 * This method starts the test
	 */
	@BeforeClass(place=-1,timeout=100)
	public void begin(){
		log.info("Starting the test ");
	}

	/**
	 * This method starts the bootstrap peer
	 */
	@Test(place=0,timeout=1000, name = "action1", step = 1)
	public void startingNetwork(){
		try {

			log.info("I am "+test.getPeerName());
			//	Loads pastry settings
			Environment env = new Environment();

			// the port to use locally
			FreeLocalPort port= new FreeLocalPort();
			int bindport = port.getPort();
			log.info("LocalPort:"+bindport);

			// build the bootaddress from the command line args
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.getBootstrap());
			Integer bootport = new Integer(TesterUtil.getBootstrapPort());
			InetSocketAddress bootaddress;

			bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());

			if(!peer.join(bindport, bootaddress, env, log)){
				inconclusive("I couldn't become a boostrapper, sorry");
			}

			// Setting the bootstrap address
			test.put(0,peer.getInetSocketAddress(bootaddr));
			log.info("Net created");

			while(!peer.isReady())
				Thread.sleep(1000);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method starts the rest of the peers
	 */
	@Test(place=-1,timeout=1000000, name = "action1", step = 2)
	public void joinNet(){

		try {
			// Wait a while due to the bootstrapper performance
			Thread.sleep(10000);
			if(test.getPeerName()!=0){
				//	Loads pastry settings
				Environment env = new Environment();

				// the port to use locally
				FreeLocalPort port= new FreeLocalPort();
				int bindport = port.getPort();
				log.info("LocalPort:"+bindport);

				// Each peer waits a while to join due to the freepastry bootstrap
				Thread.sleep(test.getPeerName()*1000);

				// Getting the bootstrap address
				InetSocketAddress bootaddress= (InetSocketAddress)test.get(0);
				log.info("Getting cached boot "+bootaddress.toString());

				if(!peer.join(bindport, bootaddress, env, log)){
					inconclusive("Couldn't boostrap, sorry");
				}
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stabilize the network.
	 */
	@Test(place=-1,timeout=1000000, name = "action2", step = 0)
	public void stabilize(){
		for (int i = 0; i < 4; i++) {
			try{
				// Force the routing table update
				peer.pingNodes();
				Thread.sleep(16000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Put some data and store in test variables.
	 */
	@Test(place=0,timeout=1000000, name = "action3", step = 0)
	public void put(){
		for(int i=0; i < 2 ; i++){
			// build the past content
			final String s = "test" + peer.env.getRandomSource().nextInt();
			final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
			peer.insert(myContent);
		}

		// Wait until all the insert ends since it is asynchronous
		while((peer.getFailedContent().size()+peer.getInsertedContent().size())<2){
			log.info("Inserted so far : "+peer.getInsertedContent().size());
			log.info("Failed so far : "+peer.getFailedContent().size());
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		List<PastContent> expecteds= new ArrayList<PastContent>();
		for (PastContent content : peer.getInsertedContent()) {
			log.info("Expected so far : "+content.toString());
			expecteds.add(content);
		}

		// Use a test variable to store the expected data
		test.put(1, expecteds);
	}
	/**
	 * Get the data and the verdict.
	 */
	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void get(){
		// Lookup
		List<PastContent> expectedContent=(List<PastContent>)test.get(1);
		Id contentKey;

		// Get the keys to lookup for data
		for (PastContent key : expectedContent) {
			contentKey=key.getId();
			if(contentKey!=null){
				log.info("Lookup Expected "+contentKey.toString());
				peer.lookup(contentKey);
			}
		}

		// Wait a little while for a response
		try {
			Thread.sleep(16000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// A list to store the retrieved data
		List<String> actuals= new ArrayList<String>();
		for (Object actual : peer.getResultSet()) {
			if(actual!=null){
				if(!actuals.contains(actual.toString())){
					log.info("[Local verdict] Actual "+actual.toString());
					actuals.add(actual.toString());
				}
			}
		}

		// Generating the expecteds list
		List<String> expecteds=new ArrayList<String>();
		for(PastContent expected : expectedContent){
			expecteds.add(expected.toString());
		}

		// Assigning a verdict
		log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
		Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
	}

	/**
	 * This method finishes the test
	 *
	 */
	@AfterClass(timeout=100,place=-1)
	public void end() {
		log.info("Peer bye bye");
	}
}

