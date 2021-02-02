package test;


import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.TestStep;

import java.rmi.RemoteException;

/**
 * PeerUnit Test Template
 *
 */
public class MainTest {


    private GlobalVariables globals;

    @TestStep(range = "1", order = 1)
    public void testOne() throws RemoteException {
		assert true;
    }

    @TestStep(range = "*", order = 2, timeout = 40)
    public void testTwo() throws RemoteException {

    }

    @TestStep(range = "2", order = 3)
    public void putData() throws RemoteException {

    }

    @TestStep(range = "*", order = 4)
    public void getData()  throws RemoteException {

    }

    @TestStep(range = "1", order = 5)
    public void putMoreData() throws RemoteException {
        
    }

    @TestStep(range = "*", order = 6)
    public void getMoreData() throws RemoteException {
		
    }

    @TestStep(range = "3", order = 7)
    public void makeFail() throws RemoteException {
        assert false : "This test step should fail";
    }

    @TestStep(range = "*", order = 8, timeout = 1000)
    public void makeTimeout() throws InterruptedException {
        Thread.sleep(2000);
    }

    @TestStep(range = "0", order = 9, timeout = 1000)
    public void setAGlobal() throws Exception {
        globals.put(1, "Juste one value");
    }   
 
    @TestStep(range = "*", order = 10, timeout = 1000)
    public void getAGlobal() throws Exception {
        String response = (String) globals.get(1);
        
        assert response.equals("Juste one value");
    } 
    
    
    @SetGlobals
    public void setGlobals(GlobalVariables gv) {
        globals = gv;
    }
}
