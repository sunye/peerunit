/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.tester;

import fr.inria.peerunit.remote.Coordinator;
import fr.inria.peerunit.common.MethodDescription;


import fr.inria.peerunit.remote.Tester;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author sunye
 */
public class RemoteTesterImpl implements Tester, Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * The tester id
     */
    protected int id;
    /**
     * The (remote) Coordinator for this tester
     */
    private transient Coordinator coordinator;
    /**
     * The next TestStep to be executed.
     */
    private transient MethodDescription action;
    private transient AtomicBoolean start = new AtomicBoolean(false);
    private transient AtomicBoolean stop = new AtomicBoolean(false);
    /**
     * Locks for blocking multi-threaded methods.
     */
    private final Lock lock = new ReentrantLock();
    private final Condition newAction = lock.newCondition();
    private final Condition hasCoordinator = lock.newCondition();
    private final Condition mayStart = lock.newCondition();

    /**
     * 
     */
    public void setCoordinator(Coordinator coord) throws RemoteException {
        lock.lock();
        try {
            this.coordinator = coord;
            hasCoordinator.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 
     */
    public void execute(MethodDescription m) throws RemoteException {
        action = m;
        lock.lock();
        try {
            action = m;
            newAction.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 
     */
    public int getId() throws RemoteException {
        return id;
    }

    /**
     * 
     */
    public void stop() throws RemoteException {
        this.stop.set(true);
    }

    /**
     * @see 
     */
    public void start() throws RemoteException {
        lock.lock();
        try {
            start.set(true);
            mayStart.signal();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the next TestStep to execute.
     * Blocks until a new TestStep is available.
     * 
     * @return The description of the TestStep to be executed
     * @throws InterruptedException
     */
    public MethodDescription take() throws InterruptedException {
        MethodDescription md;
        lock.lock();
        try {
            while (action == null) {
                newAction.await();
            }
            md = action;
            action = null;
        } finally {
            lock.unlock();
        }

        return md;
    }

    /**
     * Returns the coordinator for this tester.
     * Blocks until the coordinator is available.
     * 
     * @return The Coordinator for this tester.
     * @throws InterruptedException
     */
    public Coordinator takeCoordinator() throws InterruptedException {
        lock.lock();
        try {
            while (coordinator == null) {
                hasCoordinator.await();
            }
        } finally {
            lock.unlock();
        }

        return coordinator;
    }

    /**
     * Waits for the arrival of a start message.
     * Blocks the current thread until the start attribute becomes true;
     * 
     * @throws InterruptedException
     */
    public void waitForStart() throws InterruptedException {
        lock.lock();
        try {
            while (!start.get()) {
                mayStart.await();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns true if the current tester should stop and false for any other case.
     * 
     * @return
     */
    public boolean shouldStop() {
        return stop.get();
    }

    public void kill() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void put(Integer key, Object object) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<Integer, Object> getCollection() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object get(Integer key) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsKey(Integer key) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clear() throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}