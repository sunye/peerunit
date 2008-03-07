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
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.Test;
import static fr.inria.peerunit.test.assertion.Assert.*;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.LogFormat;
import fr.inria.peerunit.util.TesterUtil;

public class TestQueryTheoremC extends TestCaseImpl{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	StringKey key=null;

	String data="";

	private static AsynChord chord=null;

	static ChordImpl chordPrint = null;

	private static Logger log = Logger.getLogger(TestQueryTheorem.class.getName());

	private static final int OBJECTS=TesterUtil.getObjects();

	private static DbCallback callback= new DbCallback();

	static TestQueryTheoremC test;

	int sleep=TesterUtil.getSleep();

	int actualResults=0;

	int expectedResults=0;

	private Collection<Key> insertedKeys= new ArrayList<Key>(OBJECTS);

	public TestQueryTheoremC() {
		super();callback.setCallback(OBJECTS, log);
	}

	@BeforeClass(place=-1,timeout=1000000)
	public void bc(){
		log.info("[Dbpartout] Starting test DHT ");
	}

	@Test(name="action1",measure=true,step=1,timeout=10000000, place=-1)
	public void init() {
		URL localURL = null;
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("[TestDbpartout] Peer name "+test.getName());

		de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
		String protocol = URL.KNOWN_PROTOCOLS[URL.SOCKET_PROTOCOL];

		try {
			String address = InetAddress.getLocalHost().toString();
			address = address.substring(address.indexOf("/")+1,address.length());
			FreeLocalPort port= new FreeLocalPort();
			log.info("[TestFind]  Address: "+address+" on port "+port.getPort());
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
			log.info("[TestDbpartout] LocalURL: "+localURL.toString());
			chord.join(localURL,bootstrapURL);

			log.info("[TestDbpartout] Joining Chord DHT: "+chord.toString());
		} catch (ServiceException e) {
			e.printStackTrace();
			log.severe("[TestDbpartout] Peer init exception");
		} catch (Exception e){
			e.printStackTrace();
			log.severe("[TestDbpartout] Peer init exception");
		}

		log.info("[TestDbpartout] Peer init");
	}

	@Test(name="action2",measure=true,step=1,timeout=10000000, place=-1)
	public void find() {

		chordPrint=(ChordImpl)chord;
		try{
			Thread.sleep(sleep);
			log.info("[TestDbpartout] My ID is "+chord.getID());
			String[] succ=chordPrint.printSuccessorList().split("\n");
			for (String succList : succ) {
				log.info("[TestDbpartout] Successor List "+succList+" size "+succ.length );
			}
			String[] finger=chordPrint.printFingerTable().split("\n");
			for (String fingList : finger) {
				log.info("[TestDbpartout] FingerTable "+fingList);
			}

			String[] ref=chordPrint.printReferences().split("\n");
			for (String refList : ref) {
				log.info("[TestDbpartout] ReferenceTable "+refList);
			}
		}catch (RuntimeException e) {
			log.severe("[TestDbpartout] Could not find !"+e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test(name="action3",measure=true,step=1,timeout=10000000, place=1)
	public void testInsert() {
		List<String> resultSet=new ArrayList<String>();
		try{
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (int i = 1; i < OBJECTS; i++) {
			data = "" +i;
			log.info("[TestDbpartout] Inserting data "+data);
			key=new StringKey(data);
			chord.insert(key,data,callback);
			insertedKeys.add(key);
			resultSet.add(data);
		}

		log.info("[TestDbpartout] Will cache ");
		for(String cacheData: resultSet){
			log.info("[TestDbpartout] Caching data "+cacheData);
		}
		test.put(2, resultSet);
	}

	@Test(name="action4",measure=true,step=1,timeout=10000000,place=-1)
	public void testLeave() {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("[TestDbpartout] EntriesTable ");
		String[] ent=chordPrint.printEntries().split("\n");
		for (String entList : ent) {
			log.info("[TestDbpartout] EntriesTable "+entList);
		}

		if(test.getName()%2==0){
			log.info("[TestDbpartout] Leaving early");
			try {
				chord.leave();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
	}

	@Test(name="action5",measure=true,step=1,timeout=10000000,place=-1)
	public void testRetrieve() {
		if(test.getName()%2!=0){
			List<String> expecteds=null;
			while(expecteds==null){
				expecteds=(List<String>)test.get(2);
				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			for(int retrieveQty=0;retrieveQty<2;retrieveQty++){
				for (int i = 0; i < OBJECTS; i++) {
					data = ""+ i;
					key=new StringKey(data);
					chord.retrieve(key,callback);
				}
				callback.retr ++;
				String[] succ=chordPrint.printSuccessorList().split("\n");
				for (String succList : succ) {
					log.info("[TestDbpartout] Successor List "+succList+" size "+succ.length );
				}
				String[] finger=chordPrint.printFingerTable().split("\n");
				for (String fingList : finger) {
					log.info("[TestDbpartout] FingerTable "+fingList);
				}

				String[] ref=chordPrint.printReferences().split("\n");
				for (String refList : ref) {
					log.info("[TestDbpartout] ReferenceTable "+refList);
				}

				log.info("[TestDbpartout] EntriesTable ");
				String[] ent=chordPrint.printEntries().split("\n");
				for (String entList : ent) {
					log.info("[TestDbpartout] EntriesTable "+entList);
				}

				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				log.info("[Local verdict] Verifying Expected ");

				/**
				 * Here v represents the first insert
				 */
				for (String expected : expecteds) {
					log.info("[Local verdict] Expected "+expected);
				}
				for (String actual : callback.getResultSet()) {
					log.info("[Local verdict] Actual "+actual);
				}
				log.info("[Local verdict] Retrieval "+callback.retr+" " + expecteds.size()+" "+ callback.getSizeExpected());

				Assert.assertListEquals("[Local verdict] Arrays ",expecteds, callback.getResultSet());

				callback.clearResultSet();
				log.info("[Local verdict] New Retrieval will start " + expecteds.size()+" "+ callback.getSizeExpected());
			}
			log.info("[TestDbpartout] Inserted data size "+insertedKeys.size());
		}
	}

	@AfterClass(timeout=100000,place=-1)
	public void end() {
		if(test.getName()%2!=0){
			try {
				Thread.sleep(sleep);
				chord.leave();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}

			log.info("[TestDbpartout] Peer bye bye");
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
