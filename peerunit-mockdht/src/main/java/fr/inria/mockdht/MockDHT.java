package fr.inria.mockdht;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class MockDHT implements RemoteDHT {

    private Map<String, Serializable> data = new HashMap<String, Serializable>();


    public MockDHT() {

    }


    public MockDHT(String brokerAdress) {
        Registry registry;
        try {
            // enregistre MockDHT dans RMI
            RemoteDHT stub = (RemoteDHT) UnicastRemoteObject.exportObject(this, 0);
            //trouve le service de resolution de nom de RMI
            registry = LocateRegistry.getRegistry(brokerAdress);
            //registry = LocateRegistry.createRegistry(1099);
            //enregistre la référence dans le service de résolution
            registry.rebind("DHTService", stub);
        } catch (RemoteException e) {

            System.out.println("l'objet n'a pas pu être dans le registre RMI");
        }
    }

    public void put(String k, Serializable v) throws RemoteException {
        data.put(k, v);
    }

    public Serializable get(String k) throws RemoteException {

        Serializable s = data.get(k);
        return s;
    }

    public static void main(String args[]) {
        //le registre RMI torunera dans le serveur
        @SuppressWarnings("unused")
        MockDHT dht;
        try {
            dht = new MockDHT(InetAddress.getLocalHost().getHostAddress());

        } catch (UnknownHostException e) {

            e.printStackTrace();
        }


    }
}


