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
package fr.inria.peerunit.bootstrapper;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.inria.peerunit.remote.DistributedTester;
import fr.inria.peerunit.util.HNode;
import fr.inria.peerunit.util.HTree;
import fr.inria.peerunit.util.TesterUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author sunye
 */
public class GridStrategy implements TreeStrategy {

    private static final Logger LOG = Logger.getLogger(GridStrategy.class.getName());
    /**
     * default values for global variables
     */
    private TesterUtil defaults;
    /**
     * Map containing IP Address X Tester
     */
    private final HTree<String, TesterNode> testerTree;
    private final TesterMap testerMap;
    /**
     * Number of expected testers.
     */
    private final int expectedTesters;
    private AtomicInteger idCounter = new AtomicInteger(0);

    public GridStrategy(TesterUtil tu) {
        defaults = tu;
        testerMap = new TesterMap();
        testerTree = new HTree<String, TesterNode>(defaults.getTreeOrder());
        expectedTesters = defaults.getExpectedTesters();
    }

    public synchronized int register(DistributedTester tester) {
        LOG.entering("GridStrategy", "register(Tester)");
        String address = "Unknown";
        try {
            address = tester.getAddress();
        } catch (RemoteException ex) {
        }
        LOG.info("New tester:" + address);

        testerMap.put(address, tester);

        synchronized (testerMap) {
            testerMap.notifyAll();
        }
        return idCounter.getAndIncrement();
    }

    public void buildTree() {
        for (Map.Entry<String, Collection<DistributedTester>> each : testerMap.entrySet()) {
            DistributedTester[] aux  = each.getValue().toArray(new DistributedTester[]{});
            testerTree.put(each.getKey(), new TesterNode(aux));
        }
    }

    public int getNodesSize() {
        //return tree.getNodesSize();
        return 0;
    }

    public void setCommunication() {
        HNode<String, TesterNode> node = testerTree.head();
        try {
            this.setCommunication(node);
        } catch (RemoteException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void setCommunication(HNode<String, TesterNode> n) throws RemoteException {
        assert !n.isLeaf();

        HNode<String, TesterNode>[] children = n.children();

        List<TesterNode> nodes = new ArrayList<TesterNode>(children.length);
        for (HNode<String, TesterNode> each : children) {
            nodes.add(each.value());
        }
        TesterNode c = n.value();
        c.register(nodes);

        for (HNode<String, TesterNode> each : children) {
            if (!each.isLeaf()) {
                this.setCommunication(each);
            } else {
                each.value();
            }
        }
    }

    public int getRegistered() {
        return testerMap.size();
    }

    /**
     * Waits for all expected testers to registerMethods.
     */
    public void waitForTesterRegistration() throws InterruptedException {
        LOG.entering("GridStrategy", "waitForTesterRegistration()");

        while (testerMap.size() < expectedTesters) {
            LOG.fine(String.format("Waiting for %d testers to register",
                    expectedTesters - testerMap.size()));
            synchronized (testerMap) {
                testerMap.wait();
            }
        }

    }

    public void startRoot() throws RemoteException {
        DistributedTester root = testerTree.head().value().head();
        root.start();
    }

    public void cleanUp() {
        testerMap.clear();
    }

    class TesterMap {

        private int size = 0;
        private Map<String, Collection<DistributedTester>> testers;

        public TesterMap() {
            testers = new HashMap<String, Collection<DistributedTester>>();
        }
        
        void put(String address, DistributedTester tester) {
            if (!testers.containsKey(address)) {
                testers.put(address, new LinkedList<DistributedTester>());
            }
            testers.get(address).add(tester);
            size++;
        }

        int size() {
            return size;
        }

        void clear() {
            testers.clear();
        }

        Collection<Collection<DistributedTester>> values() {
            return testers.values();
        }

        Set<Map.Entry<String, Collection<DistributedTester>>> entrySet() {
            return testers.entrySet();
        }
    }

    class TesterNode {
        DistributedTester head;
        DistributedTester[] children;

        TesterNode(DistributedTester[] coll) {
            assert coll.length > 0;
            head = coll[0];
            children = new DistributedTester[coll.length -1];
            System.arraycopy(coll, 1, children, 0, coll.length -1);
        }

        void register(List<TesterNode> l) throws RemoteException {
            List<DistributedTester> list = new LinkedList<DistributedTester>();
            list.addAll(Arrays.asList(children));

            for(TesterNode each : l) {
                list.add(each.head);
            }
            head.registerTesters(list);
        }

        void register() throws RemoteException {
            if (children.length > 0) {
                head.registerTesters(Arrays.asList(children));
            }
            
        }

        DistributedTester head() {
            return head;
        }

    }
}


