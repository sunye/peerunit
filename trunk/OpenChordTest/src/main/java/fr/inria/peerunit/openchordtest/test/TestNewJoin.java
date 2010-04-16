package fr.inria.peerunit.openchordtest.test;

import static fr.inria.peerunit.tester.Assert.inconclusive;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.inria.peerunit.openchordtest.util.FreeLocalPort;
import de.uniba.wiai.lspi.chord.data.URL;
import de.uniba.wiai.lspi.chord.service.ServiceException;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test routing table update in an expanding system
 * @author almeida
 *
 */
public class TestNewJoin extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestNewJoin.class.getName());
    private List<String> routingTable = new ArrayList<String>();
    private static final long serialVersionUID = 1L;

    @TestStep(order = 2, timeout = 100000, range = "*")
    public void routingTable() throws InterruptedException {


        if (this.getPeerName() % 2 != 0) {
            chordPrint = (ChordImpl) chord;
            Thread.sleep(sleep);
            log.info("My ID is " + chord.getID());
            String[] succ = chordPrint.printSuccessorList().split("\n");
            String successor = null;
            for (int i = 0; i < succ.length; i++) {
                if (i > 0) {
                    successor = succ[i].toString().trim();
                    log.info("Successor List " + successor);
                    routingTable.add(successor);
                }
            }
        }

    }

    @TestStep(order = 3, timeout = 100000, range = "*")
    public void initOtherHalf() throws InterruptedException, MalformedURLException, UnknownHostException, ServiceException {

        if (this.getPeerName() % 2 == 0) {

            Thread.sleep(sleep);
            log.info("Peer name " + this.getPeerName());


            de.uniba.wiai.lspi.chord.service.PropertiesLoader.loadPropertyFile();
            String protocol = URL.KNOWN_PROTOCOLS.get(URL.SOCKET_PROTOCOL);


            String address = InetAddress.getLocalHost().toString();
            address = address.substring(address.indexOf("/") + 1, address.length());
            FreeLocalPort port = new FreeLocalPort();
            log.info("Address: " + address + " on port " + port.getPort());
            localURL = new URL(protocol + "://" + address + ":" + port.getPort() + "/");

            URL bootstrapURL = null;

            bootstrapURL = new URL(protocol + "://" + TesterUtil.instance.getBootstrap() + ":" + TesterUtil.instance.getBootstrapPort() + "/");


            chord = new de.uniba.wiai.lspi.chord.service.impl.ChordImpl();

            Thread.sleep(100 * this.getPeerName());
            log.info("LocalURL: " + localURL.toString());
            chord.join(localURL, bootstrapURL);

            log.info("Joining Chord DHT: " + chord.toString());



            log.info("Peer init");
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 5)
    public void testFindAgain() throws InterruptedException {

        if (this.getPeerName() % 2 != 0) {

            String[] succ = chordPrint.printSuccessorList().split("\n");

            String successor = null;
            int timeToUpdate = 0;
            boolean tableUpdated = false;
            while (!tableUpdated && timeToUpdate < TesterUtil.instance.getLoopToFail()) {
                for (int i = 0; i < succ.length; i++) {
                    if (i > 0) {
                        successor = succ[i].toString().trim();
                        log.info("New Successor List " + successor);
                        if (!routingTable.contains(successor)) {
                            tableUpdated = true;
                            break;
                        }
                    }
                }
                Thread.sleep(1000);
                timeToUpdate++;
            }
            if (!tableUpdated) {
                inconclusive("Routing Table wasn't updated. Increase qty of loops.");
            } else {
                log.info("List updated, the verdict may be PASS. Table updated " + timeToUpdate + " times.");
            }
        }

    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info(" Peer bye bye");
    }
}
