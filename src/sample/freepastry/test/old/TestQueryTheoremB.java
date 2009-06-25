package freepastry.test.old;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
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
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;
import static fr.inria.peerunit.test.assertion.Assert.*;

/**
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestQueryTheoremB extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestQueryTheoremB.class.getName());

	private static final int OBJECTS=TesterUtil.instance.getObjects();

	static TestQueryTheoremB test;

	Peer peer=new Peer();

	int sleep=TesterUtil.instance.getSleep();
	boolean iAmBootsrapper=false;


	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}
	@Test(place=0,timeout=1000000, name = "action1", step = 0)
	public void startingNetwork(){
		try {
			iAmBootsrapper=true;
			log.info("Am I a "+test.getPeerName()+" bootstrapper? "+iAmBootsrapper);
			//	Loads pastry settings
			Environment env = new Environment();

			// the port to use locally
			FreeLocalPort port= new FreeLocalPort();
			int bindport = port.getPort();
			log.info("LocalPort:"+bindport);

			// build the bootaddress from the command line args
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.instance.getBootstrap());
			Integer bootport = new Integer(TesterUtil.instance.getBootstrapPort());
			InetSocketAddress bootaddress;

			bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());

			if(!peer.join(bindport, bootaddress, env, log)){
				inconclusive("I couldn't become a boostrapper, sorry");
			}

			test.put(0,peer.getInetSocketAddress(bootaddr));
			log.info("Net created");

			while(!peer.isReady())
				Thread.sleep(sleep);

			while(test.getCollection().size() ==0){
				Thread.sleep(sleep);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Test(place=-1,timeout=1000000, name = "action2", step = 0)
	public void startingBootstraps(){

		try {

			if(!iAmBootsrapper){
				Thread.sleep(sleep);
				iAmBootsrapper=true;
				log.info("Am I a "+test.getPeerName()+" bootstrapper? "+iAmBootsrapper);

				log.info("I Am Bootsrapper");
				//	Loads pastry settings
				Environment env = new Environment();

				// the port to use locally
				FreeLocalPort port= new FreeLocalPort();
				int bindport = port.getPort();
				log.info("LocalPort:"+bindport);

				Thread.sleep(test.getPeerName()*1000);
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

	@Test(from=1,to=8,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// these variables are final so that the continuation can access them
		int randomContentId= peer.env.getRandomSource().nextInt();
		final String s = "test" +randomContentId;

			// build the past content
		final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
		try {
			peer.insert(myContent);
			test.put(test.getPeerName()*1000, myContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){

		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<PastContent> contents= new ArrayList<PastContent>();
		PastContent myContent;
		for(int i=1;i<=8;i++){
			log.info("Getting key "+i*1000);
			myContent=(PastContent)test.get(i*2000);
			contents.add(myContent);
		}

		List<String> expecteds= new ArrayList<String>();

		for (PastContent content : contents) {
			if(!expecteds.contains(content.toString())){
				log.info("[Local verdict] Expected "+content.toString());
				expecteds.add(content.toString());
			}
		}

		int timeToFind=0;
		List<String> actuals= new ArrayList<String>();
		while(timeToFind < TesterUtil.instance.getLoopToFail()){
			// Lookup first time
			Id contentKey;
			for (PastContent key : contents) {
				contentKey=key.getId();
				if(contentKey!=null){
					log.info("[PastryTest] Lookup  "+contentKey.toString());
					peer.lookup(contentKey);
				}
			}

			// Sleep
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
				e.printStackTrace();
			}

			log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

			for (Object actual : peer.getResultSet()) {
				if(actual!=null){
					log.info("[Local verdict] Actual "+actual.toString());

					if(!actuals.contains(actual.toString())){
						actuals.add(actual.toString());
					}
				}
			}
			timeToFind++;
			log.info("New Retrieval "+timeToFind+" will start. So far " + actuals.size()+" of "+ expecteds.size());
		}

		log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
		Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}


}
