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
package fr.inria.peerunit.tester;

import fr.inria.peerunit.Globals;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.util.PeerUnitLogger;
import fr.inria.peerunit.util.TesterUtil;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author sunye
 */
public abstract class AbstractTester {

    private static final Logger LOG = Logger.getLogger(AbstractTester.class.getName());
    int id = 0;
    transient GlobalVariables globals = null;
    transient TesterUtil defaults = TesterUtil.instance;

    /**
     * No arguments constructor.
     * Needed for serialization/deserialization of subclasses.
     */
    AbstractTester() {
    }

    AbstractTester(GlobalVariables gv) {
        this.globals = gv;
    }

    /**
     * Returns this tester's id
     *
     * @return the tester's id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the tester id.
     *
     * @param i Id.
     */
    void setId(int i) {
        this.id = i;
        this.initializeGlobals();
    }

    @Override
    public String toString() {
        return String.format("Tester: %d", id);
    }

    public void put(Integer key, Object object) throws RemoteException {
        this.globalTable().put(key, object);
    }

    public Object get(Integer key) throws RemoteException {
        return this.globalTable().get(key);
    }

    final GlobalVariables globalTable() {
        return globals;
    }

    void initializeLogger() {
        try {
            PeerUnitLogger.createLogger(defaults, String.format("Tester%d.log", id));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private void initializeGlobals() {
        Globals.setId(id);
    }
}