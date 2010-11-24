/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.dhtmodel;

import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class Model {
    private static final Logger LOG = Logger.getLogger(Model.class.getName());

    final private P2PSystem system = new P2PSystem();
    final private RemoteModelImpl remote;
    final private Thread nodeCreationThread;
    final private Thread nodeUpdateThread;
    private boolean running = true;

    public Model(RemoteModelImpl rmi) {
        remote = rmi;
        nodeCreationThread = new Thread(new NewNodeThread());
        nodeUpdateThread = new Thread(new NodeUpdateThread());

    }

    public void start() {
        nodeCreationThread.start();
        nodeUpdateThread.start();
    }

    public void stop() {
        //running = false;
        nodeCreationThread.interrupt();
        nodeUpdateThread.interrupt();
    }

    public void print() {
        system.print();
    }

    public boolean unicity() {
         return system.unicity();
    }

    public boolean distance() {
        return system.distance();
    }

    class NewNodeThread implements Runnable {

        public void run() {
            try {
                while (running) {
                    String id = remote.takeNewNode();
                    system.newNode(id);
                }
            } catch (InterruptedException ex) {
                //Nothing for now.
            }

        }
    }

    class NodeUpdateThread implements Runnable {

        public void run() {
            try {
                while (running) {
                    NodeUpdate nu = remote.takeNodeUpdate();
                    system.nodeUpdate(nu.id(), nu.neighbors());
                }
            } catch (InterruptedException ex) {
                //Nothing for now.
            }

        }
    }
}
