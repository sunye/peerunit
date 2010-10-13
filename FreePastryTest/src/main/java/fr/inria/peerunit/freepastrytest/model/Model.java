/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit.freepastrytest.model;

/**
 *
 * @author sunye
 */
public class Model {

    final private P2PSystem system = new P2PSystem();
    final private RemoteModelImpl remote;
    final private Thread thread;
    private boolean running = true;

    public Model(RemoteModelImpl rmi) {
        remote = rmi;
        thread = new Thread(new ModelThread());

    }

    public void start() {
        thread.start();
    }

    public void stop() {
        running = false;
    }

    public void print() {
        system.print();
    }

    class ModelThread implements Runnable {

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
}
