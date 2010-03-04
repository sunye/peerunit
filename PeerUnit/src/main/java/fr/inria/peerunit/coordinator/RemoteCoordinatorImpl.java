/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;
import fr.inria.peerunit.remote.Bootstrapper;
import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.remote.Tester;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class RemoteCoordinatorImpl implements Bootstrapper, Coordinator {

    private static final Logger LOG = Logger.getLogger(RemoteCoordinatorImpl.class.getName());

    private BlockingQueue<TesterRegistration> registrations;
    private BlockingQueue<ResultSet> results;
    private BlockingQueue<Tester> leaving;
    final private AtomicInteger runningTesters;

    public RemoteCoordinatorImpl(int expectedTesters) {
        registrations = new ArrayBlockingQueue<TesterRegistration>(expectedTesters);
        results = new ArrayBlockingQueue<ResultSet>(expectedTesters);
        leaving = new ArrayBlockingQueue<Tester>(expectedTesters);
        runningTesters = new AtomicInteger(0);
    }


    public void registerTesters(List<Tester> testers) throws RemoteException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void registerMethods(Tester t, Collection<MethodDescription> coll) throws RemoteException {
        registrations.offer(new TesterRegistration(t, coll));
    }

    public void methodExecutionFinished(ResultSet result) throws RemoteException {
        results.offer(result);
    }

    public void quit(Tester t) throws RemoteException {
        leaving.offer(t);
    }

    BlockingQueue<TesterRegistration> registrations() {
        return registrations;
    }

     BlockingQueue<ResultSet> results() {
        return results;
    }

    BlockingQueue<Tester> leaving() {
        return leaving;
    }

    /*
     * (non-Javadoc)
     *
     * @see callback.Coordinator#namer(callback.Tester) Incremented with
     * java.util.concurrent to handle the semaphore concurrency access
     */
    public int register(Tester t) throws RemoteException {
        LOG.entering("CoordinatorIml", "register(Tester)");
        int id = runningTesters.getAndIncrement();
        LOG.fine("New Registered Tester: " + id + " new client " + t);
        return id;
    }

    public void quit() throws RemoteException {
        throw new UnsupportedOperationException("Not supported.");
    }
}
