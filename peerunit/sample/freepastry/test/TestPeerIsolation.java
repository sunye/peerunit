package freepastry.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.pastry.Id;
import rice.pastry.NodeHandle;
import util.FreeLocalPort;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;

/**
 * Testing the recovery from peer isolation
 * 
 * @author almeida
 *
 */
public class TestPeerIsolation extends TesterImpl{
	private static Logger log = Logger.getLogger(TestPeerIsolation.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	static TestPeerIsolation test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	List<Id> volatiles=new ArrayList<Id>();

	public static void main(String[] str) {		
		test = new TestPeerIsolation();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestPeerIsolation.log.peer"+test.getPeerName(),true);
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
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.getBootstrap());
			Integer bootport = new Integer(TesterUtil.getBootstrapPort());
			InetSocketAddress bootaddress;

			bootaddress = new InetSocketAddress(bootaddr,bootport.intValue());

			if(!peer.join(bindport, bootaddress, env, log)){						
				inconclusive("I couldn't become a boostrapper, sorry");						
			}

			test.put(0,peer.getInetSocketAddress(bootaddr));
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


	@Test(place=-1,timeout=1000000, name = "action2", step = 0)
	public void startingBootstraps(){	

		try {			
			// waiting to create the net
			while(test.getCollection().size() ==0){
				Thread.sleep(sleep);
			}

			if(!iAmBootsrapper){
				iAmBootsrapper=true;		
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
				test.put(test.getPeerName(), peer.getId());
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

	@Test(name="action3",measure=true,step=0,timeout=10000000,place=-1)
	public void chooseAPeer() {		
		Random rand= new Random();		
		int chosePeer;
		try {
			Thread.sleep(sleep);
			if(test.getPeerName()==0){
				chosePeer = rand.nextInt(test.getCollection().size());
				Id id=(Id)test.get(chosePeer);				
				log.info("Chose peer "+chosePeer+" ID "+ id );
				test.clear();		
				Thread.sleep(sleep);
				test.put(-1,id);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	@Test(name="action4",measure=true,step=0,timeout=10000000,place=-1)
	public void listingTheNeighbours() {	
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		Object obj=test.get(-1);

		if(obj instanceof Id) {			
			Id id=(Id)obj;
			log.info("I am "+peer.getId()+" and the chose was  ID "+ id );

			// Only the chose peer store its table now
			if(peer.getId()== id){
				//storing my table
				test.put(-2, peer.getRoutingTable());

				for(NodeHandle nd: peer.getRoutingTable()){	
					volatiles.add(nd.getNodeId());
					log.info(" Successor to leave "+nd.getNodeId());					
				}
			}
		}
	}


	@Test(name="action5",measure=true,step=0,timeout=10000000,place=-1)
	public void testLeave() {		
		try {
			Thread.sleep(sleep);		

			List<NodeHandle> actuals=(List<NodeHandle>)test.get(-2);
			Object obj=test.get(-1);

			Id id=(Id)obj;
			//Checking if I am in the neighbour list to leave, excpet the chosen one
			for(NodeHandle nd: actuals){
				if(((nd.getNodeId().toString().trim().equalsIgnoreCase(peer.getId().toString().trim())))&&
					(!nd.getNodeId().toString().trim().equalsIgnoreCase(id.toString().trim()))){
					log.info("Leaving early");
					test.kill();				
				}
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void searchingNeighbour(){
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		List<NodeHandle> actuals;

		//Iterations to find someone in the routing table		
		int timeToClean=0;		
		Id obj=null;		
		boolean tableUpdated=false;
		Id idAnalyzed=(Id)test.get(-1);
		while(!tableUpdated &&	timeToClean < TesterUtil.getLoopToFail()){
			log.info(" Let's verify the table"+timeToClean);
			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
				e.printStackTrace();		
			}	

			actuals= peer.getRoutingTable();		

			for(NodeHandle nd: actuals){
				obj=nd.getNodeId();
				log.info(" Successor NodeId "+obj+" is volatile "+volatiles.contains(obj));

				// Verify if obj hasn't the same id of itself. Verify if some nd isn't volatile anymore.
				if((peer.getId()==idAnalyzed)&& (obj != peer.getId()) && (!volatiles.contains(obj))){
					log.info(" Table was updated, verdict may be PASS ");
					tableUpdated=true;				
					timeToClean=TesterUtil.getLoopToFail();
				}
			}

			log.info("Demanding the routing table update");
			peer.pingNodes();
			timeToClean++;
		}

		if((!tableUpdated)&&(peer.getId()==idAnalyzed)){
			log.info(" Did not find a sucessor ");
			fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
		}

	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("[PastryTest] Peer bye bye");
	}

}
