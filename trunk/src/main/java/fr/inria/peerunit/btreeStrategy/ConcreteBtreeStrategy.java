/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.btreeStrategy;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.util.HNode;
import fr.inria.peerunit.util.HTree;
import fr.inria.peerunit.util.TesterUtil;

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
            log.fine(String.format("Registering %d testers to tester %d", nodes.size(), n.value().getId()));
        } catch (RemoteException ex) {
            Logger.getLogger(ConcreteBtreeStrategy.class.getName()).log(Level.SEVERE, null, ex);
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

        while (testers.size() < expectedTesters) {
            log.fine(String.format("Waiting for %d testers to register", 
                    expectedTesters - testers.size()));
            synchronized (testers) {
                testers.wait();
            }
        }

    }

    public void startRoot() throws RemoteException {
        testers.head().value().start();
    }

    public void cleanUp() {
        testers.clear();
    }
}


