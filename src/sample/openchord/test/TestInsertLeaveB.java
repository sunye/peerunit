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
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import static fr.inria.peerunit.test.assertion.Assert.*;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;
/**
 * Test E5B on experiments list
 * @author almeida
 *
 */
public class TestInsertLeaveB extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestInsertLeaveB.class.getName());
	private static final int OBJECTS=TesterUtil.getObjects();

	static TestInsertLeaveB test;

	int sleep=TesterUtil.getSleep();

	boolean iAmBootsrapper=false;

	private static final long serialVersionUID = 1L;

	Key key;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static DbCallback callback= new DbCallback();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	URL localURL = null;

	Map<Integer,Object> objList=new HashMap<Integer, Object>();

	public TestInsertLeaveB() {
		super();callback.setCallback(OBJECTS, log);
	}

	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("Starting test DHT ");
	}

	@Test(name="action1",measure=true,step=0,timeout=10000000, place=-1)
	public void init() {
		try{


			Thread.sleep(test.getPeerName()*1000);
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

	@Test(place=-1,timeout=1000000, name = "action3", step = 0)
	public void stabilize(){
		int timeToFind=0;
		while(timeToFind < TesterUtil.getLoopToFail()){
			try{
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			timeToFind++;
		}

	}

	@Test(place=0,timeout=1000000, name = "action4", step = 0)
	public void testInsert(){
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Map<Key, String> map= new HashMap<Key, String>();
		for (int i = 0; i < OBJECTS; i++) {
			data = "" +i;
			log.info("[TestDbpartout] Inserting data "+data);
			key=new StringKey(data);
			chord.insert(key,data,callback);
			map.put(key, data);
		}

		while(!callback.isInserted()){
			try{
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				log.warning("Thread problem on inserting");
				e.printStackTrace();
			}
		}
		List<String> expecteds=new ArrayList<String>();
		for(Key expectedKey: callback.getInsertedKeys()){
			expecteds.add(map.get(expectedKey));
		}
		test.put(0, expecteds);
	}

	@Test(place=-1,timeout=1000000, name = "action5", step = 0)
	public void testRetrieve(){
		try {
			Thread.sleep(sleep);

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
					log.info("Retrieve before leave "+timeToFind+" got "+actual);
				}
				callback.clearResultSet();
				timeToFind++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(name="action6",measure=true,step=1,timeout=10000000, place=-1)
	public void printSuccList() {
		try{
			Thread.sleep(sleep);
			String[] succ;
			//storing my table
			String successor=null;
			chordPrint=(ChordImpl)chord;
			succ=chordPrint.printSuccessorList().split("\n");
			for (int i = 0; i < succ.length; i++) {
				if(i>0){
					successor=succ[i].toString().trim();
					log.info("Successor List before "+successor);
				}
			}
			String[] entr=chordPrint.printEntries().split("\n");
			String entry=null;
			for (int i = 0; i < entr.length; i++) {
				if(i>0){
					entry=entr[i].toString().trim();
					log.info("Entries before "+entry);
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test(name="action7",measure=true,step=1,timeout=10000000, place=-1)
	public void leaving() {
		try{
			Thread.sleep(sleep);
			if(chosenOne(test.getPeerName())){
				log.info("Leaving early ");
				chord.leave();
			}
			String[] succ;
			//storing my table
			String successor=null;
			Thread.sleep(sleep);
			chordPrint=(ChordImpl)chord;
			succ=chordPrint.printSuccessorList().split("\n");
			for (int i = 0; i < succ.length; i++) {
				if(i>0){
					successor=succ[i].toString().trim();
					log.info("Successor List after "+successor);
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
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
					callback.clearResultSet();
					timeToFind++;
				}

				String[] entr=chordPrint.printEntries().split("\n");
				String entry=null;
				for (int i = 0; i < entr.length; i++) {
					if(i>0){
						entry=entr[i].toString().trim();
						log.info("Entries after "+entry);
					}
				}

				List<String> expecteds=(List<String>)test.get(0);
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
				if ((nameChose instanceof Integer)&&(key.intValue()>=100)) {
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
