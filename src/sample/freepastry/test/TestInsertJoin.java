package freepastry.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.tutorial.past.MyPastContent;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Network;
import freepastry.Peer;

/**
 * Test Insert/Retrieve in an Expanding System
 * @author almeida
 *
 */
public class TestInsertJoin  extends TestCaseImpl {
	private static Logger log = Logger.getLogger(TestInsertJoin.class.getName());

	private static final int OBJECTS=TesterUtil.instance.getObjects();

	Peer peer=new Peer();

	int sleep=TesterUtil.instance.getSleep();

	List<Id> firstSuccessors=new ArrayList<Id>();

	int churnPercentage=TesterUtil.instance.getChurnPercentage();

	Map<Integer,Object> objList=new HashMap<Integer, Object>();

	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}


	@Test(place=0,timeout=1000000, name = "action1", step = 0)
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
			this.put(intObj.intValue()*10, intObj);
		}
	}

	@Test(place=-1,timeout=1000000, name = "action2", step = 0)
	public void startingInitNet(){

		try {
			if(!chosenOne(this.getPeerName())){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(this.getPeerName()*1000);

				if(!net.joinNetwork(peer, null,false, log)){
					inconclusive("I couldn't join, sorry");
				}
				log.info("Getting cached boot "+net.getInetSocketAddress().toString());
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}





	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);
			if(this.getPeerName()==0){
				List<PastContent> resultSet=new ArrayList<PastContent>();

				// these variables are final so that the continuation can access them
				for(int i=0; i < OBJECTS ; i++){
					final String s = "test" + peer.env.getRandomSource().nextInt();

					// build the past content
					final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);

					peer.insert(myContent);
					resultSet.add(myContent);

				}
				this.put(-1, resultSet);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testRetrieve(){
		try {
			Thread.sleep(sleep);

			if(!chosenOne(this.getPeerName())){
				// Lookup first time
				List<PastContent> keySet=(List<PastContent>)this.get(-1);
				Id contentKey;
				for (PastContent key : keySet) {
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
				List<String> expecteds= new ArrayList<String>(keySet.size());
				log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

				for (Object expected : peer.getResultSet()) {
					if(expected!=null){
						log.info("[Local verdict] Expected "+expected.toString());
						expecteds.add(expected.toString());
					}
				}
				this.put(2, expecteds);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void startingOtherHalfNet(){

		try {
			Thread.sleep(sleep);

			if(chosenOne(this.getPeerName())&&(this.getPeerName()!=0)){
				log.info("Joining in second");
				Network net= new Network();
				Thread.sleep(this.getPeerName()*1000);

				InetSocketAddress bootaddress= (InetSocketAddress)this.get(-10);
				log.info("Getting cached boot "+bootaddress.toString());

				if(!net.joinNetwork(peer, bootaddress, false, log)){
					inconclusive("I couldn't join, sorry");
				}

				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(place=-1,timeout=1000000, name = "action7", step = 0)
	public void testRetrieveByOthers(){
		try {
			if(chosenOne(this.getPeerName())&&(this.getPeerName()!=0)){

				// Lookup first time
				List<PastContent> keySet=(List<PastContent>)this.get(-1);
				Id contentKey;
				for (PastContent key : keySet) {
					contentKey=key.getId();
					if(contentKey!=null){
						log.info("[PastryTest] Lookup Expected "+contentKey.toString());
						peer.lookup(contentKey);
					}
				}

				log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());

				List<String> actuals= new ArrayList<String>();
				int timeToFind=0;

				while (actuals.size() < OBJECTS) {
					log.info("Retrieval "+timeToFind);
					for (Object actual : peer.getResultSet()) {
						if(actual!=null){
							log.info("[Local verdict] Actual "+actual.toString());

							if(!actuals.contains(actual.toString())){
								actuals.add(actual.toString());
							}
						}
					}
					peer.pingNodes();
					Thread.sleep(1000);
					timeToFind++;
				}
				List<String> expecteds=(List<String>)this.get(2);
				log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
				//Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test(place=-1,timeout=1000000, name = "action8", step = 0)
	public void getHandle(){
		List<PastContent> cont=peer.getInsertedContent();
		PastContentHandle pch;
		for(PastContent pc: cont){
			pch=pc.getHandle(peer.getPast());
			System.out.println("NodeHandle "+pch.getNodeHandle());
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}
	private boolean chosenOne(int name){
		try {
			if(objList.isEmpty()){
				objList=this.getCollection();
			}
			Set<Integer> keySet=objList.keySet();
			Object nameChose;
			for(Integer key: keySet){
				nameChose=objList.get(key);
				if (nameChose instanceof Integer) {
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
