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
import freepastry.Network;
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

	@Test(place=0,timeout=1000000, name = "action1", step = 1)
	public void startingNetwork(){
		try {	
			Network net= new Network();
			if(!net.joinNetwork(peer, null, true, log)){
				inconclusive("I couldn't become a boostrapper, sorry");
			}

			test.put(0,net.getInetSocketAddress());
			log.info("Net created");

			while(!peer.isReady())
				Thread.sleep(sleep);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}

	@Test(place=-1,timeout=1000000, name = "action1", step = 2)
	public void joiningNet(){	

		try {			
			// Wait a while due to the bootstrapper performance
			Thread.sleep(sleep);
			if(test.getPeerName()!=0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(test.getPeerName()*1000);

				InetSocketAddress bootaddress= (InetSocketAddress)test.get(0);
				log.info("Getting cached boot "+bootaddress.toString());

				if(!net.joinNetwork(peer, bootaddress, false, log)){
					inconclusive("I couldn't join, sorry");
				}

				log.info("Running on port "+peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (InterruptedException e) {			
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();		
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@Test(name="action4",measure=true,step=0,timeout=10000000,place=0)
	public void listingTheNeighbours() {	
		try {
			
			// Letting the system to stabilize
			while(peer.getRoutingTable().size()==0)
				Thread.sleep(sleep);

			
			test.put(1, peer.getRoutingTable());
			
			log.info("My ID "+peer.getId().toString());	
			for(NodeHandle nd: peer.getRoutingTable()){
				if(!peer.getId().toString().equalsIgnoreCase(nd.getNodeId().toString())){
					volatiles.add(nd.getNodeId());
					log.info(" Successor to leave "+nd.getNodeId());
				}
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} 

	}


	@Test(name="action5",measure=true,step=0,timeout=10000000,place=-1)
	public void testLeave() {		
		try {
			// Waiting a while to get the global variable
			Thread.sleep(2000);			
			
			if(test.getPeerName()!=0){
				List<NodeHandle> actuals=(List<NodeHandle>)test.get(1);

				for(NodeHandle nd: actuals){
					if(nd.getNodeId().toString().trim().equalsIgnoreCase(peer.getId().toString().trim())){
						log.info("Leaving early");
						test.kill();				
					}						
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void searchingNeighbour(){
		try {
			if(test.getPeerName()==0){
				List<NodeHandle> actuals;
		
				//Iterations to find someone in the routing table		
				int timeToClean=0;		
				Id obj=null;		
				boolean tableUpdated=false;
		
				while(!tableUpdated &&	(timeToClean < TesterUtil.getLoopToFail())){
					log.info(" Let's verify the table"+timeToClean);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();		
					}	
		
					actuals= peer.getRoutingTable();		
		
					for(NodeHandle nd: actuals){
						obj=nd.getNodeId();
						log.info(" Successor NodeId "+obj+" is volatile "+volatiles.contains(obj));
		
						if((obj != peer.getId()) && (!volatiles.contains(obj))){
							log.info(" Table was updated, verdict may be PASS ");
							tableUpdated=true;				
							timeToClean=TesterUtil.getLoopToFail();
						}
					}
		
					log.info("Demanding the routing table update");
					peer.pingNodes();
					timeToClean++;
				}	
				if(!tableUpdated){
						log.info(" Did not find a sucessor ");
						fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
				}
			}
			log.info(" Waiting to receive a  verdict ");
			Thread.sleep(1000);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("[PastryTest] Peer bye bye");
	}

}
