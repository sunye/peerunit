package freepastry.test.old;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContent;
import rice.pastry.NodeHandle;
import rice.tutorial.past.MyPastContent;
import util.FreeLocalPort;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;


/**
 * Test E5B on experiments list
 * @author almeida
 *
 */
public class TestInsertLeaveB  extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestInsertLeaveB.class.getName());

	private static final int OBJECTS=TesterUtil.instance.getObjects();

	static TestInsertLeaveB test;

	Peer peer = new Peer();

	int sleep=TesterUtil.instance.getSleep();

	List<Id> firstSuccessors=new ArrayList<Id>();

	int churnPercentage=TesterUtil.instance.getChurnPercentage();

	Map<Integer,Object> objList=new HashMap<Integer, Object>();




	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}

	@TestStep(place=0,timeout=1000000, name = "action1", step = 0)
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
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.instance.getBootstrap());
			Integer bootport = new Integer(TesterUtil.instance.getBootstrapPort());
			InetSocketAddress bootaddress;

			bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());

			if(!peer.join(bindport, bootaddress, env, log)){
				inconclusive("I couldn't become a boostrapper, sorry");
			}

			test.put(-2,peer.getInetSocketAddress(bootaddr));
			log.info("Net created");

			while(!peer.isReady())
				Thread.sleep(sleep);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=0,timeout=1000000, name = "action2", step = 0)
	public void chosingPeer(){
		Random rand=new Random();
		List<Integer> generated=new ArrayList<Integer>();
		int chosePeer;
		int netSize= (TesterUtil.instance.getExpectedTesters()*TesterUtil.instance.getChurnPercentage())/100;
		log.info("It will join "+netSize+" peers");
		boolean peerChose;
		while(netSize >0){
			peerChose=false;
			while(!peerChose){
				chosePeer=rand.nextInt(TesterUtil.instance.getExpectedTesters());
				if(chosePeer!=0){
					Integer genInt=new Integer(chosePeer);
					if(!generated.contains(genInt)){
						generated.add(genInt);
						peerChose=true;
						log.info("Chose peer "+genInt);
					}
				}
			}
			netSize--;
		}
		for(Integer intObj: generated){
			test.put(intObj.intValue()*100, intObj);
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action3", step = 0)
	public void startingInitNet(){

		try {
			// waiting to create the net
			while(test.getCollection().size() ==0){
				Thread.sleep(sleep);
			}


			log.info("Joining in first");
			//	Loads pastry settings
			Environment env = new Environment();

			// the port to use locally
			FreeLocalPort port= new FreeLocalPort();
			int bindport = port.getPort();
			log.info("LocalPort:"+bindport);

			Thread.sleep(test.getPeerName()*1000);
			InetSocketAddress bootaddress= (InetSocketAddress)test.get(-2);
			log.info("Getting cached boot "+bootaddress.toString());

			if(!peer.join(bindport, bootaddress, env, log)){
				inconclusive("Couldn't boostrap, sorry");
			}
			log.info("Running on port "+peer.getPort());
			log.info("Time to bootstrap");


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

	@TestStep(place=-1,timeout=1000000, name = "action3", step = 1)
	public void stabilize(){
		int timeToFind=0;
		while(timeToFind < TesterUtil.instance.getLoopToFail()){
			try{
				peer.pingNodes();
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeToFind++;
		}
	}

	@TestStep(place=0,timeout=1000000, name = "action4", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);
			//List<PastContent> insertedContent=new ArrayList<PastContent>();
			// these variables are final so that the continuation can access them
			for(int i=0; i < OBJECTS ; i++){
				final String s = "test" + peer.env.getRandomSource().nextInt();
				// build the past content
				final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
				peer.insert(myContent);
				//insertedContent.add(myContent);
			}

			while((peer.getFailedContent().size()+peer.getInsertedContent().size())<OBJECTS){
				log.info("Inserted so far : "+peer.getInsertedContent().size());
				log.info("Failed so far : "+peer.getFailedContent().size());
				Thread.sleep(sleep);
			}

			List<String> expecteds= new ArrayList<String>();
			for (PastContent content : peer.getInsertedContent()) {
				log.info("Expected so far : "+content.toString());
				expecteds.add(content.toString());
			}

			test.put(-3, peer.getInsertedContent());
			test.put(-1, expecteds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testRetrieve(){
		try {
			Thread.sleep(sleep);

			// Lookup first time

			Id contentKey;
			for (PastContent key : peer.getInsertedContent()) {
				contentKey=key.getId();
				if(contentKey!=null){
					log.info("[PastryTest] Lookup Expected "+contentKey.toString());
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
			for (Object expected : peer.getResultSet()) {
				if(expected!=null){
					log.info("Retrieve before depart "+expected.toString());
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=-1,timeout=1000000, name = "action6", step = 0)
	public void leaving(){
		try{

			log.info("I am "+peer.getId()+" chose? "+chosenOne(test.getPeerName()));

			for(NodeHandle nd: peer.getRoutingTable()){
					log.info(" Successor before "+nd.getNodeId());
			}

			if(chosenOne(test.getPeerName())){
				log.info("Leaving early ");
				peer.leave();
			}

			Thread.sleep(sleep);
			for(NodeHandle nd: peer.getRoutingTable()){
				log.info(" Successor after "+nd.getNodeId());
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@TestStep(place=-1,timeout=9000000, name = "action7", step = 0)
	public void testRetrieveByOthers(){
		try {
			if(!chosenOne(test.getPeerName())){
				Thread.sleep(sleep);

				// Lookup first time
				List<PastContent> insertedContent=(List<PastContent>)test.get(-3);
				Id contentKey;
				List<String> actuals= new ArrayList<String>();
				int timeToFind=0;
				while(timeToFind < TesterUtil.instance.getLoopToFail()){
					for (PastContent key : insertedContent) {
						contentKey=key.getId();
						if(contentKey!=null){
							log.info("[PastryTest] Lookup Expected "+contentKey.toString());
							peer.lookup(contentKey);
						}
					}

					Thread.sleep(sleep);

					log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());
					actuals.clear();
					log.info("Retrieval "+timeToFind);
					for (Object actual : peer.getResultSet()) {
						if(actual!=null){
							if(!actuals.contains(actual.toString())){
								log.info("[Local verdict] Actual "+actual.toString());
								actuals.add(actual.toString());
							}
						}
					}

					if(actuals.size()==insertedContent.size()){
						break;
					}else{
						peer.pingNodes();
						timeToFind++;
					}
				}

				List<String> expecteds=(List<String>)test.get(-1);
				log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
				Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}


	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}
	private boolean chosenOne(int name){
		try {
			if(objList.isEmpty()){
				objList=test.getCollection();
			}
			Set<Integer> keySet=objList.keySet();
			Object nameChose;
			for(Integer key: keySet){
				nameChose=objList.get(key);
				log.info("key "+key.intValue());
				if ((nameChose instanceof Integer)&&(key.intValue()>=100)) {
					Integer new_name = (Integer) nameChose;
					if(new_name.intValue()==name){
						return true;
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
}
