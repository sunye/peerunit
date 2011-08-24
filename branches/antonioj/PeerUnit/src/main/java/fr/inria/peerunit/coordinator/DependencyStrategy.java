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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dependent execution strategy
 *
 * @author albonico, jeugenio
 */
class DependencyStrategy implements CoordinationStrategy {

    private static final Logger LOG = Logger.getLogger(DependencyStrategy.class.getName());
    private TesterSet testers;

    public void init(TesterSet ts) {
        testers = ts;
    }

    public void testcaseExecution() throws InterruptedException {
        LOG.entering("DependencyStrategy", "testCaseExecution()");

        boolean error;
        ArrayList<String> errorActions = new ArrayList();
        ResultSet rs;

        for (MethodDescription action : testers.getSchedule().methods()) {
            error = false;
            for (String depend: action.getDepends()){
                if (errorActions.contains(depend)){
                    error = true;
                    break;
                }
            }
            if (!error) {
                testers.execute(action);
                rs = testers.getResult(action);
                int errors = 0;
                errors += rs.getErrors();
                errors += rs.getInconclusives();
                errors += rs.getfailures();
                if (rs.getErrors() > 0
                        ||rs.getInconclusives() > 0
                        ||rs.getfailures() > 0) {
                    errorActions.add(action.getName());
                }
            }
            else {
                rs = new ResultSet(action);
                rs.addSimulatedError();
                testers.setResult(action, rs);
                errorActions.add(action.getName());
                LOG.log(Level.FINEST, "Action {0} was not executed due to its dependence!", action.getName());
            }
        }
    }

    public void testcaseExecutionOld() throws InterruptedException {
        LOG.entering("DependencyStrategy", "testCaseExecution()");

        int error = 0;
        ArrayList<String> errorMethods = new ArrayList();
        ResultSet rs;

        for (MethodDescription each : testers.getSchedule().methods()) {
            /**
             * If not exist error (error == 0)
             *      execute a method
             */
            if (error == 0) {
                testers.execute(each);
                rs = testers.getResult(each);

                int errors = 0;
                errors += rs.getErrors();
                errors += rs.getInconclusives();
                errors += rs.getfailures();

                // System.out.println("Dependency: " + each.getDepend());

                /**
                 * If exist error add the method to the errorMethods list.
                 */
                if (errors > 0) {
                    // System.out.println("Error!!! ");
                    errorMethods.add(each.getName());
                }

            }
            /**
             * If an error was found before
             *      verify
             *      read the errorMethods list
             * verifique se uma delas possui relação de dependência com a ação que irá ser executada.
             */
            else {

                Iterator i = errorMethods.iterator();
                int equal = 0;
                while (i.hasNext()) {

                    String compare = (String) i.next();
                    if (each.getDepend().equals(compare)) {

                        equal = 1;

                    }
                }
                /**
                 * Se não possuir relação com a lista de ações com erro e 
                 * o valor do parâmetro depend for diferente de *,
                 * então não depende de uma ação com erro.
                 */
                if (equal != 1 && !each.getDepend().equals("*")) {

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
                        errorMethods.add(each.getName());
                    }

                }
                /**
                 * Se possuir relação com uma ação que retornou erro,
                 * então não execute a ação e sete um erro para ela.
                 */
                else {

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
