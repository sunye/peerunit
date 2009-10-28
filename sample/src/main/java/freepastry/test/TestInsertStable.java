package freepastry.test;

import static fr.inria.peerunit.test.assertion.Assert.inconclusive;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import rice.environment.Environment;
import rice.p2p.past.PastContent;
import rice.tutorial.past.MyPastContent;
import util.FreeLocalPort;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.test.assertion.Assert;
import fr.inria.peerunit.util.TesterUtil;
import freepastry.Peer;

/**
 * Test Insert and retrieve on a stable system
 * 
 * @author almeida
 * 
 */
public class TestInsertStable extends TestCaseImpl {
	private static Logger log = Logger.getLogger(TestInsertStable.class
			.getName());

	Peer peer = new Peer();

	int sleep = TesterUtil.instance.getSleep();

	@BeforeClass(range = "*", timeout = 1000000)
	public void start() {
		log.info("Starting test peer  ");
	}

	@TestStep(range = "0", timeout = 1000000, order = 1)
	public void startingNetwork() {
		try {

			log.info("I am " + this.getPeerName());
			// Loads pastry settings
			Environment env = new Environment();

			// the port to use locally
			FreeLocalPort port = new FreeLocalPort();
			int bindport = port.getPort();
			log.info("LocalPort:" + bindport);

			// build the bootaddress from the command line args
			InetAddress bootaddr = InetAddress.getByName(TesterUtil.instance
					.getBootstrap());
			Integer bootport = new Integer(TesterUtil.instance
					.getBootstrapPort());
			InetSocketAddress bootaddress;

			bootaddress = new InetSocketAddress(bootaddr, bootport.intValue());
			if (!peer.join(bindport, bootaddress, env, log, true)) {
				inconclusive("I couldn't become a boostrapper, sorry");
			}

			this.put(-1, peer.getInetSocketAddress(bootaddr));
			// log.info("Cached boot address: "+bootaddress.toString());
			// test.put(-1,bootaddress);
			log.info("Net created");

			while (!peer.isReady())
				Thread.sleep(sleep);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 2)
	public void startingInitNet() {

		try {
			// Wait a while due to the bootstrapper performance
			Thread.sleep(sleep);
			if (this.getPeerName() != 0) {
				log.info("Joining in first");
				// Loads pastry settings
				Environment env = new Environment();

				// the port to use locally
				FreeLocalPort port = new FreeLocalPort();
				int bindport = port.getPort();
				log.info("LocalPort:" + bindport);

				Thread.sleep(this.getPeerName() * 1000);
				InetSocketAddress bootaddress = (InetSocketAddress) this
						.get(-1);
				log.info("Getting cached boot " + bootaddress.toString());
				if (!peer.join(bindport, bootaddress, env, log)) {
					inconclusive("Couldn't boostrap, sorry");
					this.put(this.getPeerName(), "INCONCLUSIVE");
				}
				log.info("Running on port " + peer.getPort());
				log.info("Time to bootstrap");

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stabilize the network.
	 */
	@TestStep(range = "*", timeout = 1000000, order = 3)
	public void stabilize() {
		for (int i = 0; i < 4; i++) {
			try {
				// Force the routing table update
				peer.pingNodes();
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * @TestStep(place=-1,timeout=1000000, name = "action3", order = 0) public
	 * void testInsert(){ try { Thread.sleep(test.getPeerName()*1000); String s;
	 * for(int i=0;i< TesterUtil.instance.getObjects();i++){ s = "" +
	 * ((test.getPeerName()*TesterUtil.instance.getObjects())+i);
	 * 
	 * // build the past content PastContent myContent = new
	 * MyPastContent(peer.localFactory.buildId(s), s); peer.insert(myContent);
	 * 
	 * }
	 * 
	 * } catch (RemoteException e) { e.printStackTrace(); } catch
	 * (InterruptedException e) { e.printStackTrace(); } }
	 */
	@TestStep(range = "*", timeout = 1000000, order = 4)
	public void testInsert() {
		try {
			Thread.sleep(sleep);
			if (this.getPeerName() == 0) {
				List<PastContent> resultSet = new ArrayList<PastContent>();

				// these variables are final so that the continuation can access
				// them
				for (int i = 0; i < TesterUtil.instance.getObjects(); i++) {
					final String s = "test"
							+ peer.env.getRandomSource().nextInt();

					// build the past content
					final PastContent myContent = new MyPastContent(
							peer.localFactory.buildId(s), s);

					peer.insert(myContent);
					resultSet.add(myContent);

				}
				this.put(-1, resultSet);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@TestStep(range = "*", timeout = 1000000, order = 5)
	public void testRetrieve() {
		try {
			Thread.sleep(sleep);
			String content;
			List<String> actuals = new ArrayList<String>();
			List<String> expecteds = new ArrayList<String>();
			int timeToFind = 0;
			while (timeToFind < TesterUtil.instance.getLoopToFail()) {
				for (int i = 0; i < TesterUtil.instance.getObjects(); i++) {
					log.info("lookup for " + i);
					// Build the content
					content = "" + i;

					if (bootstrapped(i)) {
						if (expecteds.size() < TesterUtil.instance.getObjects()) {
							expecteds.add(new MyPastContent(peer.localFactory
									.buildId(content), content).toString());
						}
						peer.lookup(peer.localFactory.buildId(content));
					}
				}

				Thread.sleep(sleep);

				log.info("Retrieval " + timeToFind);
				for (Object actual : peer.getResultSet()) {
					if (actual != null) {
						log.info("Actual " + actual.toString());

						if (!actuals.contains(actual.toString())) {
							actuals.add(actual.toString());
						}
					}
				}
				peer.pingNodes();
				Thread.sleep(sleep);
				timeToFind++;
			}
			log.info("Waiting a Verdict. Found " + actuals.size() + " of "
					+ expecteds.size());
			Assert.assertListEquals("[Local verdict] ", expecteds, actuals);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@AfterClass(timeout = 100000, range = "*")
	public void end() {
		log.info("Peer bye bye");
	}

	/**
	 * Returns true if the peer bootstrapped properly
	 * 
	 * @param i
	 * @return
	 */
	private boolean bootstrapped(int i) {

		try {
			for (Integer peer : this.getCollection().keySet()) {
				if (peer.intValue() == i)
					return false;

			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}
}
