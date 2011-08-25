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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dependent execution strategy
 *
 * @author albonico, jeugenio
 */
class DependencyStrategy implements CoordinationStrategy {

    private static final Logger LOG = Logger.getLogger(DependencyStrategy.class.getName());
    private TesterSet testers = null;

    public void init(TesterSet ts) {
        testers = ts;
    }

    public void testCaseExecution() throws InterruptedException {
        LOG.entering("DependencyStrategy", "testCaseExecution()");


        /**
         * One should reason about this code and refactor it.
         */

        boolean error;
        ArrayList<String> errorActions = new ArrayList<String>();
        ResultSet rs;

        for (MethodDescription action : testers.getSchedule().methods()) {
            error = false;
            for (String depend : action.getDepends()) {
                if (errorActions.contains(depend)) {
                    error = true;
                    break;
                }
            }
            if (!error) {
                testers.execute(action);
                rs = testers.getResult(action);
                int errors = 0;
                errors += rs.getErrors();
                errors += rs.getInconclusive();
                errors += rs.getFailures();
                if (rs.getErrors() > 0
                        || rs.getInconclusive() > 0
                        || rs.getFailures() > 0) {
                    errorActions.add(action.getName());
                }
            } else {
                rs = new ResultSet(action);
                rs.addSimulatedError();
                testers.setResult(action, rs);
                errorActions.add(action.getName());
                LOG.log(Level.FINEST, "Action {0} was not executed due to its dependence!", action.getName());
            }
        }
    }
}
