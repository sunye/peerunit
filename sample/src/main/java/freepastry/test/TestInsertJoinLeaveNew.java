package freepastry.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import rice.p2p.commonapi.Id;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastContentHandle;
import rice.tutorial.past.MyPastContent;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Network;
import freepastry.Peer;

/**
 * Test Insert/Retrieve in a Shrinking System
 * 
 * @author almeida
 * 
 */
public class TestInsertJoinLeaveNew extends TestCaseImpl {
	private static Logger log = Logger.getLogger(TestInsertJoinLeaveNew.class
			.getName());

	private static final int OBJECTS = TesterUtil.instance.getObjects();

	Peer peer = new Peer();

	int sleep = TesterUtil.instance.getSleep();

	List<Id> firstSuccessors = new ArrayList<Id>();

	int churnPercentage = TesterUtil.instance.getChurnPercentage();

	Map<Integer, Object> objList = new HashMap<Integer, Object>();

	List<String> expecteds = new ArrayList<String>();

	List<PastContent> keySet;

	@BeforeClass(range = "*", timeout = 1000000)
	public void bc() {
		log.info("[PastryTest] Starting test peer  ");
	}

	@TestStep(range = "0", timeout = 1000000, order = 1)
	public void startingNetwork() {
		try {
			Thread.sleep(2000);
			if (super.getPeerName() == 0) {
				Network net = new Network();
				if (!net.joinNetwork(peer, null, false, log)) {
					inconclusive("I couldn't become a boostrapper, sorry");
				}

				super.put(-10, net.getInetSocketAddress());
				log.info("Net created");

				while (!peer.isReady())
					Thread.sleep(16000);
			}
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "0", timeout = 1000000, order = 2)
	public void chosingPeer() {
		Random rand = new Random();
		List<Integer> generated = new ArrayList<Integer>();
		int chosePeer;
		int netSize = (TesterUtil.instance.getExpectedTesters() * TesterUtil.instance
				.getChurnPercentage()) / 100;
		log.info("It will join " + netSize + " peers");
		boolean peerChose;
		while (netSize > 0) {
			peerChose = false;
			while (!peerChose) {
				chosePeer = rand.nextInt(TesterUtil.instance
						.getExpectedTesters());
				if (chosePeer != 0) {
					Integer genInt = new Integer(chosePeer);
					if (!generated.contains(genInt)) {
						generated.add(genInt);
						peerChose = true;
						log.info("Chose peer " + genInt);
					}
				}
			}
			netSize--;
		}
		for (Integer intObj : generated) {
			super.put(intObj.intValue() * 100, intObj);
			if (intObj.intValue() % 2 == 0)
				log.info("leave " + intObj.intValue());
			else
				log.info("join " + intObj.intValue());
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 3)
	public void startingInitNet() {

		try {

			if (!chosenOne(super.getPeerName()).equalsIgnoreCase("join")
					&& (super.getPeerName() != 0)) {
				log.info("Joining before volatility");
				Network net = new Network();
				Thread.sleep(super.getPeerName() * 1000);

				InetSocketAddress bootaddress = (InetSocketAddress) super
						.get(-10);
				log.info("Getting cached boot " + bootaddress.toString());

				if (!net.joinNetwork(peer, bootaddress, false, log)) {
					inconclusive("I couldn't join, sorry");
				}

				log.info("Running on port " + peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 4)
	public void testInsert() {
		try {
			Thread.sleep(sleep);
			if (super.getPeerName() == 0) {
				List<PastContent> resultSet = new ArrayList<PastContent>();

				// these variables are final so that the continuation can access
				// them
				for (int i = 0; i < OBJECTS; i++) {
					final String s = "test"
							+ peer.env.getRandomSource().nextInt();

					// build the past content
					final PastContent myContent = new MyPastContent(
							peer.localFactory.buildId(s), s);

					peer.insert(myContent);
					resultSet.add(myContent);

				}
				super.put(-1, resultSet);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@TestStep(range = "*", timeout = 1000000, order = 5)
	public void testRetrieve() {

		try {
			if (!chosenOne(super.getPeerName()).equalsIgnoreCase("join")) {
				Thread.sleep(sleep);

				// Lookup first time
				keySet = (List<PastContent>) super.get(-1);
				Id contentKey;
				for (PastContent key : keySet) {
					contentKey = key.getId();
					if (contentKey != null) {
						log.info("[PastryTest] Lookup Expected "
								+ contentKey.toString());
						peer.lookup(contentKey);
					}
				}

				// Sleep
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
					e.printStackTrace();
				}

				log.info("[PastryTest] Retrieved so far "
						+ peer.getResultSet().size());

				for (Object expected : peer.getResultSet()) {
					if (expected != null) {
						log.info("Retrieve before depart "
								+ expected.toString());
						expecteds.add(expected.toString());
					}
				}
				if (super.getPeerName() == 0) {
					super.put(2, expecteds);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@TestStep(range = "*", timeout = 1000000, order = 6)
	public void volatility() {

		try {
			Thread.sleep(sleep);

			if (chosenOne(super.getPeerName()).equalsIgnoreCase("join")) {
				log.info("Joining after volatility");
				Network net = new Network();
				Thread.sleep(super.getPeerName() * 1000);

				InetSocketAddress bootaddress = (InetSocketAddress) super
						.get(-10);
				log.info("Getting cached boot " + bootaddress.toString());

				if (!net.joinNetwork(peer, bootaddress, false, log)) {
					inconclusive("I couldn't join, sorry");
				}

				log.info("Running on port " + peer.getPort());
				log.info("Time to bootstrap");

			} else if (chosenOne(super.getPeerName()).equalsIgnoreCase("leave")) {
				log.info("Leaving early ");
				super.kill();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 7)
	public void testInitialRetrieve() {
		try {
			if (!chosenOne(super.getPeerName()).equalsIgnoreCase("leave")) {
				List<String> actuals = new ArrayList<String>();
				Thread.sleep(sleep);
				Id contentKey;
				for (PastContent key : keySet) {
					contentKey = key.getId();
					if (contentKey != null) {
						log.info("[PastryTest] Lookup Expected "
								+ contentKey.toString());
						peer.lookup(contentKey);
					}
				}

				// Sleep
				try {
					Thread.sleep(sleep);
				} catch (Exception e) {
					e.printStackTrace();
				}

				log.info("[PastryTest] Retrieved so far "
						+ peer.getResultSet().size());

				for (Object actual : peer.getResultSet()) {
					if (actual != null) {
						log.info("Retrieve before depart " + actual.toString());
						actuals.add(actual.toString());
						super.put(super.getPeerName(), actuals);

					}
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	@TestStep(range = "*", timeout = 1000000, order = 8)
	public void buildExpecteds() {
		try {
			Set<Integer> newKeySet = super.getCollection().keySet();
			List<String> cached = new ArrayList<String>();
			Object obj;
			for (Integer key : newKeySet) {
				obj = super.get(key);
				if ((key.intValue() >= 0) && (key.intValue() < 64)) {
					cached = (List<String>) obj;
				}

				for (String cachedObj : cached) {
					if (!expecteds.contains(cachedObj)) {
						expecteds.add(cachedObj);
					}
				}
			}
			log.info("I may find " + expecteds.size() + " objects");
			for (String exp : expecteds) {
				log.info("I may find " + exp);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 9)
	public void testRetrieveByOthers() {
		try {
			if (!chosenOne(super.getPeerName()).equalsIgnoreCase("leave")) {
				Thread.sleep(sleep);

				// Lookup first time

				Id contentKey;
				for (PastContent key : keySet) {
					contentKey = key.getId();
					if (contentKey != null) {
						log.info("[PastryTest] Lookup Expected "
								+ contentKey.toString());
						peer.lookup(contentKey);
					}
				}

				Thread.sleep(sleep);

				log.info("[PastryTest] Retrieved so far "
						+ peer.getResultSet().size());

				List<String> actuals = new ArrayList<String>();
				int timeToFind = 0;
				while (timeToFind < TesterUtil.instance.getLoopToFail()) {
					log.info("Retrieval " + timeToFind);
					for (Object actual : peer.getResultSet()) {
						if (actual != null) {
							log.info("[Local verdict] Actual "
									+ actual.toString());

							if (!actuals.contains(actual.toString())) {
								actuals.add(actual.toString());
							}
						}
					}
					peer.pingNodes();
					Thread.sleep(sleep);
					timeToFind++;
				}

				// List<String> expecteds=(List<String>)test.get(2);
				log.info("[Local verdict] Waiting a Verdict. Found "
						+ actuals.size() + " of " + expecteds.size());
				// Assert.assertListEquals("[Local verdict] Arrays ",expecteds,
				// actuals);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 10)
	public void getHandle() {
		List<PastContent> cont = peer.getInsertedContent();
		PastContentHandle pch;
		for (PastContent pc : cont) {
			pch = pc.getHandle(peer.getPast());
			System.out.println("NodeHandle " + pch.getNodeHandle());
		}
	}

	@AfterClass(timeout = 100000, range = "*")
	public void end() {
		log.info("[PastryTest] Peer bye bye");
	}

	private String chosenOne(int name) {
		try {
			if (objList.isEmpty()) {
				objList = super.getCollection();
			}
			Set<Integer> keySet = objList.keySet();
			Object nameChose;

			for (Integer key : keySet) {
				nameChose = objList.get(key);
				if (nameChose instanceof Integer) {
					Integer new_name = (Integer) nameChose;
					if (new_name.intValue() == name) {
						if ((new_name.intValue() / 100) % 2 == 0)
							return "leave";
						else
							return "join";
					}
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return "remain";
	}
}
