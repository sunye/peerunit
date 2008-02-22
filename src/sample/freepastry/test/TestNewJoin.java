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
 * Test routing table update in an expanding system
 * @author almeida
 *
 */
public class TestNewJoin extends TesterImpl{
	private static Logger log = Logger.getLogger(TestNewJoin.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	static TestNewJoin test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	List<Id> firstSuccessors=new ArrayList<Id>();


	public static void main(String[] str) {		
		test = new TestNewJoin();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestNewJoin.log.peer"+test.getPeerName(),true);
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


	@Test(place=-1,timeout=1000000, name = "action2", step = 0)
	public void startingHalfNet(){	

		try {			

			if(test.getPeerName()%2!=0){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(test.getPeerName()*1000);
							
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
	public void testFind(){
		try {
			Thread.sleep(sleep);
			if((test.getPeerName()%2!=0)||(test.getPeerName()==0)){
				log.info("My ID "+peer.getId());
				for(NodeHandle nd: peer.getRoutingTable()){
					log.info("Successor NodeId "+nd.getId());	
					firstSuccessors.add(nd.getNodeId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();		
		}	

	}
	@Test(place=-1,timeout=1000000, name = "action5", step = 0)
	public void startingOtherHalfNet(){	

		try {			
			
			if((test.getPeerName()!=0)&&(test.getPeerName()%2==0)){
				log.info("Joining in first");
				Network net= new Network();
				Thread.sleep(test.getPeerName()*1000);
							
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

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void updatingTables(){
		try {		
			Thread.sleep(sleep);

			if((test.getPeerName()%2==0)&&(test.getPeerName()!=0)){		
				log.info("My ID "+peer.getId());
				for(NodeHandle nd: peer.getRoutingTable()){
					log.info("Successor NodeId "+nd.getId());	
					firstSuccessors.add(nd.getNodeId());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();		
		}	
	}


	@Test(place=-1,timeout=1000000, name = "action7", step = 0)
	public void testFindAgain(){
		try {
			if((test.getPeerName()%2!=0)||(test.getPeerName()==0)){
				List<NodeHandle> actuals;

				//Iterations to clean the volatiles from the routing table
				int timeToUpdate=0;		
				Id obj=null;		
				boolean tableUpdated=false;
				while(!tableUpdated &&	timeToUpdate < TesterUtil.getLoopToFail()){
					log.info("Verifying the "+timeToUpdate+" time ");
					try {
						Thread.sleep(sleep);
					} catch (Exception e) {
						e.printStackTrace();		
					}	
					actuals= peer.getRoutingTable();		

					for(NodeHandle nd: actuals){
						obj=nd.getNodeId();
						log.info(" Successor NodeId "+obj);
						if(!firstSuccessors.contains(obj)){		
							log.info("List updated, the verdict may be PASS ");
							tableUpdated=true;
						}
					}

					//Demanding the routing table update
					peer.pingNodes();

					timeToUpdate++;
				}
				if(!tableUpdated)
					fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
				else
					log.info("List updated, the verdict may be PASS. Table updated "+timeToUpdate+" times.");

			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("[PastryTest] Peer bye bye");
	}
}
