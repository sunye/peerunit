package test;

import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.BeforeClass;

import java.rmi.RemoteException;

/**
 * PeerUnit Test Template
 *
 */
public class SimpleTest {


    private GlobalVariables globals;

    @BeforeClass(range="*",timeout=1000)
    public void begin(){

    }

    @TestStep(range = "*", order = 2, timeout = 1000)
    public void action2() throws RemoteException {

    }

    @TestStep(range="1-3", order = 3, timeout=1000)
    public void action3() throws RemoteException {

    }

    @TestStep(range = "2", order = 4, timeout=1000)
    public void action4()  throws RemoteException {

    }

    @TestStep(range = "*", order = 5, timeout=1000)
    public void action5() throws RemoteException {
        
    }

    @TestStep(range = "*", order = 6, timeout=1000)
    public void action6() throws RemoteException {
		
    }

    @TestStep(range = "*", order = 7, timeout=1000)
    public void action7() throws RemoteException {

    }

    @AfterClass(timeout=1000,range="*")
    public void end() {

    }
}
