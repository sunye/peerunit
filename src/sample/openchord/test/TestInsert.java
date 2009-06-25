package openchord.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import static fr.inria.peerunit.test.assertion.Assert.*;
/**
 * Test E3 on experiments list
 * @author almeida
 *
 */
public class TestInsert extends TestCaseImpl{
	private static Logger log = Logger.getLogger(TestInsert.class.getName());
	private static final int OBJECTS=TesterUtil.instance.getObjects();

	static TestInsert test;

	int sleep=TesterUtil.instance.getSleep();

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

	Map<Integer,Object> objList=new HashMap<Integer, Object>();

	public TestInsert() {
		super();callback.setCallback(OBJECTS, log);
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
			bootstrapURL = new URL(protocol + "://"+TesterUtil.instance.getBootstrap()+":"+TesterUtil.instance.getBootstrapPort()+"/");
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

	@Test(place=0,timeout=1000000, name = "action3", step = 0)
	public void testInsert(){
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		List<String> resultSet=new ArrayList<String>();
		for (int i = 1; i < OBJECTS; i++) {
			data = "" +i;
			log.info("[TestDbpartout] Inserting data "+data);
			key=new StringKey(data);
			chord.insert(key,data,callback);
			resultSet.add(data);
		}
		try {
			while(!callback.isInserted()){
				Thread.sleep(sleep);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		test.put(0, resultSet);
	}

	@Test(place=-1,timeout=1000000, name = "action4", step = 0)
	public void testRetrieve(){
		List<String> actuals=new ArrayList<String>();
		try {

			int timeToFind=0;
			while(timeToFind < TesterUtil.instance.getLoopToFail()){
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
			List<String> expecteds=(List<String>)test.get(0);
			log.info("[Local verdict] Waiting a Verdict. Found "+actuals.size()+" of "+expecteds.size());
			Assert.assertListEquals("[Local verdict] Arrays ",expecteds, actuals);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		log.info(" Peer bye bye");
	}

}
