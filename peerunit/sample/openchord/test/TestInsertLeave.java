package openchord.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
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
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
/**
 * Test E5 on experiments list
 * @author almeida
 *
 */
public class TestInsertLeave extends TesterImpl{
	private static Logger log = Logger.getLogger(TestInsertLeave.class.getName());
	private static final int OBJECTS=TesterUtil.getObjects();

	static TestInsertLeave test;

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

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

	Map<Integer,Object> objList=new HashMap<Integer, Object>();
	
	List<String> expecteds=new ArrayList<String>();

	public static void main(String[] str) {		
		test = new TestInsertLeave();
		test.export(test.getClass());		
		// Log creation
		FileHandler handler;
		try {
			System.out.println("NAME "+test.getPeerName());
			handler = new FileHandler(TesterUtil.getLogfolder()+"/TestInsertLeave.log.peer"+test.getPeerName(),true);
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

			} catch (ServiceException e) {			
				e.printStackTrace();
				log.severe("Peer init exception");		
			} catch (Exception e){
				e.printStackTrace();
				log.severe("Peer init exception");		
			}

			log.info("Peer init");			    

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {			
			e.printStackTrace();
		}			
	}

	@Test(place=0,timeout=1000000, name = "action2", step = 0)
	public void chosingPeer(){
		Random rand=new Random();
		List<Integer> generated=new ArrayList<Integer>();
		int chosePeer;
		int netSize= (TesterUtil.getExpectedPeers()*TesterUtil.getChurnPercentage())/100;
		log.info("It will join "+netSize+" peers");
		boolean peerChose;
		while(netSize >0){
			peerChose=false;
			while(!peerChose){
				chosePeer=rand.nextInt(TesterUtil.getExpectedPeers());
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

	@Test(place=0,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try{			
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < OBJECTS; i++) {					
			data = "" +i;
			log.info("[TestDbpartout] Inserting data "+data);
			key=new StringKey(data);			
			chord.insert(key,data,callback);
			insertedKeys.add(key);			
		}		

	}

	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){	
		
		try {
			Thread.sleep(sleep);
			if(!chosenOne(test.getPeerName())){
				for (int i = 0; i < OBJECTS; i++) {
					data = ""+ i;
					key=new StringKey(data);				
					chord.retrieve(key,callback);			
				}
				callback.retr ++;
				Thread.sleep(sleep);
				for (String actual : callback.getResultSet()) {
					log.info("Retrieve before depart "+actual);					
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}			
	}

	@Test(name="action5",measure=true,step=1,timeout=10000000, place=-1)
	public void leaving() {
		try{
			if(chosenOne(test.getPeerName())){
				log.info("Leaving early ");
				test.kill();				
			}				
		} catch (RemoteException e) {			
			e.printStackTrace();
		}			
	}

	@Test(place=-1,timeout=1000000, name = "action6", step = 0)
	public void testInitialRetrieve(){		

		try {			
			if(!chosenOne(test.getPeerName())){
				List<String> actuals=new ArrayList<String>();
				Thread.sleep(sleep);
				
				for (int i = 0; i < OBJECTS; i++) {
					data = ""+ i;
					key=new StringKey(data);				
					chord.retrieve(key,callback);			
				}
				callback.retr ++;
				Thread.sleep(sleep);
				for (String actual : callback.getResultSet()) {
					log.info("Retrieve after depart "+actual);
					actuals.add(actual);			
					test.put(test.getPeerName(), actuals);
				}				
			}
		} catch (InterruptedException e) {				
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}		
	}

	@Test(place=-1,timeout=1000000, name = "action7", step = 0)
	public void buildExpecteds(){	
		try {
			Set<Integer> keySet=test.getCollection().keySet();
			List<String> cached=new ArrayList<String>();
			Object obj;
			for(Integer key: keySet){
				obj=test.get(key);
				if (!(obj instanceof Integer)) {
					cached=(List<String>) obj;					
				}
				
				for(String cachedObj:cached){
					log.info("Cached "+cached);
					if(!expecteds.contains(cachedObj)){
						expecteds.add(cachedObj);
					}
				}				
			}
			log.info("I may find "+expecteds.size()+" objects");
			for(String exp:expecteds){
				log.info("I may find "+exp);
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test(place=-1,timeout=1000000, name = "action8", step = 0)
	public void testFinalRetrieve(){		

		try {			
			if(!chosenOne(test.getPeerName())){
				Thread.sleep(sleep);
				List<String> actuals=new ArrayList<String>();
				int timeToFind=0;			
				while(timeToFind < TesterUtil.getLoopToFail()){
					for (int i = 0; i < OBJECTS; i++) {
						data = ""+ i;
						key=new StringKey(data);				
						chord.retrieve(key,callback);			
					}
					callback.retr ++;
					Thread.sleep(sleep);
					for (String actual : callback.getResultSet()) {
						log.info("Final retrieve "+timeToFind+" got "+actual);
						if(!actuals.contains(actual.toString())){
							actuals.add(actual);
						}else{
							log.info("Already have "+actual);
						}
					}
					timeToFind++;			
				}
				//List<String> expecteds=(List<String>)test.get(0);		
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
		log.info(" Peer bye bye");
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
