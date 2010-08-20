package fr.inria.mockdht;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDHT extends Remote {
        public void put(String k, Serializable v) throws RemoteException;
        public Serializable get(String k) throws RemoteException;
}
