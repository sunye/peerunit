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
package fr.inria.peerunit.remote;

import fr.inria.peerunit.base.ResultSet;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import fr.inria.peerunit.parser.MethodDescription;

import java.rmi.Remote;

public interface Coordinator extends Remote {
	
	
	public void registerTesters(List<Tester> testers) throws RemoteException;
	
	
	/**
	 * @param tester
	 * @param list
	 * @throws RemoteException
	 * @author Eduardo Almeida, Veronique Pelleau
	 */
	public void registerMethods(Tester tester, Collection<MethodDescription> list)
			throws RemoteException;

	public void methodExecutionFinished(ResultSet result)
			throws RemoteException;

	/**
	 * Finish the test case and calculates the global oracle
	 * 
	 * @param Tester
	 * @param Verdict
	 * 
	 */
	public void quit(Tester t) throws RemoteException;
	
}
