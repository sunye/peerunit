package fr.inria.peerunit.btreeStrategy;

import fr.inria.peerunit.Coordinator;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.util.BTreeNode;
import fr.inria.peerunit.util.HTree;
import fr.inria.peerunit.util.HNode;
import fr.inria.peerunit.util.TesterUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Veronique Pelleau
 * @author Aboubakar Koita
 */
public class ConcreteBtreeStrategy implements TreeStrategy {

    private static final Logger log = Logger.getLogger(ConcreteBtreeStrategy.class.getName());
    /**
     * default values for global variables
     */
    private TesterUtil defaults;
    /**
     * Tree containing Tester Id X Tester
     */
    private final HTree<Integer, Tester> testers;
    /**
     * Number of expected testers.
     */
    private final int expectedTesters;

    public ConcreteBtreeStrategy(TesterUtil tu) {
        defaults = tu;
        testers = new HTree<Integer, Tester>(defaults.getTreeOrder());
        expectedTesters = defaults.getExpectedTesters();
    }

    /**
     *
     * @param tester
     * @return
     * @throws RemoteException
     */
    public int register(Tester tester) throws RemoteException {
        log.entering("ConcreteBtreeStrategy", "register(Tester)");

        int id = testers.size() + 1;
        testers.put(new Integer(id), tester);
        synchronized (testers) {
            testers.notifyAll();
        }
        return id;
    }

    public void buildTree() {
    }

    public BTreeNode getNode(Integer i) {
        //return tree.getNode(i);
        return null;
    }

    public int getNodesSize() {
        //return tree.getNodesSize();
        return 0;
    }

    public void setCommunication() {
        HNode<Integer, Tester> node = testers.head();
        this.setCommunication(node);
    }

    private void setCommunication(HNode<Integer, Tester> n) {
        assert !n.isLeaf();

        HNode<Integer, Tester>[] children = n.children();
        List<Tester> nodes = new ArrayList<Tester>(children.length);
        for (HNode<Integer, Tester> each : children) {
            nodes.add(each.value());
        }
        
        try {
            Coordinator c = (Coordinator) n.value();
            c.registerTesters(nodes);
        } catch (RemoteException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        for (HNode<Integer, Tester> each : children) {
            if (!each.isLeaf()) {
                this.setCommunication(each);
            }
        }
    }

    public int getRegistered() {
        return testers.size();
    }

    /**
     * Waits for all expected testers to registerMethods.
     */
    public void waitForTesterRegistration() throws InterruptedException {
        log.entering("ConcreteBtreeStrategy", "waitForTesterRegistration()");
        log.info("Waiting for tester registration");
        while (testers.size() < expectedTesters) {
            log.fine("Comparing " + testers.size() + " with: " + expectedTesters);
            synchronized (testers) {
                testers.wait();
            }
        }

    }
}


