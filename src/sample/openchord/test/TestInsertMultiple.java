package openchord.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import openchord.DbCallback;
import openchord.StringKey;
import util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.AsynChord;
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
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestInsertMultiple extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestInsertMultiple.class.getName());
	private static final int OBJECTS=TesterUtil.getObjects();

	static TestInsertMultiple test;

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



	URL localURL = null;


	public TestInsertMultiple() {
		super();callback.setCallback(OBJECTS, log);
		// TODO Auto-generated constructor stub
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


		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);

		try {
			log.info("Peer name "+test.getPeerName());
			String address = InetAddress.getLocalHost().toString();
			address = address.substring(address.indexOf("/")+1,address.length());
			FreeLocalPort port= new FreeLocalPort();
			log.info("Address: "+address+" on port "+port.getPort());
			localURL = new URL(protocol + "://"+address+":"+port.getPort()+"/");
		} catch (MalformedURLException e){
			throw new RuntimeException(e);
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Test(name="action3",measure=true,step=1,timeout=10000000, from=0,to=9)
	public void testInsert() {
		try{
			Thread.sleep(sleep);
			data = "" +test.getPeerName();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		log.info("[TestDbpartout] Inserting data "+data);
		key=new StringKey(data);
		chord.insert(key,data,callback);

		log.info("[TestDbpartout] Will cache ");
		log.info("[TestDbpartout] Caching data "+data);

		try {
			test.put(test.getPeerName(), data);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){
		List<String> actuals=new ArrayList<String>();
		try {

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

			// Loading expecteds
			List<String> expecteds=new ArrayList<String>();
			Set<Integer> keySet=test.getCollection().keySet();
			Object data;
			for(Integer globalKey: keySet){
				data=test.get(globalKey);
				//if (data instanceof String) {
					log.info("Expected "+(String) data);
					expecteds.add((String) data);
				//}
			}

			log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
			Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
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

}
