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
package fr.inria.peerunit.rmi.coord;

import java.rmi.RemoteException;
import fr.inria.peerunit.parser.MethodDescription;
import fr.inria.peerunit.tester.Tester;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Eduardo Almeida.
 * @version 1.0
 * @since 1.0
 * @see java.lang.Runnable
 * @see fr.inria.peerunit.tester.Tester
 * @see fr.inria.peerunit.parser.MethodDescription
 */
public class MethodExecute implements Runnable {

    private static final Logger LOG = Logger.getLogger(MethodExecute.class.getName());

    private Tester tester;
    private MethodDescription md;

    /**
     *
     * @param t the tester.
     * @param m the method to be executed by the tester.
     */
    public MethodExecute(Tester t, MethodDescription m) {
        assert t != null : "Null Tester";
        assert m != null : "Null MethodDescription";
        
        tester = t;
        md = m;
    }

    /**
     * Asks the Tester to execute the Method.
     */
    public void run() {
        try {
            tester.execute(md);
        } catch (RemoteException e) {
            LOG.log(Level.SEVERE,null, e);
            for (StackTraceElement each : e.getStackTrace()) {
                LOG.severe(each.toString());
            }
        }
    }
}
