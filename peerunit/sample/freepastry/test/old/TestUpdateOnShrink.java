package freepastry.test.old;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
 * Test routing table update in a shrinking system
 * @author almeida
 *
 */
public class TestUpdateOnShrink  extends TesterImpl{
	private static Logger log = Logger.getLogger(TestUpdateOnShrink.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	static TestUpdateOnShrink test;

	Peer peer=new Peer();

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	public static void main(String[] str) {		
		test = new TestUpdateOnShrink();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestUpdateOnShrink.log.peer"+test.getPeerName(),true);
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

	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testFind(){
		try {
			Thread.sleep(sleep);
		} catch (Exception e) {
			e.printStackTrace();		
		}		
		log.info("My ID "+peer.getId());
		for(NodeHandle nd: peer.getRoutingTable()){
			log.info("Successor NodeId "+nd.getId());			
		}		
	}
	@Test(name="action5",measure=true,step=0,timeout=10000000,place=-1)
	public void testLeave() {		
		try {
			Thread.sleep(sleep);		
			if(test.getPeerName()%2!=0){
				test.put(test.getPeerName(), peer.getId());
				log.info("Leaving early");
				peer.leave();				
				Thread.sleep(sleep);
				if(peer.isAlive()){
					log.info("doing in the hard way!!");
					test.kill();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}	
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void testFindAgain(){
		try {
			if(test.getPeerName()%2==0){
				List<Id> volatiles=new ArrayList<Id>();

				Set<Integer> keySet=test.getCollection().keySet();
				Object gotValue;
				for(Integer i: keySet){	
					gotValue=test.get(i);								
					if(gotValue instanceof Id) {
						volatiles.add((Id)gotValue);
						log.info("Volatiles NodeId "+test.get(i));
					}
				}

				List<NodeHandle> actuals;

				//Lists to store the volatiles after the routing table update
				List<Id>  volatilesInTable= new ArrayList<Id>();
				List<Id>  previousVolatilesInTable= new ArrayList<Id>();

				//Iterations to clean the volatiles from the routing table
				int timeToClean=TesterUtil.getLoopToFail();		
				Id obj=null;		
				boolean tableUpdated=false;
				while(timeToClean > 0){
					try {
						Thread.sleep(sleep);
					} catch (Exception e) {
						e.printStackTrace();		
					}	
					actuals= peer.getRoutingTable();		

					for(NodeHandle nd: actuals){
						obj=nd.getNodeId();
						log.info(" Successor NodeId "+obj+" is volatile "+volatiles.contains(obj));
						if(volatiles.contains(obj)){
							volatilesInTable.add(obj);
						}
					}

					//Comaparing both lists
					if(!previousVolatilesInTable.isEmpty()){
						for(Id id : previousVolatilesInTable){
							log.info("Previous NodeId "+id.toString());
							if(!volatilesInTable.contains(id)){
								log.info("Do not contains "+id.toString());
								tableUpdated=true;	
							}
						}
					}
					
					//Charging the previous list
					previousVolatilesInTable.clear();
					for(Id id : volatilesInTable){
						previousVolatilesInTable.add(id);						
					}			

					log.info("In "+timeToClean+" contains "+volatilesInTable.size()+" on "+actuals.size());
					//	Cleaning the actual list
					volatilesInTable.clear();
					
					//Demanding the routing table update
					peer.pingNodes();

					timeToClean--;
				}
				if(!tableUpdated)
					fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("[PastryTest] Peer bye bye");
	}
}