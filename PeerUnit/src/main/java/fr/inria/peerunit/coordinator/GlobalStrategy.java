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
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author albonico
 */
class GlobalStrategy implements CoordinationStrategy {

    private static final Logger LOG = Logger.getLogger(GlobalStrategy.class.getName());
    private TesterSet testers;

    public void init(TesterSet ts) {
        testers = ts;
    }

    /**
     * Global execution of test steps.
     * 
     * @param schedule
     * @throws InterruptedException
     */
    
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("GlobalStrategy", "testCaseExecution()");

        boolean error = false;
        ArrayList<String> errors = new ArrayList<String>();
        
        for (Integer order : testers.getSchedule().orders()) {

            errors = testers.execute(order, testers, errors);

        }
        
    }
}
