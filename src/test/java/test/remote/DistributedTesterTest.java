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

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;


import fr.inria.peerunit.Tester;
import fr.inria.peerunit.rmi.tester.DistributedTesterImpl;
import fr.inria.peerunit.util.TesterUtil;


public class DistributedTesterTest {
	
	@Test
	public void testSerialization() {
        Registry registry;
		try {
			registry = LocateRegistry.createRegistry(1099);
	        DistributedTesterImpl dt = new DistributedTesterImpl(null, null, null, TesterUtil.instance);
	        Tester stub = (Tester) UnicastRemoteObject.exportObject(dt, 0);
			registry.bind("DT", stub);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}