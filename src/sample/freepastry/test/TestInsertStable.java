package freepastry.test;

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
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;


/**
 * Test Insert and retrieve on a stable system
 * @author almeida
 *
 */
public class TestInsertStable  extends TesterImpl{
	private static Logger log = Logger.getLogger(TestInsertStable.class.getName());

	static TestInsertStable test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	public static void main(String[] str) {		
		test = new TestInsertStable();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestInsertStable.log.peer"+test.getPeerName(),true);
			handler.setFormatter(new LogFormat());
			log.addHandler(handler);
		} catch (SecurityException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		test.run();
	}
	@BeforeClass(place=-1,timeout=1000000)
	public void start(){
		log.info("Starting test peer  ");
	}

	@Test(place=0,timeout=1000000, name = "action1", step = 1)
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
			if(!peer.join(bindport, bootaddress, env, log,true)){						
				inconclusive("I couldn't become a boostrapper, sorry");						
			}

			test.put(-1,peer.getInetSocketAddress(bootaddr));
			//log.info("Cached boot address: "+bootaddress.toString());
			//test.put(-1,bootaddress);
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
	
	@Test(place=-1,timeout=1000000, name = "action1", step = 2)
	public void startingInitNet(){	

		try {			
			// Wait a while due to the bootstrapper performance
			Thread.sleep(sleep);
			if(test.getPeerName()!=0){
				log.info("Joining in first");
				//	Loads pastry settings
				Environment env = new Environment();

				// the port to use locally
				FreeLocalPort port= new FreeLocalPort();				
				int bindport = port.getPort();
				log.info("LocalPort:"+bindport); 

				Thread.sleep(test.getPeerName()*1000);
				InetSocketAddress bootaddress= (InetSocketAddress)test.get(-1);
				log.info("Getting cached boot "+bootaddress.toString());
				if(!peer.join(bindport, bootaddress, env, log)){					
					inconclusive("Couldn't boostrap, sorry");				
					test.put(test.getPeerName(),"INCONCLUSIVE");
				}
				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (InterruptedException e) {			
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
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
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}
	}
	
	/*@Test(place=-1,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(test.getPeerName()*1000);
			String s;
			for(int i=0;i< TesterUtil.getObjects();i++){
				s = "" + ((test.getPeerName()*TesterUtil.getObjects())+i);

				// 	build the past content
				PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);
				peer.insert(myContent);
			
			}
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
	@Test(place=-1,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try {
			Thread.sleep(sleep);		
			if(test.getPeerName()==0){
				List<PastContent> resultSet=new ArrayList<PastContent>();

				// these variables are final so that the continuation can access them
				for(int i=0; i < TesterUtil.getObjects() ; i++){
					final String s = "test" + peer.env.getRandomSource().nextInt();

					// build the past content
					final PastContent myContent = new MyPastContent(peer.localFactory.buildId(s), s);

					peer.insert(myContent);
					resultSet.add(myContent);

				}
				test.put(-1, resultSet);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){		
		try {
			Thread.sleep(sleep);
			String content;
			List<String> actuals= new ArrayList<String>();
			List<String> expecteds= new ArrayList<String>();
			int timeToFind=0;			
			while(timeToFind < TesterUtil.getLoopToFail()){
				for(int i=0;i< TesterUtil.getObjects();i++){
					log.info("lookup for "+i);
					// Build the content
					content=""+i;
					
					
					if(bootstrapped(i)){
						if(expecteds.size()<TesterUtil.getObjects()){
							expecteds.add(new MyPastContent(peer.localFactory.buildId(content), content).toString());
						}					
						peer.lookup(peer.localFactory.buildId(content));
					}
				}
	
				Thread.sleep(sleep);

				log.info("Retrieval "+timeToFind);
				for (Object actual : peer.getResultSet()) {
					if(actual!=null){
						log.info("Actual "+actual.toString());

						if(!actuals.contains(actual.toString())){
							actuals.add(actual.toString());
						}
					}		
				}
				peer.pingNodes();
				Thread.sleep(sleep);
				timeToFind++;
			}
			log.info("Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
			Assert.assertListEquals("[Local verdict] ",expecteds, actuals);	
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("Peer bye bye");
	}	
	
	/**
	 * Returns true if the peer bootstrapped properly
	 * @param i
	 * @return
	 */
	private boolean bootstrapped(int i){
		
		try {
			for(Integer peer: test.getCollection().keySet()){
				if(peer.intValue()==i)
					return false;
					
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
}
