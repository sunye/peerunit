/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.freepastrytest.model;

import java.rmi.RemoteException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author sunye
 */
public class RemoteModelImpl implements RemoteModel {

    BlockingQueue<String> newNodes = new LinkedBlockingQueue<String>();

    public void newNode(String id) throws RemoteException {
        newNodes.offer(id);
    }

    public String takeNewNode() throws InterruptedException {
        return newNodes.take();
    }

}
