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
package fr.inria.peerunit.base;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.Tester;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public abstract class AbstractTester implements Tester {

    private static final Logger log = Logger.getLogger(AbstractTester.class.getName());

    protected int id;
    protected transient GlobalVariables globals;

    /**
     * No arguments constructor.
     * Needed for serialization/deserialization of subclasses.
     */
    public AbstractTester() {
    	
    }

    public AbstractTester(GlobalVariables gv) {
        this.globals = gv;
    }
     /**
     * Returns this tester's id
     * @return the tester's id
     */
    public int getId() {
        log.entering("AbstractTester", "getId()");

        return this.id;
    }

    public void setId(int i) {
        log.entering("AbstractTester", "setId(int)");

       this.id =i;
    }

    public int getPeerName() throws RemoteException {
        return this.getId();
    }

    @Override
    public String toString() {
        return "Tester: " + id;
    }

    /*
    public void execute(MethodDescription m) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void kill() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/

    public void put(Integer key, Object object) throws RemoteException {
        this.globalTable().put(key, object);
    }
    

    public Map<Integer, Object> getCollection() throws RemoteException {
        return this.globalTable().getCollection();
    }

    public Object get(Integer key) throws RemoteException {
        return this.globalTable().get(key);
    }

    public boolean containsKey(Integer key) throws RemoteException {
        return this.globalTable().containsKey(key);
    }

    public void clear() throws RemoteException {
        this.globalTable().clearCollection();
    }

    protected final GlobalVariables globalTable() {
        return globals;
    }
}
