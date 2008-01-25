
package openchord.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import openchord.DbCallback;
import openchord.StringKey;
import util.FreeLocalPort;
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

public class TestFindSuccTheoremB extends TesterImpl{

	private static final long serialVersionUID = 1L;

	StringKey key=null;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static Logger log = Logger.getLogger(TestFindSuccTheoremB.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	private static DbCallback callback= new DbCallback();

	static TestFindSuccTheoremB test;

	int sleep=TesterUtil.getSleep();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	URL localURL = null;
	/**
	 * @param args
	 */
	public static void main(String[] str) {		
		test = new TestFindSuccTheoremB();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestFindSuccTheoremB.log.peer"+test.getName(),true);
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Peer name "+test.getName());		

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
			Thread.sleep(100*test.getName());
			log.info("LocalURL: "+localURL.toString());
			chord.join(localURL,bootstrapURL);			

			log.info("Joining Chord DHT: "+chord.toString());				
		} catch (ServiceException e) {			
			e.printStackTrace();
			log.severe("Peer init exception");		
		} catch (Exception e){
			e.printStackTrace();
			log.severe("Peer init exception");		
		}

		log.info("Peer init");			    
	}

	@Test(name="action2",measure=true,step=1,timeout=10000000, place=-1)
	public void find() {

		chordPrint=(ChordImpl)chord;
		try{			
			Thread.sleep(sleep);		
			log.info("My ID is "+chord.getID());
			String[] succ=chordPrint.printSuccessorList().split("\n");
			for (String succList : succ) {
				log.info("Successor List "+succList+" size "+succ.length );
			}

		}catch (RuntimeException e) {
			log.severe("Could not find !"+e);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(name="action4",measure=true,step=1,timeout=10000000,place=-1)
	public void testLeave() {		
		try {
			Thread.sleep(sleep);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		
		if(test.getName()%10==9){
			log.info("Leaving early");
			try {
				chord.leave();
				String insertValue=chord.getID().toString().substring(0,2)+" "+localURL.toString();
				test.put(test.getName(), insertValue);
				log.info("Cached "+ insertValue);				
			} catch (ServiceException e) {				
				e.printStackTrace();
			}
		}		
		int maxSize=(TesterUtil.getExpectedPeers()/2);
		log.info("MAX SIZE "+maxSize );
		int newSize=0;
		try {						
			while(test.getCollection().size() < maxSize){
				log.info("Cached size "+test.getCollection().size() );
				Thread.sleep(1000);	
				newSize++;
				if(newSize == 15)
					maxSize=test.getCollection().size();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test(name="action5",measure=true,step=1,timeout=10000000,place=-1)
	public void testRetrieve() {		
		try {
			Thread.sleep(sleep);			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(test.getName()%10!=9){
			String[] immediateSuccessor=chordPrint.printSuccessorList().split("\n");
			String successor=null;
			for (int i = 0; i < immediateSuccessor.length; i++) {
				if(i==1)
					successor=immediateSuccessor[i].toString();
			}

			log.info("Immediate successor "+successor);

			List<String> listQuitPeers = new ArrayList<String>();
			String quitPeer;
			for(int i=0;i< TesterUtil.getExpectedPeers();i++){
				if(i%10==9){
					if(test.get(i)!=null){
						quitPeer=test.get(i).toString().trim();
						listQuitPeers.add(quitPeer);
						log.info("Quit peer "+quitPeer);
					}
				}				
			}
			 String[] succ=chordPrint.printSuccessorList().split("\n");
                        for (String succList : succ) {
                                log.info("Successor List "+succList+" size "+succ.length );
                        }
	
			if(listQuitPeers.contains(successor.trim())){
					log.info("Contains in GV " + successor);
					assertTrue("Successor wasn't updated correctly ",false);
			}else log.info("Not Contains in GV " + successor);
			
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		if(test.getName()%10!=9){
			try {
				Thread.sleep(sleep);
				chord.leave();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}	

			log.info("Peer bye bye");
		}
	}

	private int getName(){
		int peerName=0;
		try {
			peerName= super.getPeerName();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return peerName;
	}

}

