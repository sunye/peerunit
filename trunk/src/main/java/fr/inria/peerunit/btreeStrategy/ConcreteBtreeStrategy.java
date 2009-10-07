package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import fr.inria.peerunit.Tester;
import fr.inria.peerunit.btree.TreeElements;
import fr.inria.peerunit.util.BTreeImpl;
import fr.inria.peerunit.util.BTreeNode;
import fr.inria.peerunit.util.TesterUtil;
import java.util.Collections;

/**
 * @author Veronique Pelleau
 * @author Aboubakar Koïta
 */
public class ConcreteBtreeStrategy implements TreeStrategy {

    /**
     * Map containing Tester Id X Tester
     */
    private final Map<Integer, Tester> testers;
    /**
     * default values for global variables
     */
    TesterUtil defaults;
    private BTreeImpl btree;
    
    /**
     * Number of expected testers.
     */
    private final int expectedTesters;

    public ConcreteBtreeStrategy(TesterUtil tu) {
        defaults = tu;
        btree = new BTreeImpl(defaults.getTreeOrder());
        testers = Collections.synchronizedMap(new HashMap<Integer, Tester>());
        expectedTesters = defaults.getExpectedTesters();

    }

    /**
     *
     * @param tester
     * @return
     * @throws RemoteException
     */
    public int register(Tester tester) throws RemoteException {
        int id = testers.size() + 1;
        testers.put(new Integer(id), tester);
        testers.notifyAll();
        return id;
    }

    public void buildTree() {
        btree.buildTree();
    }

    public BTreeNode getNode(Integer i) {
        return btree.getNode(i);
    }

    public int getNodesSize() {
        return btree.getNodesSize();
    }

    public void setCommunication() {
        //TODO Clean this method
        //Node tester;

        for (Integer key : testers.keySet()) {
            TreeElements te = new TreeElements();
            if (!getNode(key).isLeaf()) {
                for (BTreeNode child : getNode(key).getChildren()) {
                    if (child != null) {
                        //te.setChildren(testers.get(child.getId()));
                    }
                }
            } else {
                te.setChildren(null);
            }

            if (!getNode(key).isRoot()) {
                //int parentId = getNode(key).getParent().getId();
                //te.setParent(testers.get(parentId));
            }
            /**
             * Now we inform Node its tree elements.
             */
            /*
            tester = testers.get(key);
            System.out.println("[Bootstrapper] Contacting Node " + tester);
            try {
            tester.setElements(getNode(key), te);
            } catch (RemoteException e) {
            e.printStackTrace();
            }
             */
        }
    }

    public int getRegistered() {
        return testers.size();
    }

    /**
     * Waits for all expected testers to registerMethods.
     */
    public void waitForTesterRegistration() throws InterruptedException {

        while (testers.size() < expectedTesters) {
            synchronized (testers) {
                testers.wait();
            }
        }

    }
}


