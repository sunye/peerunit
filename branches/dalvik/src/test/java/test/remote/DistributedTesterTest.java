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
package test.remote;

import fr.univnantes.alma.rmilite.UnexportedException;
import java.rmi.RemoteException;
//import java.rmi.AlreadyBoundException;
//import java.rmi.RemoteException;

import fr.univnantes.alma.rmilite.registry.NamingServer_Socket;
//import java.rmi.registry.LocateRegistry;
import fr.univnantes.alma.rmilite.registry.Registry;
//import java.rmi.registry.Registry;
import fr.univnantes.alma.rmilite.server.RemoteObjectProvider_Socket;
import fr.inria.peerunit.GlobalVariables;
//import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;


import fr.inria.peerunit.Tester;
import fr.inria.peerunit.rmi.tester.DistributedTesterImpl;
import fr.inria.peerunit.util.TesterUtil;

public class DistributedTesterTest {

    @Test
    public void testSerialization() throws Exception {
        Registry registry;
        try {
            NamingServer_Socket nameServer = new NamingServer_Socket();
        	registry = nameServer.createRegistry(1099);
            //registry = LocateRegistry.createRegistry(1099);
            DistributedTesterImpl dt = new DistributedTesterImpl(null, null, null, TesterUtil.instance);
            RemoteObjectProvider_Socket rop = new RemoteObjectProvider_Socket();
            Tester stub = (Tester) rop.exportObject(dt, 0);
            //Tester stub = (Tester) UnicastRemoteObject.exportObject(dt, 0);
            registry.bind("DT", stub);
        } catch (RemoteException e) {
        } catch (UnexportedException e) {
        }

    }
}
