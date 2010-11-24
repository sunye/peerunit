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
package fr.inria.peerunit.openchordtest.test;

import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.coordinator.CoordinatorImpl;
import fr.inria.peerunit.coordinator.CoordinationStrategy;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RoutingTableTestStrategy implements CoordinationStrategy {

    private static final Logger LOG =
            Logger.getLogger(RoutingTableTestStrategy.class.getName());
    private CoordinatorImpl coordinator;
    private Map<String, MethodDescription> methods;

    public void init(CoordinatorImpl coord) {
        coordinator = coord;
    }

    /**
     * Sequencial execution of test steps.
     *
     * @param schedule
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("RoutingTableTestStrategy", "testCaseExecution()");


        this.execute("initialize");
        this.execute("startModel");

        this.execute("lookupModel");
        this.execute("startBootstrap");
        this.execute("startingNetwork");
        this.execute("nodeCreation");
        this.execute("stabilize");


        for (int i = 0; i < 3; i++) {
            this.execute("updateModel");
        }
        this.execute("unicity");
        this.execute("again");
        this.execute("reunicity");
        this.execute("distance");
        this.execute("printPeer");

        this.execute("print");
        this.execute("quit");

    }

    private void execute(String str) throws InterruptedException {
        if (methods == null) {
            // Lazy initialization of methods Map.
            LOG.log(Level.FINE, "Method map initialization.");
            methods = new HashMap<String, MethodDescription>();
            for (MethodDescription each : coordinator.getSchedule().methods()) {
                methods.put(each.getName(), each);
            }
        }

        if (methods.containsKey(str)) {
            coordinator.execute(methods.get(str));
        } else {
            LOG.log(Level.WARNING, "Method not found: {0}", str);
        }
    }
}
