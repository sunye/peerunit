/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.dhtmodel;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author sunye 
 */
public class RemoteModelImpl implements RemoteModel {

    BlockingQueue<String> newNodes = new LinkedBlockingQueue<String>();

    BlockingQueue<NodeUpdate> nodeUpdates =
            new LinkedBlockingQueue<NodeUpdate>();

    public void newNode(String id) throws RemoteException {
        newNodes.offer(id);
    }

    public void updateNode(String id, Set<String> neighbors)
            throws RemoteException {
        nodeUpdates.offer(new NodeUpdate(id, neighbors));
    }


    public String takeNewNode() throws InterruptedException {
        return newNodes.take();
    }

    public NodeUpdate takeNodeUpdate() throws InterruptedException {
        return nodeUpdates.take();
    }
}
