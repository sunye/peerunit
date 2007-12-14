package openchord.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import openchord.DbCallback;
import openchord.StringKey;
import util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
import de.uniba.wiai.lspi.chord.service.Key;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import fr.inria.peerunit.rmi.tester.TesterImpl;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test the recovery from peer isolation
 * @author almeida
 *
 */
public class TestPeerIsolation extends TesterImpl{
	private static Logger log = Logger.getLogger(TestPeerIsolation.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	static TestPeerIsolation test;

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	List<String> volatiles=new ArrayList<String>();

	private static final long serialVersionUID = 1L;

	StringKey key=null;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static DbCallback callback= new DbCallback();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	URL localURL = null;

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
		callback.setCallback(OBJECTS, log);
		test.run();
	}
	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("Starting test DHT ");
	}

	@Test(name="action1",measure=true,step=1,timeout=10000000, place=-1)
	public void init() {
		try{
			Thread.sleep(sleep);
			log.info("Peer name "+test.getPeerName());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {			
			e.printStackTrace();
		}				

		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile(); 
		String protocol = URL.KNOWN_PROTOCOLS[URL.SOCKET_PROTOCOL]; 

		try {
			String address = InetAddress.getLocalHost().toString();			
			address = address.substring(address.indexOf("/")+1,address.length());				
			FreeLocalPort port= new FreeLocalPort();
			log.info("Address: "+address+" on port "+port.getPort());			
			localURL = new URL(protocol + "://"+address+":"+port.getPort()+"/");
		} catch (MalformedURLException e){
			throw new RuntimeException(e); 
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		URL bootstrapURL=null;
		try {
			bootstrapURL = new URL(protocol + "://"+TesterUtil.getBootstrap()+":"+TesterUtil.getBootstrapPort()+"/");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl(); 
		try {
			Thread.sleep(100*test.getPeerName());
			log.info("LocalURL: "+localURL.toString());
			chord.join(localURL,bootstrapURL);			

			log.info("Joining Chord DHT: "+chord.toString());		
			test.put(test.getPeerName(), chord.getID());
		} catch (ServiceException e) {			
			e.printStackTrace();
			log.severe("Peer init exception");		
		} catch (Exception e){
			e.printStackTrace();
			log.severe("Peer init exception");		
		}

		log.info("Peer init");			    
	}

	@Test(name="action3",measure=true,step=0,timeout=10000000,place=-1)
	public void chooseAPeer() {		
		Random rand= new Random();		
		int chosePeer;
		try {
			Thread.sleep(sleep);
			if(test.getPeerName()==0){
				chosePeer = rand.nextInt(test.getCollection().size());
				ID id=(ID)test.get(chosePeer);				
				log.info("Chose peer "+chosePeer+" ID "+chord.getID());
				test.clear();		
				Thread.sleep(sleep);
				test.put(-1,chord.getID());
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
		chordPrint=(ChordImpl)chord;
		if(obj instanceof ID) {					
			ID id=(ID)obj;
			log.info("I am "+chord.getID()+" and the chose was  ID "+ id );

			// Only the chose peer store its table now
			if(chord.getID().toString().equals(id.toString())){
				log.info("Let's see the list");
				try{			
					Thread.sleep(sleep);

					String[] succ=chordPrint.printSuccessorList().split("\n");
					//storing my table
					test.put(-2, succ);

					String successor=null;
					for (int i = 0; i < succ.length; i++) {
						if(i>0){							
							successor=succ[i].toString().trim();
							log.info("Successor List "+successor);
							volatiles.add(successor);
						}
					}

				}catch (RuntimeException e) {
					log.severe("Could not find !"+e);			
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}
	}


	@Test(name="action5",measure=true,step=0,timeout=10000000,place=-1)
	public void testLeave() {		
		try {
			Thread.sleep(sleep);		

			String idToSearch=chord.getID().toString().substring(0,2)+" "+localURL.toString().trim();
			String[] succ=(String[] )test.get(-2);

			String successor=null;
			for (int i = 0; i < succ.length; i++) {
				successor=succ[i].toString().trim();
				if(successor.equalsIgnoreCase(idToSearch)){							
					//test.put(test.getPeerName(),idToSearch);
					log.info("Leaving early "+idToSearch);
					test.kill();				
					
					Thread.sleep(sleep);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}  
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void searchingNeighbours(){
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	

		Object obj=test.get(-1);
		chordPrint=(ChordImpl)chord;
		if(obj instanceof ID) {					
			ID id=(ID)obj;
			if(chord.getID().toString().equals(id.toString())){
				
				//Iterations to find someone in the routing table		
				int timeToClean=0;	
				
				boolean tableUpdated=false;
				while(!tableUpdated &&	timeToClean < TesterUtil.getLoopToFail()){
					log.info(" Let's verify the table "+timeToClean);
					try {
						Thread.sleep(sleep);
					} catch (Exception e) {
						e.printStackTrace();		
					}	
					// 	my list of successor
					String[] succ=chordPrint.printSuccessorList().split("\n");
					
					String successor=null;
					for (int i = 0; i < succ.length; i++) {
						if(i>0){							
							successor=succ[i].toString().trim();
							log.info("New Successor List "+successor);
							//if((successor.equalsIgnoreCase(chord.getID().toString().trim())) && (!volatiles.contains(successor))){
							if(!volatiles.contains(successor)){
								tableUpdated=true;
							}
						}
					}
					
					//Demanding the routing table update				
					timeToClean++;
				}
				if(!tableUpdated)
					fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
			}		
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {		
		log.info("[PastryTest] Peer bye bye");
	}

}
