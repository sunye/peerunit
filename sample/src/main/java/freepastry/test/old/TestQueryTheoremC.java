package freepastry.test.old;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import rice.p2p.past.PastContent;
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
 * Should rebuild
 * @author almeida
 *
 */
@Deprecated
public class TestQueryTheoremC extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestQueryTheoremC.class.getName());

	private static final int OBJECTS=TesterUtil.instance.getObjects();

	static TestQueryTheoremC test;

	Peer peer=new Peer();

	int sleep=TesterUtil.instance.getSleep();


	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[PastryTest] Starting test peer  ");
	}
	@TestStep(place=-1,timeout=1000000, name = "action1", step = 0)
	public void starting(){
//		 Loads pastry settings
		//Environment env = new Environment();
		int netSize=TesterUtil.instance.getExpectedTesters();
		if(netSize<10)
			netSize=10;

		try {
			// the port to use locally
			FreeLocalPort port= new FreeLocalPort();
			int bindport = port.getPort();
			log.info("LocalPort:"+bindport);
			log.info("Bootstraps on system "+netSize/10);
			// build the bootaddress from the command line args
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.instance.getBootstrap());
			Integer bootport = new Integer(TesterUtil.instance.getBootstrapPort());
			InetSocketAddress bootaddress;

			if(test.getPeerName()==0){
				bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());

				//bootport=  new Integer(peer.getPort());
				bootaddress=peer.getInetSocketAddress(bootaddr);

				test.put(0,bootaddress);
				log.info("Cached boot address");
			}else
				Thread.sleep(test.getPeerName()*1000);

				//bootport= (InetSocketAddress)test.get(0);
				//bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());
				bootaddress= (InetSocketAddress)test.get(0);
				log.info("Getting cached boot "+bootaddress.toString());
				if(test.getPeerName()%9==0){
					while(test.getCollection().size()==0){
						Thread.sleep(500);
					}
					InetAddress thisIp = InetAddress.getLocalHost();
					InetSocketAddress newBootAddr = new InetSocketAddress(thisIp.getHostAddress(),bindport);

					log.info("Running on port "+peer.getPort());
					test.put(test.getPeerName(), newBootAddr);
				}else{
					Random rand=new Random();
					int boot=0;
					while(boot%9!=0){
						boot=rand.nextInt(netSize);
					}

					if(boot==0){

					}else{
						while(!test.containsKey(boot)){
							Thread.sleep(1000);
						}
						bootaddress =  (InetSocketAddress)test.get(boot);
						log.info("Got cached "+boot+" boot address "+bootaddress.toString());

					}
				}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(place=0,timeout=1000000, name = "action2", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<PastContent> resultSet=new ArrayList<PastContent>();

		// these variables are final so that the continuation can access them
		for(int i=0; i < OBJECTS ; i++){
			final String s = "test" + peer.env.getRandomSource().nextInt();

			// build the past content
			final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
			try {
				peer.insert(myContent);
				resultSet.add(myContent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		test.put(-1, resultSet);
	}
	/*@TestStep(place=-1,timeout=1000000, name = "action3", step = 0)
	public void testLeave(){
		try {
			if(test.getPeerName()%2==0){
				log.info("Leaving early");
				peer.leave();
				while(peer.isAlive()){
					Thread.sleep(sleep);
					log.info("I'm still alive");
					log.info("doing in the hard way!!");
					test.kill();
				}
			}
		} catch (RemoteException e2) {
			e2.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/

	@TestStep(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();
		}
		retrieveAssert();
		int peerName=0;
		try {
			peerName=test.getPeerName();
		} catch (RemoteException e2) {
			e2.printStackTrace();
		}

		if(peerName%2==0){
			log.info("Leaving early");
			peer.leave();
			int countAlive=0;
			while(peer.isAlive() ){
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.info("I'm still alive");
				countAlive++;
				if(countAlive>3){
					log.info("doing in the hard way!!");
					test.kill();
					break;
				}
			}

		}else{
			retrieveAssert();
		}
		log.info("[Local verdict] Almost finished");
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}

	@SuppressWarnings("unchecked")
	private void retrieveAssert(){
		List<PastContent> resultSet=(List<PastContent>)test.get(-1);
		for (PastContent content : resultSet) {
			log.info("[PastryTest] Will get "+content.getId().toString());
			peer.lookup( content.getId());
		}

		List<String> expecteds= new ArrayList<String>(resultSet.size());

		for (PastContent expected : resultSet) {
			log.info("[Local verdict] Expected "+expected.toString());
			expecteds.add(expected.toString());
		}

		log.info("[PastryTest] Retrieved so far "+peer.getResultSet().size());
		List<String> actuals= new ArrayList<String>();
		int sleeps=1;
		while(actuals.size()<resultSet.size()){
			try {
				Thread.sleep(sleep);
				log.info("[PastryTest] Sleep "+sleeps);
			} catch (Exception e) {
				e.printStackTrace();
			}

			for (PastContent content : resultSet) {
				log.info("[PastryTest] Will get the "+sleeps+" time "+content.getId().toString());
				peer.lookup( content.getId());
			}

			for (Object actual : peer.getResultSet()) {
				if(actual!=null){
					log.info("[Local verdict] Actual "+actual.toString());
					if(!actuals.contains(actual.toString()))
						actuals.add(actual.toString());

				}
			}
			sleeps++;
			if(sleeps>4){
				log.info("Enough!!");
				break;
			}
		}
		log.info("[Local verdict] Waiting a Verdict");
		Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
	}
}

