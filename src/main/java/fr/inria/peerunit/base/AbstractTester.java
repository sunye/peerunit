/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import fr.inria.peerunit.GlobalVariables;
import fr.inria.peerunit.Tester;
import fr.inria.peerunit.parser.MethodDescription;
import java.rmi.RemoteException;
import java.util.Map;

/**
 *
 * @author sunye
 */
public abstract class AbstractTester implements Tester {
    private int id;
    private GlobalVariables globals;


    public AbstractTester(GlobalVariables gv) {
        this.globals = gv;
    }
     /**
     * Returns this tester's id
     * @return the tester's id
     */
    public int getId() {
        return this.id;
    }

    public void setId(int i) {
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

    private final GlobalVariables globalTable() {
        return globals;
    }
}
