package fr.inria.peerunit.openchordtest.test;

import static fr.inria.peerunit.test.assertion.Assert.fail;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import de.uniba.wiai.lspi.chord.data.ID;
import de.uniba.wiai.lspi.chord.service.impl.ChordImpl;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

/**
 * Test the recovery from peer isolation
 * @author almeida
 *
 */
public class TestPeerIsolation extends AbstractOpenChordTest {

    private static Logger log = Logger.getLogger(TestPeerIsolation.class.getName());
    private List<String> volatiles = new ArrayList<String>();
    private static final long serialVersionUID = 1L;

    public TestPeerIsolation() {
        super();
        callback.setCallback(OBJECTS, log);
        // TODO Auto-generated constructor stub
    }

    @TestStep(order = 3, timeout = 100000, range = "*")
    public void chooseAPeer() throws InterruptedException, RemoteException {
        Random rand = new Random();
        int chosePeer;

        Thread.sleep(sleep);
        if (this.getPeerName() == 0) {
            chosePeer = rand.nextInt(this.getCollection().size());
            ID id = (ID) this.get(chosePeer);
            log.info("Chose peer " + chosePeer + " ID " + chord.getID());
            this.clear();
            Thread.sleep(sleep);
            this.put(-1, chord.getID());
        }

    }

    @TestStep(order = 4, timeout = 100000, range = "*")
    public void listingTheNeighbours() throws RemoteException, InterruptedException {

        Thread.sleep(sleep);


        Object obj = this.get(-1);
        chordPrint = (ChordImpl) chord;
        if (obj instanceof ID) {
            ID id = (ID) obj;
            log.info("I am " + chord.getID() + " and the chose was  ID " + id);

            // Only the chose peer store its table now
            if (chord.getID().toString().equals(id.toString())) {
                log.info("Let's see the list");

                Thread.sleep(sleep);

                String[] succ = chordPrint.printSuccessorList().split("\n");
                //storing my table
                this.put(-2, succ);

                String successor = null;
                for (int i = 0; i < succ.length; i++) {
                    if (i > 0) {
                        successor = succ[i].toString().trim();
                        log.info("Successor List " + successor);
                        volatiles.add(successor);
                    }
                }



            }
        }
    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void testLeave() throws RemoteException, InterruptedException {

        Thread.sleep(sleep);

        String idToSearch = chord.getID().toString().substring(0, 2) + " " + localURL.toString().trim();
        String[] succ = (String[]) this.get(-2);

        String successor = null;
        for (int i = 0; i < succ.length; i++) {
            successor = succ[i].toString().trim();
            if (successor.equalsIgnoreCase(idToSearch)) {
                //test.put(test.getPeerName(),idToSearch);
                log.info("Leaving early " + idToSearch);
                this.kill();

                Thread.sleep(sleep);
            }
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 6)
    public void searchingNeighbours() throws RemoteException, InterruptedException {

        Object obj = this.get(-1);
        chordPrint = (ChordImpl) chord;
        if (obj instanceof ID) {
            ID id = (ID) obj;
            if (chord.getID().toString().equals(id.toString())) {

                //Iterations to find someone in the routing table
                int timeToClean = 0;

                boolean tableUpdated = false;
                while (!tableUpdated && timeToClean < TesterUtil.instance.getLoopToFail()) {
                    log.info(" Let's verify the table " + timeToClean);


                    Thread.sleep(1000);

                    // 	my list of successor
                    String[] succ = chordPrint.printSuccessorList().split("\n");

                    String successor = null;
                    for (int i = 0; i < succ.length; i++) {
                        if (i > 0) {
                            successor = succ[i].toString().trim();
                            log.info("New Successor List " + successor);
                            //if((successor.equalsIgnoreCase(chord.getID().toString().trim())) && (!volatiles.contains(successor))){
                            if (!volatiles.contains(successor)) {
                                tableUpdated = true;
                            }
                        }
                    }

                    //Demanding the routing table update
                    timeToClean++;
                }
                if (!tableUpdated) {
                    fail("Routing Table wasn't updated. Still finding all volatiles. Increase qty of loops.");
                }
            }
        }
    }

    @AfterClass(timeout = 100000, range = "*")
    public void end() {
        log.info("[OpenChord] Peer bye bye");
    }
}
