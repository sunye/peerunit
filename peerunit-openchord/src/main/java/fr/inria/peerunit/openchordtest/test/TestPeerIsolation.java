package fr.inria.peerunit.openchordtest.test;

import static fr.inria.peerunit.tester.Assert.fail;

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

    private static final Logger LOG = Logger.getLogger(TestPeerIsolation.class.getName());
    private List<String> volatiles = new ArrayList<String>();
    private static final long serialVersionUID = 1L;

    public TestPeerIsolation() {
        super();
        callback.setCallback(OBJECTS, LOG);
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
            LOG.info("Chose peer " + chosePeer + " ID " + getChord().getID());
            this.clear();
            Thread.sleep(sleep);
            this.put(-1, getChord().getID());
        }

    }

    @TestStep(order = 4, timeout = 100000, range = "*")
    public void listingTheNeighbours() throws RemoteException, InterruptedException {

        Thread.sleep(sleep);


        Object obj = this.get(-1);
        chordPrint = (ChordImpl) getChord();
        if (obj instanceof ID) {
            ID id = (ID) obj;
            LOG.info("I am " + getChord().getID() + " and the chose was  ID " + id);

            // Only the chose peer store its table now
            if (getChord().getID().toString().equals(id.toString())) {
                LOG.info("Let's see the list");

                Thread.sleep(sleep);

                String[] succ = chordPrint.printSuccessorList().split("\n");
                //storing my table
                this.put(-2, succ);

                String successor = null;
                for (int i = 0; i < succ.length; i++) {
                    if (i > 0) {
                        successor = succ[i].toString().trim();
                        LOG.info("Successor List " + successor);
                        volatiles.add(successor);
                    }
                }



            }
        }
    }

    @TestStep(order = 5, timeout = 100000, range = "*")
    public void testLeave() throws RemoteException, InterruptedException {

        Thread.sleep(sleep);

        String idToSearch = getChord().getID().toString().substring(0, 2) + " " + localURL.toString().trim();
        String[] succ = (String[]) this.get(-2);

        String successor = null;
        for (int i = 0; i < succ.length; i++) {
            successor = succ[i].toString().trim();
            if (successor.equalsIgnoreCase(idToSearch)) {
                //test.put(test.getPeerName(),idToSearch);
                LOG.info("Leaving early " + idToSearch);
                this.kill();

                Thread.sleep(sleep);
            }
        }

    }

    @TestStep(range = "*", timeout = 10000, order = 6)
    public void searchingNeighbours() throws RemoteException, InterruptedException {

        Object obj = this.get(-1);
        chordPrint = (ChordImpl) getChord();
        if (obj instanceof ID) {
            ID id = (ID) obj;
            if (getChord().getID().toString().equals(id.toString())) {

                //Iterations to find someone in the routing table
                int timeToClean = 0;

                boolean tableUpdated = false;
                while (!tableUpdated && timeToClean < TesterUtil.instance.getLoopToFail()) {
                    LOG.info(" Let's verify the table " + timeToClean);


                    Thread.sleep(1000);

                    // 	my list of successor
                    String[] succ = chordPrint.printSuccessorList().split("\n");

                    String successor = null;
                    for (int i = 0; i < succ.length; i++) {
                        if (i > 0) {
                            successor = succ[i].toString().trim();
                            LOG.info("New Successor List " + successor);
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
        LOG.info("[OpenChord] Peer bye bye");
    }
}
