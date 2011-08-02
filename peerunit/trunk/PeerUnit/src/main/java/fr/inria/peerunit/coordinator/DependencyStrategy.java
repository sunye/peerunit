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
import java.util.Iterator;
import java.util.logging.Logger;

/**
 *
 * @author albonico
 */
class DependencyStrategy implements CoordinationStrategy {

    private static final Logger LOG = Logger.getLogger(DependencyStrategy.class.getName());
    private TesterSet testers;

    public void init(TesterSet ts) {
        testers = ts;
    }

    /**
     * Sequencial execution of test steps.
     * 
     * @param schedule
     * @throws InterruptedException
     */
    
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("DependencyStrategy", "testCaseExecution()");

        int error = 0;
        ArrayList<String> orderMethod = new ArrayList();
        ResultSet rs;

        for (MethodDescription each : testers.getSchedule().methods()) {

            if (error == 0) {

                testers.execute(each);
                rs = testers.getResult(each);

                int errors = 0;
                errors += rs.getErrors();
                errors += rs.getInconclusives();
                errors += rs.getfailures();

               // System.out.println("Dependency: " + each.getDepend());

                if (errors > 0) {
                   // System.out.println("Error!!! ");
                    error = 1;
                    orderMethod.add(each.getName());
                }

            } else {

                Iterator i = orderMethod.iterator();
                int equal = 0;
                while (i.hasNext()) {

                    String compare = (String) i.next();
                    if (each.getDepend().equals(compare)) {
                        
                        equal = 1;

                    }
                }

                if (equal != 1 && !each.getDepend().equals("*")){

                    testers.execute(each);
                    rs = testers.getResult(each);

                    int errors = 0;
                    errors += rs.getErrors();
                    errors += rs.getInconclusives();
                    errors += rs.getfailures();

                  //  System.out.println("Dependency: " + each.getDepend());

                    if (errors > 0) {
                     //   System.out.println("Error!!! ");
                        error = 1;
                        orderMethod.add(each.getName());
                    }

                } else {

                    rs = new ResultSet(each);
                    rs.addSimulatedError();
                    testers.setResult(each, rs);

                    String action = "";
                    if (each.getDepend().equals("*")) {
                        action = "all actions";
                    } else {
                        action = "action " + each.getDepend();
                    }

                    String message = "Step " + each.getOrder() + " was not executed because it has dependendency with " + action + "!";
                    LOG.info(message);
                    System.out.println(message);
                 
                }
            }
        }
    }
}
