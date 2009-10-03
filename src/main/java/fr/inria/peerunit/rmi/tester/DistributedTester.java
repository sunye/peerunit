/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.rmi.tester;

import fr.inria.peerunit.Coordinator;
import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.base.AbstractTester;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.test.oracle.Verdicts;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author sunye
 */
public class DistributedTester extends AbstractTester implements Tester, Coordinator {
    

    public DistributedTester(GlobalVariables gv) {
        super(gv);
    }

    public void registerMethods(Tester tester, List<MethodDescription> list) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int register(Tester t) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void methodExecutionFinished() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void quit(Tester t, Verdicts v) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
