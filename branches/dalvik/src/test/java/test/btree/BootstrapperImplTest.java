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
package test.btree;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


import fr.univnantes.alma.rmilite.UnexportedException;
import java.rmi.RemoteException;
//import java.rmi.AccessException;
//import java.rmi.AlreadyBoundException;
//import java.rmi.NotBoundException;
//import java.rmi.RemoteException;

import fr.univnantes.alma.rmilite.registry.NamingServer_Socket;
//import java.rmi.registry.LocateRegistry;
import fr.univnantes.alma.rmilite.registry.Registry;
//import java.rmi.registry.Registry;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_Socket;
import fr.inria.peerunit.GlobalVariables;
//import java.rmi.server.UnicastRemoteObject;


import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import fr.inria.peerunit.Bootstrapper;
import fr.inria.peerunit.TestCaseImpl;
import fr.inria.peerunit.btree.BootstrapperImpl;
import fr.inria.peerunit.rmi.tester.DistributedTesterImpl;
import fr.inria.peerunit.util.TesterUtil;

public class BootstrapperImplTest {

    private BootstrapperImpl bootstrapper;
    private Properties properties;
    private TesterUtil defaults;

    @Before
    public void setup() {
        properties = new Properties();
        properties.setProperty("tester.peers", "3");
        properties.setProperty("test.treeStrategy", "1");
        defaults = new TesterUtil(properties);
        bootstrapper = new BootstrapperImpl(defaults);
    }

    // @Test
    public void testBootstrapperIml() {
        /*
         * properties.setProperty("tester.peers", "1"); TesterUtil defaults =
         * new TesterUtil(properties); fr.inria.peerunit.Bootstrapper b = new
         * BootstrapperImpl(defaults); Node node = mock(Node.class); try {
         * b.register(node); } catch (RemoteException e) { fail(e.getMessage());
         * }
         *
         * assertTrue(b != null);
         */
    }

    @Test
    public void testRegister() {
        int id = 0;
        try {
            for (int i = 0; i < 5; i++) {
                DistributedTesterImpl tester = mock(DistributedTesterImpl.class);
                id = bootstrapper.register(tester);
                assertTrue(id == i);
            }

        } catch (RemoteException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoteRegister() throws Exception {
        try {
            NamingServer_Socket nameServer = new NamingServer_Socket();
        	Registry registry = nameServer.createRegistry(1099);
            //Registry registry = LocateRegistry.createRegistry(1099);
                RemoteObjectProvider_Socket rop = new RemoteObjectProvider_Socket();
        	Bootstrapper stub = (Bootstrapper) rop.exportObject(bootstrapper, 0);
            //Bootstrapper stub = (Bootstrapper) UnicastRemoteObject.exportObject(bootstrapper, 0);
            registry.bind("BootTest", stub);

            Bootstrapper remoteBoot = (Bootstrapper) registry.lookup("BootTest");
            DistributedTesterImpl tester = new DistributedTesterImpl(TestCaseImpl.class, remoteBoot, null, defaults);
            tester.register();

            assertTrue(tester.getId() == 0);
        } catch (UnexportedException e) {
            fail(e.getMessage());
        } catch (RemoteException e) {
            fail(e.getMessage());
        } 

    }

    @Test
    public void testGetRegistered() {
        // fail("Not yet implemented");
    }

    @Test
    public void testIsRoot() {
        // fail("Not yet implemented");
    }
}
