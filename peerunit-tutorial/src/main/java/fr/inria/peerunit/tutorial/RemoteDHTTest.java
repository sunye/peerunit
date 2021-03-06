package fr.inria.peerunit.tutorial;

import fr.inria.mockdht.MockDHT;
import fr.inria.mockdht.RemoteDHT;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.TestStep;
import static fr.inria.peerunit.tester.Assert.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Remote DHT Test
 *
 */
public class RemoteDHTTest {

    private RemoteDHT mock;
    private GlobalVariables globals;

    @TestStep(range = "1", order = 1)
    public void startDHT() throws RemoteException {
        MockDHT dht = new MockDHT();
        RemoteDHT stub = (RemoteDHT) UnicastRemoteObject.exportObject(dht, 0);
        Registry registry = LocateRegistry.createRegistry(1222);
        registry.rebind("DHTService", stub);
    }

    @TestStep(range = "*", order = 2, timeout = 40)
    public void lookupDHT() throws RemoteException, NotBoundException {
        Registry registry;
        registry = LocateRegistry.getRegistry("127.0.0.1", 1222);
        mock = (RemoteDHT) registry.lookup("DHTService");
    }

    @TestStep(range = "2", order = 3)
    public void putData() throws RemoteException {
        mock.put("b", "wrong");
        mock.put("a", "toto");
        mock.put("b", "halo");
    }

    @TestStep(range = "*", order = 4)
    public void getData()  throws RemoteException {
		assertEquals("toto", mock.get("a"));
        assertEquals("halo", mock.get("b"));
    }

    @TestStep(range = "1", order = 5)
    public void putMoreData() throws RemoteException {
        mock.put("a", "more");
    }

    @TestStep(range = "*", order = 6)
    public void getMoreData() throws RemoteException {
		assertEquals("more", mock.get("a"));
    }

    @TestStep(range = "3", order = 7)
    public void makeFail() throws RemoteException {
        assertEquals("mm", mock.get("a"));
    }

    @TestStep(range = "*", order = 8, timeout = 1000)
    public void makeTimeout() throws InterruptedException {
        Thread.sleep(2000);
    }

    @TestStep(range = "0", order = 9, timeout = 1000)
    public void setAGlobal() throws Exception {
        globals.put(1, "Just one value");
    }   
 
    @TestStep(range = "*", order = 10, timeout = 1000)
    public void getAGlobal() throws Exception {
        String response = (String) globals.get(1);
        
        assert response.equals("Just one value");
    } 
    
    
    @AfterClass
    public void end() {
    }
    
    @SetGlobals
    public void setGlobals(GlobalVariables gv) {
        globals = gv;
    }
}
